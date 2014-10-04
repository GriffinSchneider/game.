package zone.griff.game.entities;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;
import zone.griff.game.SceneManager;
import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.util.Box2DHelper;
import zone.griff.game.util.SpriteAndOutline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.RubeSceneLoader;

public class Room {
	
	public class CameraBounds {
		public float minX=0;
		public float minY=0;
		public float maxX=0;
		public float maxY=0;
	}
	public CameraBounds cameraBounds;
	
	private FileHandle roomFile;
	private World world;
	
	private ShaderProgram shader;
	private Texture palatte;
	private int palatteSize;

	private Array<SpriteAndOutline> groundPolySprites;
	private Array<MovingPlatform> movingPlatforms;
	
	public Room(FileHandle roomFile, World world) {
		this.roomFile = roomFile;
		this.world = world;
		
		this.movingPlatforms = new Array<MovingPlatform>();
		this.groundPolySprites = new Array<SpriteAndOutline>();
		
		this.setupShader();

	}
	
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

	
	
	public void loadFromFile() {
		RubeSceneLoader loader = new RubeSceneLoader(world);
		RubeScene scene = loader.loadScene(this.roomFile);
		
		Texture textureGround =  new Texture(Gdx.files.internal("badlogic.jpg"));
	  textureGround.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	  TextureRegion texreg = new TextureRegion(textureGround,0,0,1,1);
	  texreg.setTexture(textureGround);
		
		for (Body body : scene.getBodies()) {
			String type = this.getBodyType(body, scene); 
			Gdx.app.log("", "" + type);
			if (this.isMovingPlatformType(type)) {
				this.setupMovingPlatform(body, texreg, scene);
			} else if (this.isCameraBoundsType(type)) {
				this.setupCameraBounds(body, scene);
			} else if (this.isDoorType(type)) {
				body.getFixtureList().get(0).setUserData("door");
			} else {
				this.setupGroundPlatform(body, texreg, scene);
			}
		}
	}
	


	private static final String BODY_TYPE = "type";
	private static final String BODY_TYPE_MOVING = "moving";
	private static final String BODY_TYPE_DOOR = "door";
	private static final String BODY_TYPE_CAMERA_BOUNDS = "cameraBounds";

	public String getBodyType(Body body, RubeScene scene) {
		return (String)scene.getCustom(body, BODY_TYPE);
	}

	public boolean isCameraBoundsType(String type) {
		return type != null && type.equals(BODY_TYPE_CAMERA_BOUNDS);
	}

	public boolean isDoorType(String type) {
		return type != null && type.equals(BODY_TYPE_DOOR);
	}

	public boolean isMovingPlatformType(String type) {
		return type != null && type.equals(BODY_TYPE_MOVING);
	}

	public void setupCameraBounds(Body body, RubeScene scene) {
		this.cameraBounds = new CameraBounds();
		Transform t = body.getTransform();
		for (Fixture f : new Array<Fixture>(body.getFixtureList())) {
			Shape shape = f.getShape();
			Vector2 p = new Vector2().set(((CircleShape) shape).getPosition());
			t.mul(p);
		  this.cameraBounds.minX = Math.min(p.x, this.cameraBounds.minX);
			this.cameraBounds.minY = Math.min(p.y, this.cameraBounds.minY);
			this.cameraBounds.maxX = Math.max(p.x, this.cameraBounds.maxX);
			this.cameraBounds.maxY = Math.max(p.y, this.cameraBounds.maxY);
		}
	}
	
	public void setupGroundPlatform(Body body, TextureRegion texreg, RubeScene scene) {
		this.groundPolySprites.add(Box2DHelper.polygonSpriteForBody(body, texreg));
	}
	
	
	public void setupMovingPlatform(Body moving, TextureRegion texreg, RubeScene scene) {

		Array<Vector2> targetPoints = new Array<Vector2>();
		Transform t = moving.getTransform();

		Body platformBody = null;

		for (Fixture f : new Array<Fixture>(moving.getFixtureList())) {
			if (f.isSensor()) {
				CircleShape shape = (CircleShape)f.getShape();
				Vector2 p = new Vector2().set(shape.getPosition());
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
				platformBody.createFixture(fdef);

				moving.destroyFixture(f);
			}
		}

		this.movingPlatforms.add(new MovingPlatform(
				platformBody, 
				targetPoints,
				Box2DHelper.polygonSpriteForBody(platformBody, texreg)));
	}
	
	
	public void update(float dt) {
		for (MovingPlatform plat : this.movingPlatforms) {
			plat.update(dt);
		}
	}
	
	
	public void draw(PolygonSpriteBatch batch, Player player, Camera camera, SceneManager sceneManager) {
		Vector2 v = Vector2Pool.obtain();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();

		batch.setShader(shader);
		shader.setUniformf("iGlobalTime", sceneManager.gameTime);
		shader.setUniform3fv("iResolution", sceneManager.gameSizeArray, 0, 3);
		v.set(camera.position.x, camera.position.y);
		v.scl(PPM * sceneManager.screenWidth/500f);

		shader.setUniformf("xOffset", v.x);
		shader.setUniformf("yOffset", v.y);

		shader.setUniformf("palatteSize", palatteSize);

		// sprite.draw() will call bind() on the sprite's texture.
		// So, bind the palatte to texture #1, and then set the active texture to #0
		// to stop sprite.draw() from binding over the palatte.
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
		this.palatte.bind();
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);

		shader.setUniformi("palatte", 1);
		
		for (SpriteAndOutline spriteAndOutline : this.groundPolySprites) {
			spriteAndOutline.drawSprite(batch);
		}

		batch.flush();
		this.setupShaderForBody(v, player.body, player.originalBodyWorldCenter, shader, camera, sceneManager);
		player.draw(batch);
		
		batch.flush();
		for (MovingPlatform p : this.movingPlatforms) {
			this.setupShaderForBody(v, p.platformBody, p.originalBodyWorldCenter, shader, camera, sceneManager);
			p.render(batch);
			batch.flush();
		}
		
		batch.flush();
		batch.setShader(null);
		player.drawOutline(batch);
		for (SpriteAndOutline spriteAndOutline : this.groundPolySprites) {
			spriteAndOutline.drawOutline(batch);
		}
		for (MovingPlatform p : this.movingPlatforms) {
			p.renderOutline(batch);
		}

		batch.end();

		Vector2Pool.release(v);

	}
	
	private void setupShaderForBody(Vector2 tempVector, Body body, Vector2 originalWorldCenter, ShaderProgram shader, Camera camera, SceneManager sceneManager) {
		tempVector.set(originalWorldCenter);
		tempVector.sub(body.getWorldCenter());
		tempVector.add(camera.position.x, camera.position.y);
		tempVector.scl(PPM*sceneManager.screenWidth/500);
		shader.setUniformf("xOffset", tempVector.x);
		shader.setUniformf("yOffset", tempVector.y);
	}

	public void dispose() {
		this.palatte.dispose();
	}

}
