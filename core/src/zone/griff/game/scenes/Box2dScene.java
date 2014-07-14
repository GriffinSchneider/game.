package zone.griff.game.scenes;

import static zone.griff.game.B2DVars.PPM;
import zone.griff.game.B2DVars;
import zone.griff.game.MyContactListener;
import zone.griff.game.SceneManager;
import zone.griff.game.pools.Vector2Pool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.RubeSceneLoader;

public class Box2dScene extends Scene {
	
	// Tuning
	private static Vector2 GRAVITY = new Vector2(0, -14);

	private static float CAMERA_LERP_FACTOR = 0.1f;
	
	private static float PLAYER_MOVEMENT_X_VELOCITY = 2.3f;
	private static float PLAYER_MOVEMENT_MULTIPLIER = 0.18f;
	private static float PLAYER_STOPPING_MULTIPLIER = 2.0f;
	private static float PLAYER_JUMP_Y_VELOCITY = 3;

	private static float WORLD_STEP_TIME = 1.0f/60.0f;
	
	
	private World world;
	private MyContactListener cl;
	private Box2DDebugRenderer b2dr;
	private Body playerBody;
	private OrthographicCamera b2dCam;
	
	private PolygonSpriteBatch polyBatch;
	private Array<PolygonSprite> polySprites;
	
	public Box2dScene(SceneManager sceneManager) {
		super(sceneManager);

		this.world = new World(GRAVITY, true);
		
		this.cl = new MyContactListener();
		this.world.setContactListener(cl);
		
		this.b2dr = new Box2DDebugRenderer();
		
		this.b2dCam = new OrthographicCamera();
		
		this.setupScene();
		this.setupShader();
		this.setupPlayerBody();
	}
	
	public void setupPlayerBody() {
		BodyDef bdef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fdef = new FixtureDef();
		
		// create player
		bdef.position.set(160 / PPM, 200 / PPM);
		bdef.type = BodyType.DynamicBody;
		this.playerBody = world.createBody(bdef);
		
		shape.setAsBox(5 / PPM, 5 / PPM);
		fdef.shape = shape;
		this.playerBody.createFixture(fdef).setUserData("player");
		
		// create foot sensor
		shape.setAsBox(2 / PPM, 2 / PPM, new Vector2(0, -5 / PPM), 0);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		fdef.isSensor = true;
		this.playerBody.createFixture(fdef).setUserData("foot");
	}
	
	public void setupScene() {
		RubeSceneLoader loader = new RubeSceneLoader(world);
		RubeScene scene = loader.loadScene(Gdx.files.internal("levels/json/untitled1.json"));

		this.polyBatch = new PolygonSpriteBatch();
		this.polySprites = new Array<PolygonSprite>();

		Texture textureGround =  new Texture(Gdx.files.internal("badlogic.jpg"));
	  textureGround.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	  TextureRegion texreg = new TextureRegion(textureGround,0,0,1,1);
	  texreg.setTexture(textureGround);
		
		for (Body body : scene.getBodies()) {
			for (Fixture fixture : body.getFixtureList()) {
				this.polySprites.add(this.polygonSpriteForFixture(fixture, texreg));
			}
		}
	}
	
	public PolygonSprite polygonSpriteForFixture(Fixture fixture, TextureRegion texreg) {
		PolygonShape shape = (PolygonShape)fixture.getShape();
		Body body = fixture.getBody();
		
		Vector2 tmp = Vector2Pool.obtain();
				
		int vertexCount = shape.getVertexCount();
		float[] vertices = new float[vertexCount * 2];
		for (int k = 0; k < vertexCount; k++) {
			shape.getVertex(k, tmp);
			tmp.rotate(body.getAngle() * MathUtils.radiansToDegrees);
			tmp.add(body.getPosition());
			vertices[k * 2] = tmp.x;
			vertices[k * 2 + 1] = tmp.y;
		}

		Vector2Pool.release(tmp);

		short triangles[] = new EarClippingTriangulator().computeTriangles(vertices).toArray();
		PolygonRegion region = new PolygonRegion(texreg, vertices, triangles);

		return new PolygonSprite(region);
	}
	
	Texture palatte;
	int palatteSize;
	ShaderProgram shader;
	
