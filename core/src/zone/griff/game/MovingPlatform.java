package zone.griff.game;

import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.scenes.Box2DHelper.SpriteAndOutline;

import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class MovingPlatform {
	
	public static float MOVING_PALATFORM_VELOCITY = 3f;
	
	public Body platformBody;

	private Array<Vector2> targetBodies;
	private int currentTargetBodyIndex;
	
	private SpriteAndOutline platformSprite;
	
	public Vector2 originalBodyWorldCenter;
	private Vector2 originalSpritePosition;
	
	private float previousXDistanceToNextTarget;
	private float previousYDistanceToNextTarget;

	public MovingPlatform(Body platform, Array<Vector2> targets, SpriteAndOutline sprite) {
		this.platformBody = platform;
		this.targetBodies = targets;
		this.currentTargetBodyIndex = 0;
		this.platformSprite = sprite;
		
		this.originalBodyWorldCenter = new Vector2(this.platformBody.getWorldCenter());
		this.originalSpritePosition = new Vector2( 
				this.platformSprite.sprite.getX(),
				this.platformSprite.sprite.getY());
	}
	
	public void update(float dt) {
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
	
	public void render(PolygonSpriteBatch batch) {
		Vector2 v = Vector2Pool.obtain();
		
		v.set(this.platformBody.getWorldCenter());
		v.sub(this.originalBodyWorldCenter);
		
		v.add(originalSpritePosition);
		
		this.platformSprite.setRotation(this.platformBody.getAngle() * MathUtils.radiansToDegrees);
		this.platformSprite.setPosition(v.x, v.y);
		this.platformSprite.sprite.draw(batch);

		Vector2Pool.release(v);
	}

	public void renderOutline(PolygonSpriteBatch batch) {
		this.platformSprite.outline.draw(batch);
	}

}
