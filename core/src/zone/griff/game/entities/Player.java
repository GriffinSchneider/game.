package zone.griff.game.entities;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;
import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.scenes.box2d.B2DVars;
import zone.griff.game.util.Box2DHelper;
import zone.griff.game.util.SpriteAndOutline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
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
	public Fixture mainFixture;
	
	public Vector2 originalBodyWorldCenter;
	
	public SpriteAndOutline spriteAndOutline;
	
	private MoveDirection moveDir;
	public void setMoveDir(MoveDirection d) { this.moveDir = d; }
	public MoveDirection getMoveDir() { return this.moveDir; }
	
	public Player(World world) {
		this.setupPlayerBody(world);
		this.originalBodyWorldCenter = new Vector2(this.body.getWorldCenter());
	}
	
	private void setupPlayerBody(World world) {
		BodyDef bdef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fdef = new FixtureDef();
		
		Vector2 playerPos = new Vector2(0, 0);
		bdef.type = BodyType.DynamicBody;
		this.body = world.createBody(bdef);
		
		float middleFixtureHalfWidth = 10 / PPM;
		float middleFixtureHalfHeight = 10 / PPM;
		
		// Middle fixture
		shape.setAsBox(middleFixtureHalfWidth, middleFixtureHalfHeight);
		fdef.shape = shape;
		fdef.density = 0f;
		fdef.friction = 0.3f;
		fdef.restitution = 0.0f;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		this.mainFixture = this.body.createFixture(fdef);
		this.mainFixture.setUserData("player");

		// Jump sensor
		shape.setAsBox(6 / PPM, 6 / PPM, new Vector2(0, -8 / PPM), 0);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		fdef.isSensor = true;
		this.body.createFixture(fdef).setUserData("foot");;
		
		Texture textureGround =  new Texture(Gdx.files.internal("badlogic.jpg"));
	  textureGround.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	  TextureRegion texreg = new TextureRegion(textureGround,0,0,1,1);
	  texreg.setTexture(textureGround);
		
		this.spriteAndOutline = Box2DHelper.polygonSpriteForBody(this.body, texreg);
		this.spriteAndOutline.setOrigin(playerPos.x, playerPos.y);
		this.spriteAndOutline.setOrigin(playerPos.x, playerPos.y);
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
		this.move(dt, currentPlayerKinematicGround);
	}
	
	public void draw(PolygonSpriteBatch batch) {
		Vector2 v = Vector2Pool.obtain();
		v.set(this.body.getWorldCenter());
		this.spriteAndOutline.setRotation(this.body.getAngle() * MathUtils.radiansToDegrees);
		this.spriteAndOutline.setPosition(v.x, v.y);
		this.spriteAndOutline.drawSprite(batch);
		Vector2Pool.release(v);
	}

	public void drawOutline(PolygonSpriteBatch batch) {
		this.spriteAndOutline.drawOutline(batch);
	}

}
