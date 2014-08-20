package zone.griff.game.scenes;

import static zone.griff.game.B2DVars.PPM;
import zone.griff.game.MovingPlatform;
import zone.griff.game.MyContactListener;
import zone.griff.game.SceneManager;
import zone.griff.game.ShaderBackground;
import zone.griff.game.scenes.Player.MoveDirection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.RubeSceneLoader;
 
public class Box2dScene extends Scene {
	
	// Tuning
	private static Vector2 GRAVITY = new Vector2(0, -50);

	private static float CAMERA_LERP_FACTOR = 0.1f;
	
	private static float WORLD_STEP_TIME = 1.0f/60.0f;
	
	private World world;
	private Box2DDebugRenderer b2dr;
	private OrthographicCamera b2dCam;

	private Player player;
	private Array<MovingPlatform> movingPlatforms;
	
	private PolygonSpriteBatch polyBatch;
	private Array<PolygonSprite> groundPolySprites;
	
	private ShaderBackground background;
	
	private MyContactListener contactListener;

	// "buttons"
	static final float moveLeftEnd = 0.1f;
	static final float moveRightEnd = 0.3f;
	static final float jumpStart = 0.7f;
	
	public Box2dScene(SceneManager sceneManager) {
		super(sceneManager);

		this.world = new World(GRAVITY, true);
		
		this.b2dr = new Box2DDebugRenderer();
		
		this.b2dCam = new OrthographicCamera();
		
		this.movingPlatforms = new Array<MovingPlatform>();

		this.polyBatch = new PolygonSpriteBatch();

		this.groundPolySprites = new Array<PolygonSprite>();

		this.background = new ShaderBackground(sceneManager);

		this.setupScene();
		this.setupShader();
		this.player = new Player(this.world);
		
		this.contactListener = new MyContactListener(this.player);
		this.world.setContactListener(this.contactListener);
		
		final Box2dScene t = this;
		Gdx.input.setInputProcessor(new InputProcessor() {
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) { 
				if (screenX/(float)t.sceneManager.screenWidth > jumpStart) {
					t.jumpPressed();
				}
				return false;
			}
			@Override
			public boolean scrolled(int amount) { return false; }
			@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
			@Override
			public boolean keyUp(int keycode) { return false; }
			@Override
			public boolean keyTyped(char character) { return false; }
			@Override
			public boolean keyDown(int keycode) {
				if (keycode == Keys.SPACE) {
					t.jumpPressed();
				}
				return false;
			}
		});
	}
	
	private static final String MOVING = "moving";
	
	public void setupScene() {
		RubeSceneLoader loader = new RubeSceneLoader(world);
		RubeScene scene = loader.loadScene(Gdx.files.internal("levels/json/untitled1.json"));
		
		Texture textureGround =  new Texture(Gdx.files.internal("badlogic.jpg"));
	  textureGround.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	  TextureRegion texreg = new TextureRegion(textureGround,0,0,1,1);
	  texreg.setTexture(textureGround);
		
		for (Body body : scene.getBodies()) {
			String platformType = this.getPlatformType(body, scene); 
			Gdx.app.log("", "" + platformType);
			if (this.isMovingPlatformType(platformType)) {
				this.setupMovingPlatform(body, texreg, scene);
			} else {
				this.setupGroundPlatform(body, texreg, scene);
			}
		}
	}
	

	public void setupMovingPlatform(Body moving, TextureRegion texreg, RubeScene scene) {
		
		Array<Vector2> targetPoints = new Array<Vector2>();
		Transform t = moving.getTransform();

		Body platformBody = null;
		Fixture platformFixture = null;

		for (Fixture f : new Array<Fixture>(moving.getFixtureList())) {
			Shape shape = f.getShape();
			if (f.isSensor()) {
				Vector2 p = new Vector2().set(((CircleShape) shape).getPosition());
				t.mul(p);
				targetPoints.add(p);
			} else {
				// Split out the actual platform fixture into a different body than the
				// sensors, so that the debug renderer won't show the sensors moving around
				// with the platform
				FixtureDef fdef = new FixtureDef();
				fdef.shape = f.getShape();
				fdef.friction = f.getFriction();
				fdef.restitution = f.getRestitution();
				fdef.density = f.getDensity();

				BodyDef bdef = new BodyDef();
				bdef.type = BodyType.KinematicBody;
				bdef.position.set(moving.getPosition());
				platformBody = scene.getWorld().createBody(bdef);
				platformFixture = platformBody.createFixture(fdef);

				moving.destroyFixture(f);
			}
		}

		this.movingPlatforms.add(new MovingPlatform(
				platformBody, 
				targetPoints,
				Box2DHelper.polygonSpriteForFixture(platformFixture, texreg)));
	}
	
	public boolean isMovingPlatformType(String platformType) {
		return platformType != null && platformType.equals(MOVING);
	}


	public String getPlatformType(Body body, RubeScene scene) {
		return (String)scene.getCustom(body, "platformType");
	}
	
	public void setupGroundPlatform(Body body, TextureRegion texreg, RubeScene scene) {
		for (Fixture fixture : body.getFixtureList()) {
			this.groundPolySprites.add(Box2DHelper.polygonSpriteForFixture(fixture, texreg));
		}
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
				Gdx.files.internal("shaders/angularGround.frag"));

		if (!this.shader.isCompiled()) {
			Gdx.app.log("Shader",  "compile errors!\n-----\n" + this.shader.getLog() + "-----");
		}
	}
	 
	@Override
	public void resize (int width, int height) {
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		b2dCam.setToOrtho(false, camWidth, camHeight);
		this.background.resize(width, height);
	}
	
	
	public void update(float dt) {
		this.updateInput(WORLD_STEP_TIME);
		this.updateMovingPlatforms(WORLD_STEP_TIME);
		this.player.update(WORLD_STEP_TIME, this.contactListener.currentPlayerKinematicGround());
		world.step(WORLD_STEP_TIME, 6, 2);
		this.updateCamera(dt);
	}
	
	public void updateCamera(float dt) {
		Vector2 playerCenter = this.player.body.getWorldCenter();
		Vector3 cameraCenter = this.b2dCam.position;
		
		this.b2dCam.translate(
				(playerCenter.x - cameraCenter.x) * CAMERA_LERP_FACTOR,
				(playerCenter.y - cameraCenter.y) * CAMERA_LERP_FACTOR);
		this.b2dCam.update();

		this.background.parallaxX = cameraCenter.x*0.0004f;
		this.background.parallaxY = cameraCenter.y*0.0004f;
	}
	
	public void updateMovingPlatforms(float dt) {
		for (MovingPlatform plat : this.movingPlatforms) {
			plat.update(dt);
		}
	}

	public void jumpPressed() {
		if (this.contactListener.isPlayerOnGround()) {
			this.contactListener.playerJumped();
			this.player.jump();
		}
	}

	public void updateInput(float dt) {

		boolean foundLeftOrRight = false;

		for (int i = 0; i < 4; i++) {
			if (Gdx.input.isTouched(i)) {
				float touchUV = Gdx.input.getX(i) / (float)this.sceneManager.screenWidth;
				Gdx.app.log("", "IT HAS OCCURRED__  " + touchUV);
				if (touchUV < moveLeftEnd) {
					foundLeftOrRight = true;
					this.player.setMoveDir(MoveDirection.LEFT);
				} else if (touchUV < moveRightEnd) {
					foundLeftOrRight = true;
					this.player.setMoveDir(MoveDirection.RIGHT);
				}
			}
		}

		if (!foundLeftOrRight) {
			// Move left/right or stop
			if (Gdx.input.isKeyPressed(Keys.J)) {
				this.player.setMoveDir(MoveDirection.LEFT);
			} else if (Gdx.input.isKeyPressed(Keys.L)) {
				this.player.setMoveDir(MoveDirection.RIGHT);
			} else {
				this.player.setMoveDir(MoveDirection.NONE);
			}
			// Zoom
			if (Gdx.input.isKeyPressed(Keys.EQUALS)) {
				this.b2dCam.zoom += 0.03f;
			} else if (Gdx.input.isKeyPressed(Keys.MINUS)) {
				this.b2dCam.zoom -= 0.03f;
			}
		}
	}

	@Override
	public void render() {
		
		this.background.draw();

		this.polyBatch.setProjectionMatrix(this.b2dCam.combined);
		this.polyBatch.begin();
		this.polyBatch.disableBlending();

		this.polyBatch.setShader(this.shader);
		this.shader.setUniformf("iGlobalTime", this.sceneManager.gameTime);
		this.shader.setUniform3fv("iResolution", this.sceneManager.gameSizeArray, 0, 3);
		this.shader.setUniformf("xOffset", this.b2dCam.position.x*PPM*(this.sceneManager.screenWidth/500f));
		this.shader.setUniformf("yOffset", this.b2dCam.position.y*PPM*(this.sceneManager.screenWidth/500f));
//		this.shader.setUniformf("palatteSize", palatteSize);
//		this.shader.setUniform2fv("center", this.sceneManager.gameSizeArray, 0, 2);
//
//		// sprite.draw() will call bind() on the sprite's texture.
//		// So, bind the palatte to texture #1, and then set the active texture to #0
//		// to stop sprite.draw() from binding over the palatte.
//		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
//		this.palatte.bind();
//		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
//
//		this.shader.setUniformi("palatte", 1);

		for (PolygonSprite sprite : this.groundPolySprites) {
			sprite.draw(this.polyBatch);
		}

		this.player.draw(this.polyBatch);

		for (MovingPlatform p : this.movingPlatforms) {
			p.render(this.polyBatch);
		}

		this.polyBatch.end();

//		b2dr.render(world, this.b2dCam.combined);
	}
	
	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		this.palatte.dispose();
		this.polyBatch.dispose();
		this.background.dispose();
	}

}
