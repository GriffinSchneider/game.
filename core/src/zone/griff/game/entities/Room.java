package zone.griff.game.entities;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;
import zone.griff.game.SceneManager;
import zone.griff.game.entities.Floor.DoorNode;
import zone.griff.game.entities.Floor.RoomNode;
import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.util.BodyInterpolator;
import zone.griff.game.util.Box2DHelper;
import zone.griff.game.util.PaletteManager;
import zone.griff.game.util.ShaderManager;
import zone.griff.game.util.ShaderManager.Shader;
import zone.griff.game.util.SpriteAndOutline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
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
		public float minX=Integer.MAX_VALUE;
		public float minY=Integer.MAX_VALUE;
		public float maxX=Integer.MIN_VALUE;
		public float maxY=Integer.MIN_VALUE;
	}
	public CameraBounds cameraBounds;
	
	private World world;
	
	private Array<SpriteAndOutline> groundPolySprites;

	private Array<MovingPlatform> movingPlatforms;
	
	public DoorNode nodeForDoorBody(Body body) {
		return (DoorNode)body.getUserData();
	}
	
	public RoomNode roomNode;
	private Array<DoorNode> doors;
	
	public Room(RoomNode roomNode, World world) {
		this.roomNode = roomNode;
		this.world = world;
		
		this.movingPlatforms = new Array<MovingPlatform>();
		this.groundPolySprites = new Array<SpriteAndOutline>();
		this.doors = new Array<DoorNode>();
	}
	
	public void loadFromFile() {
		RubeSceneLoader loader = new RubeSceneLoader(this.world);
		RubeScene scene = loader.loadScene(this.roomNode.getFile());
		
	  TextureRegion texreg = new TextureRegion(PaletteManager.getPaletteTexture(),0,0,PaletteManager.getPaletteSize(),1);
		
		for (Body body : scene.getBodies()) {
			String type = this.getBodyType(body, scene); 
//			Gdx.app.log("", "" + type);
			if (this.isMovingPlatformType(type)) {
				this.setupMovingPlatform(body, texreg, scene);
			} else if (this.isCameraBoundsType(type)) {
				this.setupCameraBounds(body, scene);
			} else if (this.isDoorType(type)) {
				this.setupDoor(body, scene, this.roomNode);
			} else {
				this.setupGroundPlatform(body, texreg, scene);
			}
		}
	}
	
	public Body putPlayerAtDoor(Player player, DoorNode doorNode, Vector2 offsetFromDoor) {
		Vector2 doorCenter = doorNode.getBody().getWorldCenter();
		player.getBody().setTransform(doorCenter.x + offsetFromDoor.x, doorCenter.y + offsetFromDoor.y, player.getBody().getAngle());
		return this.doors.get(0).getBody();
	}

	private static final String BODY_TYPE = "type";
	private static final String BODY_TYPE_MOVING = "moving";
	private static final String BODY_TYPE_DOOR = "door";
	private static final String BODY_TYPE_CAMERA_BOUNDS = "cameraBounds";

	private String getBodyType(Body body, RubeScene scene) {
		return (String)scene.getCustom(body, BODY_TYPE);
	}

	private boolean isCameraBoundsType(String type) {
		return type != null && type.equals(BODY_TYPE_CAMERA_BOUNDS);
	}

	private boolean isDoorType(String type) {
		return type != null && type.equals(BODY_TYPE_DOOR);
	}

	private boolean isMovingPlatformType(String type) {
		return type != null && type.equals(BODY_TYPE_MOVING);
	}

	public void setupDoor(Body body, RubeScene scene, RoomNode roomNode) {
		DoorNode door = roomNode.doorNodeForBodyPosition(body.getWorldCenter());
		door.setBody(body);
		this.doors.add(door);
		body.setUserData(door);
		body.getFixtureList().get(0).setUserData("door");
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
	
	
	public void draw(PolygonSpriteBatch batch, Player player, Camera camera, SceneManager sceneManager, float interpolationAlpha) {
		Vector2 v = Vector2Pool.obtain();
		
		batch.flush();

		ShaderProgram shader = ShaderManager.get(Shader.WOBBLY_CIRCLES);
		ShaderProgram outlineShader = ShaderManager.get(Shader.OUTLINE);

		batch.setShader(shader);
		shader.setUniformf("iGlobalTime", sceneManager.gameTime);
		shader.setUniform3fv("iResolution", sceneManager.gameSizeArray, 0, 3);
		v.set(camera.position.x, camera.position.y);
		v.scl(PPM * sceneManager.screenWidth/500f);

		shader.setUniformf("xOffset", v.x);
		shader.setUniformf("yOffset", v.y);

		shader.setUniformf("palatteSize", PaletteManager.getPaletteSize());

		// sprite.draw() will call bind() on the sprite's texture.
		// So, bind the palette to texture #1, and then set the active texture to #0
		// to stop sprite.draw() from binding over the palette.
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
		PaletteManager.getPaletteTexture().bind();
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);

		shader.setUniformi("palatte", 1);
		
		for (SpriteAndOutline spriteAndOutline : this.groundPolySprites) {
			spriteAndOutline.drawSprite(batch);
		}

		batch.flush();
		this.setupShaderForBody(
				player.getInterpolatedPosition(interpolationAlpha),
				player.getInterpolatedAngle(interpolationAlpha),
				player.originalBodyWorldCenter,
				shader,
				camera,
				sceneManager);
		player.draw(batch, interpolationAlpha);
		
		batch.flush();
		for (MovingPlatform p : this.movingPlatforms) {
			this.setupShaderForBody(
					p.getInterpolatedPosition(interpolationAlpha),
					p.getInterpolatedAngle(interpolationAlpha),
					p.originalBodyWorldCenter,
					shader,
					camera,
					sceneManager);
			p.draw(batch, interpolationAlpha);
			batch.flush();
		}
		
		batch.flush();
		batch.setShader(outlineShader);
		player.drawOutline(batch);
		for (SpriteAndOutline spriteAndOutline : this.groundPolySprites) {
			spriteAndOutline.drawOutline(batch);
		}
		for (MovingPlatform p : this.movingPlatforms) {
			p.drawOutline(batch);
		}

		Vector2Pool.release(v);
	}
	
	private void setupShaderForBody(Vector2 position, float rotation, Vector2 originalWorldCenter, ShaderProgram shader, Camera camera, SceneManager sceneManager) {
		float f = PPM*sceneManager.screenWidth/500;
		shader.setUniformf("xOffset", f * (originalWorldCenter.x - position.x + camera.position.x));
		shader.setUniformf("yOffset", f * (originalWorldCenter.y - position.y + camera.position.y));
	}

	public void dispose() {
		Array<Body> bodies = new Array<Body>();
		this.world.getBodies(bodies);
		for (Body body : bodies) {
			if (!Box2DHelper.isPlayerBody(body)) {
				world.destroyBody(body);
			}
		}

		this.groundPolySprites = null;
		this.movingPlatforms = null;
	}

}