	public void setupShader() {
		this.palatteSize = 5;
		Pixmap pixmap = new Pixmap(palatteSize, 1, Format.RGBA8888);
		pixmap.drawPixel(0, 0, 0x69D2E7FF);
		pixmap.drawPixel(1, 0, 0xA7DBD8FF);
		pixmap.drawPixel(2, 0, 0xE0E4CCFF);
		pixmap.drawPixel(3, 0, 0xF38630FF);
		pixmap.drawPixel(4, 0, 0xFA6900FF);
		this.palatte = new Texture(pixmap);
		pixmap.dispose();

		this.shader = new ShaderProgram(
				Gdx.files.internal("shaders/default.vert"), 
				Gdx.files.internal("shaders/wobblyCircles.frag"));

		if (!this.shader.isCompiled()) {
			Gdx.app.log("Shader",  "compile errors!\n-----\n" + this.shader.getLog() + "-----");
		}
	}
	 
	@Override
	public void resize (int width, int height) {
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		b2dCam.setToOrtho(false, camWidth, camHeight);
	}
	
	public void update(float dt) {
		this.updateInput(dt);
		world.step(WORLD_STEP_TIME, 6, 2);

		Vector2 playerCenter = this.playerBody.getWorldCenter();
		Vector3 cameraCenter = this.b2dCam.position;
		
		cameraCenter.x += (playerCenter.x - cameraCenter.x) * CAMERA_LERP_FACTOR;
		cameraCenter.y += (playerCenter.y - cameraCenter.y) * CAMERA_LERP_FACTOR;

		this.b2dCam.update();
	}

	public void updateInput(float dt) {
		// Jump
		if (Gdx.input.isKeyPressed(Keys.I) || Gdx.input.isKeyPressed(Keys.SPACE)) {
			this.playerBody.setLinearVelocity(this.playerBody.getLinearVelocity().x, PLAYER_JUMP_Y_VELOCITY);
		}
		
		// Move left/right or stop
		if (Gdx.input.isKeyPressed(Keys.J)) {
			this.movePlayer(false, dt);
		} else if (Gdx.input.isKeyPressed(Keys.L)) {
			this.movePlayer(true, dt);
		} else {
			this.stopPlayer(dt);
		}

		// Zoom
		if (Gdx.input.isKeyPressed(Keys.EQUALS)) {
			this.b2dCam.zoom += 0.03f;
		} else if (Gdx.input.isKeyPressed(Keys.MINUS)) {
			this.b2dCam.zoom -= 0.03f;
		}
	}

	public void movePlayer(boolean right, float dt) {
		Vector2 playerVelocity = this.playerBody.getLinearVelocity();
		Vector2 playerCenter = this.playerBody.getWorldCenter();
		float desiredVel = right ? PLAYER_MOVEMENT_X_VELOCITY : -PLAYER_MOVEMENT_X_VELOCITY;
		float velChange = (desiredVel - playerVelocity.x) * PLAYER_MOVEMENT_MULTIPLIER;
		this.playerBody.applyLinearImpulse(velChange, 0, playerCenter.x, playerCenter.y, true);
	}

	public void stopPlayer(float dt) {
		Vector2 playerVelocity = this.playerBody.getLinearVelocity();
		Vector2 playerCenter = this.playerBody.getWorldCenter();
		this.playerBody.applyForce(playerVelocity.x * -PLAYER_STOPPING_MULTIPLIER,
				0, playerCenter.x, playerCenter.y, true);
	}
	
	@Override
	public void render() {

		this.polyBatch.setProjectionMatrix(this.b2dCam.combined);
		this.polyBatch.begin();
		this.polyBatch.disableBlending();

		this.polyBatch.setShader(this.shader);
		this.shader.setUniformf("iGlobalTime", this.sceneManager.gameTime);
//		this.shader.setUniform3fv("iResolution", this.sceneManager.gameSizeArray, 0, 3);
		this.shader.setUniformf("palatteSize", palatteSize);
		this.shader.setUniform2fv("center", this.sceneManager.gameSizeArray, 0, 2);

		// sprite.draw() will call bind() on the sprite's texture.
		// So, bind the palatte to texture #1, and then set the active texture to #0
		// to stop sprite.draw() from binding over the palatte.
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
		this.palatte.bind();
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);

		this.shader.setUniformi("palatte", 1);

		for (PolygonSprite sprite : this.polySprites) {
			sprite.draw(this.polyBatch);
		}
		
		this.polyBatch.end();

		b2dr.render(world, this.b2dCam.combined);
	}
	
	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

}
