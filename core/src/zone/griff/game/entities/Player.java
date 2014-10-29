package zone.griff.game.entities;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;
import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.scenes.box2d.B2DVars;
import zone.griff.game.util.BodyInterpolator;
import zone.griff.game.util.Box2DHelper;
import zone.griff.game.util.PaletteManager;
import zone.griff.game.util.SpriteAndOutline;

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
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

	private static float PLAYER_DASH_X_VELOCITY = 18.0f;
	private static float PLAYER_DASH_TIME = 0.22f;

	public enum MoveDirection {
		NONE, RIGHT, LEFT
	}
	
	private BodyInterpolator interp;
	public void getInterpolatedPosition(Vector2 v, float interpolationAlpha) {
		interp.getInterpolatedPosition(v, interpolationAlpha);
	}

	private Body body;
	public Body getBody() {
		return this.body;
	}

	public Fixture mainFixture;
	
	public Vector2 originalBodyWorldCenter;
	
	public SpriteAndOutline spriteAndOutline;
	
	private MoveDirection moveDir;
	private MoveDirection lastMovingDir;
	public void setMoveDir(MoveDirection d) { 
		if (this.isDashing) {
			return;
		}
		this.moveDir = d; 
		if (d != MoveDirection.NONE) {
			this.lastMovingDir = d;
		}
	}
	public MoveDirection getMoveDir() { return this.moveDir; }
	
	private float dashTimer;
	private boolean isDashing;
	
	public Player(World world) {
		this.setupPlayerBody(world);
		this.originalBodyWorldCenter = new Vector2(this.body.getWorldCenter());
	}
	
	private void setupPlayerBody(World world) {
		BodyDef bdef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fdef = new FixtureDef();
		
		Vector2 playerPos = new Vector2(8, 8);
		bdef.type = BodyType.DynamicBody;
		this.body = world.createBody(bdef);
		body.setUserData("player");
		
		this.interp = new BodyInterpolator(body);
		
		float w = 10 / PPM;
		float h = 10 / PPM;

		float corner = 2f / PPM;

		Vector2[] verts = new Vector2[8];
		
		// Bottom Left
		verts[0] = new Vector2(-w, -h + corner);
		verts[1] = new Vector2(-w + corner, -h);

		// Bottom Right
		verts[2] = new Vector2(w - corner, -h);
		verts[3] = new Vector2(w, -h + corner);

		// Top Right
		verts[4] = new Vector2(w, h - corner);
		verts[5] = new Vector2(w - corner, h);

		// Top Left
		verts[6] = new Vector2(-w + corner, h);
		verts[7] = new Vector2(-w, h - corner);

		shape.set(verts);
		
		fdef.shape = shape;
		fdef.density = 0f;
		fdef.friction = 0.3f;
		fdef.restitution = 0.0f;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		this.mainFixture = body.createFixture(fdef);
		this.mainFixture.setUserData("player");

		// Jump sensor
		shape.setAsBox(6 / PPM, 6 / PPM, new Vector2(0, -8 / PPM), 0);
		fdef.density = 0f;
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		fdef.isSensor = true;
		body.createFixture(fdef).setUserData("foot");

		this.spriteAndOutline = Box2DHelper.polygonSpriteForBody(body, PaletteManager.getPaletteTextureRegion());
		this.spriteAndOutline.setOrigin(0, 0);
		
		body.setTransform(new Vector2(playerPos.x, playerPos.y), 0);
	}
	
	public void jump() {
		this.body.setLinearVelocity(this.body.getLinearVelocity().x, PLAYER_JUMP_Y_VELOCITY);
	}
	
	public void dash() {
		if (!this.isDashing) {
			this.dashTimer = 0;
			this.isDashing = true;
		}
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

			// Only apply the movement velocity if it's going to make the player's velocity
			// closer to the desired velocity
			if (velChange * desiredVel > 0) {
				this.body.applyLinearImpulse(velChange*this.body.getMass(), 0, playerCenter.x, playerCenter.y, true);
			} else {
				this.body.applyForce(
						(playerVelocity.x - desiredVel) * -PLAYER_STOPPING_MULTIPLIER, 0,
						playerCenter.x, playerCenter.y,
						true);
			}
		}
		
		Vector2Pool.release(groundVelocity);
	}
	
	public void update(float dt, Body currentPlayerKinematicGround) {
		this.interp.update(dt);
		if (this.isDashing) {
			this.dashTimer += dt;
			this.body.setLinearVelocity(PLAYER_DASH_X_VELOCITY * (this.lastMovingDir == MoveDirection.RIGHT ? 1 : -1), 0);
			this.body.applyForceToCenter(0, 50, true);
			if (dashTimer >= PLAYER_DASH_TIME) {
				this.isDashing = false;
			}
		} else {
			this.move(dt, currentPlayerKinematicGround);
		}
	}
	
	
	public void draw(PolygonSpriteBatch batch, float interpolationAlpha) {
		Vector2 currentPosition = Vector2Pool.obtain();
		this.interp.getInterpolatedPosition(currentPosition, interpolationAlpha);
		this.spriteAndOutline.setRotation(this.interp.getInterpolatedAngle(interpolationAlpha) * MathUtils.radiansToDegrees);
		this.spriteAndOutline.setPosition(currentPosition.x, currentPosition.y);
		this.spriteAndOutline.drawSprite(batch);
		Vector2Pool.release(currentPosition);
	}

	public void drawOutline(PolygonSpriteBatch batch) {
		this.spriteAndOutline.drawOutline(batch);
	}

	public void dispose() {
		this.interp.dispose();
		this.interp = null;
	}
}
