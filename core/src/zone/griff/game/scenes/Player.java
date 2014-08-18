package zone.griff.game.scenes;

import static zone.griff.game.B2DVars.PPM;
import zone.griff.game.B2DVars;
import zone.griff.game.pools.Vector2Pool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;



public class Player {

	private static float PLAYER_MOVEMENT_X_VELOCITY = 8.f;
	private static float PLAYER_MOVEMENT_MULTIPLIER = 0.4f;
	private static float PLAYER_JUMP_Y_VELOCITY = 15;  
	private static float PLAYER_STOPPING_MULTIPLIER = 4f;

	public enum MoveDirection {
		NONE, RIGHT, LEFT
	}
	
	public Body body;
	public Body jumpSensor;
	
	public PolygonSprite sprite;
	
	private MoveDirection moveDir;
	public void setMoveDir(MoveDirection d) {
		this.moveDir = d;
	}
	
	public Player(World world) {
		this.setupPlayerBody(world);
		this.setupPlayerJumpSensor(world);
	}
	
	private void setupPlayerBody(World world) {
		BodyDef bdef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fdef = new FixtureDef();
		
		Vector2 playerPos = new Vector2(0, 0);
		bdef.type = BodyType.DynamicBody;
		this.body = world.createBody(bdef);
		
		shape.setAsBox(10 / PPM, 10 / PPM);
		fdef.shape = shape;
		fdef.density = 0f;
		fdef.friction = 0.2f;
		fdef.restitution = 0.0f;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		
		this.body.createFixture(fdef).setUserData("player");
		
		this.body.setAngularDamping(10); 
		
		Texture textureGround =  new Texture(Gdx.files.internal("badlogic.jpg"));
	  textureGround.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	  TextureRegion texreg = new TextureRegion(textureGround,0,0,1,1);
	  texreg.setTexture(textureGround);
		
		this.sprite = Box2DHelper.polygonSpriteForFixture(this.body.getFixtureList().get(0), texreg);
		this.sprite.setOrigin(playerPos.x, playerPos.y);
	}
	
	private void setupPlayerJumpSensor(World world) {
		BodyDef bdef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fdef = new FixtureDef();

		bdef.type = BodyType.DynamicBody;
		this.jumpSensor = world.createBody(bdef);
		
		// create foot sensor
		shape.setAsBox(6 / PPM, 6 / PPM, new Vector2(0, -8 / PPM), 0);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		fdef.isSensor = true;
		this.jumpSensor.createFixture(fdef).setUserData("foot");
	}
	
	public void jump() {
		this.body.setLinearVelocity(this.body.getLinearVelocity().x, PLAYER_JUMP_Y_VELOCITY);
	}
	
	private void move(float dt, Body currentPlayerKinematicGround) {
		Vector2 groundVelocity = Vector2Pool.obtain().set(0,0);
		if (currentPlayerKinematicGround != null) {
			groundVelocity.set(currentPlayerKinematicGround.getLinearVelocity());
		}

		if (this.moveDir == MoveDirection.NONE) {
			Vector2 playerVelocity = this.body.getLinearVelocity();
			Vector2 playerCenter = this.body.getWorldCenter();
			this.body.applyForce(
					(playerVelocity.x - groundVelocity.x) * -PLAYER_STOPPING_MULTIPLIER, 0,
					playerCenter.x, playerCenter.y,
					true);
		} else {
			Vector2 playerVelocity = this.body.getLinearVelocity();
			Vector2 playerCenter = this.body.getWorldCenter();
			float desiredVel = this.moveDir == MoveDirection.RIGHT ? PLAYER_MOVEMENT_X_VELOCITY : -PLAYER_MOVEMENT_X_VELOCITY;
			desiredVel += groundVelocity.x;
			float velChange = (desiredVel - playerVelocity.x) * PLAYER_MOVEMENT_MULTIPLIER;
			this.body.applyLinearImpulse(velChange*this.body.getMass(), 0, playerCenter.x, playerCenter.y, true);
		}
		
		Vector2Pool.release(groundVelocity);
	}
	
	
	public void update(float dt, Body currentPlayerKinematicGround) {
		this.updateJumpSensor(dt);
		this.move(dt, currentPlayerKinematicGround);
	}
	
	private void updateJumpSensor(float dt) {
		Vector2 v = Vector2Pool.obtain().set(this.body.getWorldCenter());
		Vector2 v2 = Vector2Pool.obtain(this.body.getLinearVelocity());
		v.add(v2.scl(dt));
		v.sub(this.jumpSensor.getWorldCenter());
		v.scl(1.0f/dt);
		this.jumpSensor.setLinearVelocity(v);
		Vector2Pool.release(v);
		Vector2Pool.release(v2);
	}
	
	public void draw(PolygonSpriteBatch batch) {
		Vector2 v = Vector2Pool.obtain();
		v.set(this.body.getWorldCenter());
		this.sprite.setRotation(this.body.getAngle() * MathUtils.radiansToDegrees);
		this.sprite.setPosition(v.x, v.y);
		this.sprite.draw(batch);
		Vector2Pool.release(v);
	}

}
