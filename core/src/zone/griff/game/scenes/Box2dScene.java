package zone.griff.game.scenes;

import static zone.griff.game.B2DVars.PPM;
import zone.griff.game.B2DVars;
import zone.griff.game.MyContactListener;
import zone.griff.game.SceneManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Box2dScene extends Scene {
	
	// Tuning
	private static Vector2 GRAVITY = new Vector2(0, -14);

	private static float CAMERA_LERP_FACTOR = 0.2f;
	
	private static float PLAYER_MAX_X_VELOCITY = 2f;
	private static float PLAYER_STOPPING_MULTIPLIER = 10f;
	private static float PLAYER_JUMP_Y_VELOCITY = 4;
	
	
	private World world;
	private MyContactListener cl;
	private Box2DDebugRenderer b2dr;
	private Body playerBody;
	private OrthographicCamera b2dCam;
	
	public Box2dScene(SceneManager sceneManager) {
		super(sceneManager);

		world = new World(GRAVITY, true);
		
		cl = new MyContactListener();
		world.setContactListener(cl);
		
		b2dr = new Box2DDebugRenderer();
		
		// create platform
		BodyDef bdef = new BodyDef();
		bdef.position.set(160 / PPM, 120 / PPM);
		bdef.type = BodyType.StaticBody;
		Body body = world.createBody(bdef);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(50 / PPM, 5 / PPM);
		FixtureDef fdef = new FixtureDef();
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_GROUND;
		fdef.filter.maskBits = B2DVars.BIT_PLAYER;
		body.createFixture(fdef).setUserData("ground");
		
		// create player
		bdef.position.set(160 / PPM, 200 / PPM);
		bdef.type = BodyType.DynamicBody;
		this.playerBody = world.createBody(bdef);
		
		shape.setAsBox(5 / PPM, 5 / PPM);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		this.playerBody.createFixture(fdef).setUserData("player");
		
		// create foot sensor
		shape.setAsBox(2 / PPM, 2 / PPM, new Vector2(0, -5 / PPM), 0);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		fdef.isSensor = true;
		this.playerBody.createFixture(fdef).setUserData("foot");
		
		// set up box2d cam
		this.b2dCam = new OrthographicCamera();
	}
	 
	@Override
	public void resize (int width, int height) {
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		b2dCam.setToOrtho(false, camWidth, camHeight);
	}
	
	public void update(float dt) {
		this.updateInput(dt);
		world.step(dt, 6, 2);
	}

	public void updateInput(float dt) {
		// Jump
		if (Gdx.input.isKeyPressed(Keys.I) ||
				Gdx.input.isKeyPressed(Keys.SPACE)) {
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
	}

	public void movePlayer(boolean right, float dt) {
		Vector2 playerVelocity = this.playerBody.getLinearVelocity();
		Vector2 playerCenter = this.playerBody.getWorldCenter();
		float desiredVel = right ? PLAYER_MAX_X_VELOCITY : -PLAYER_MAX_X_VELOCITY;
		float velChange = desiredVel - playerVelocity.x;
		this.playerBody.applyLinearImpulse(velChange, 0, playerCenter.x, playerCenter.y, true);
		// this.playerBody.applyLinearImpulse(movement, this.playerBody.getWorldCenter(), true);
	}

	public void stopPlayer(float dt) {
		Vector2 playerVelocity = this.playerBody.getLinearVelocity();
		Vector2 playerCenter = this.playerBody.getWorldCenter();
		this.playerBody.applyForce(playerVelocity.x * -PLAYER_STOPPING_MULTIPLIER,
															 0, playerCenter.x, playerCenter.y, true);
	}
	
	@Override
	public void render() {

		Vector2 playerCenter = this.playerBody.getWorldCenter();
		Vector3 cameraCenter = this.b2dCam.position;
		
		cameraCenter.x += (playerCenter.x - cameraCenter.x) * CAMERA_LERP_FACTOR;
		cameraCenter.y += (playerCenter.y - cameraCenter.y) * CAMERA_LERP_FACTOR;

		this.b2dCam.update();
		
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
