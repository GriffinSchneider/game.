package zone.griff.game.entities;

import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.util.BodyInterpolator;
import zone.griff.game.util.SpriteAndOutline;

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class MovingPlatform {
	
	public static float MOVING_PALATFORM_VELOCITY = 3f;
	
	private Body platformBody;
	public Body getBody() {
		return this.platformBody;
	}

	private BodyInterpolator interp;
	public Vector2 getInterpolatedPosition(float interpolationAlpha) {
		return interp.getInterpolatedPosition(interpolationAlpha);
	}
	public float getInterpolatedAngle(float interpolationAlpha) {
		return interp.getInterpolatedAngle(interpolationAlpha);
	}

	private Array<Vector2> targetBodies;
	private int currentTargetBodyIndex;
	
	private SpriteAndOutline platformSprite;
	
	public Vector2 originalBodyWorldCenter;
	private Vector2 originalSpritePosition;
	
	private float previousXDistanceToNextTarget;
	private float previousYDistanceToNextTarget;

	public MovingPlatform(Body platform, Array<Vector2> targets, SpriteAndOutline sprite) {
		this.platformBody = platform;
		this.interp = new BodyInterpolator(platform);
		this.targetBodies = targets;
		this.currentTargetBodyIndex = 0;
		this.platformSprite = sprite;
		
		this.originalBodyWorldCenter = new Vector2(this.platformBody.getWorldCenter());
		this.originalSpritePosition = new Vector2( 
				this.platformSprite.getSprites().get(0).getX(),
				this.platformSprite.getSprites().get(0).getY());
	}
	
	public void update(float dt) {
		this.interp.update(dt);
		Vector2 v = Vector2Pool.obtain();
		
		Vector2 currentTargetPoint = this.targetBodies.get(this.currentTargetBodyIndex);
		v.set(currentTargetPoint).sub(this.platformBody.getWorldCenter());
		
		boolean didXReverse = (this.previousXDistanceToNextTarget > 0) != (v.x > 0);
		boolean didYReverse = (this.previousYDistanceToNextTarget > 0) != (v.y > 0);

		if (didXReverse || didYReverse) {
			this.currentTargetBodyIndex++;
			this.currentTargetBodyIndex %= this.targetBodies.size;

			currentTargetPoint = this.targetBodies.get(this.currentTargetBodyIndex);
			v.set(currentTargetPoint).sub(this.platformBody.getWorldCenter());
		}
		
		this.previousXDistanceToNextTarget = v.x;
		this.previousYDistanceToNextTarget = v.y;
		
		v.nor();
		v.scl(MOVING_PALATFORM_VELOCITY);
		this.platformBody.setLinearVelocity(v);
		Vector2Pool.release(v);
	}
	
	public void draw(PolygonSpriteBatch batch, float interpolationAlpha) {
		Vector2 v = this.interp.getInterpolatedPosition(interpolationAlpha);
		v.sub(this.originalBodyWorldCenter);
		
		v.add(originalSpritePosition);
		
		this.platformSprite.setRotation(this.interp.getInterpolatedAngle(interpolationAlpha) * MathUtils.radiansToDegrees);
		this.platformSprite.setPosition(v.x, v.y);
		this.platformSprite.drawSprite(batch);
	}

	public void drawOutline(PolygonSpriteBatch batch) {
		this.platformSprite.drawOutline(batch);
	}
	
}
