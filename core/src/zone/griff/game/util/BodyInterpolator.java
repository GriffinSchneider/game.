package zone.griff.game.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class BodyInterpolator {
	
	public Body body;
	
	private Vector2 lastPosition = new Vector2();
	private Vector2 currentPosition = new Vector2();

	private float lastAngle;
	private float currentAngle;

	public BodyInterpolator(Body body) {
		this.body = body;
	}
	
	public void update(float dt) {
		this.lastPosition.set(this.currentPosition);
		this.currentPosition.set(this.body.getWorldCenter());

		lastAngle = currentAngle;
		currentAngle = this.body.getAngle();
	}
	
	private Vector2 interpolatedPosition = new Vector2();
	public Vector2 getInterpolatedPosition(float interpolationAlpha) {
		interpolatedPosition.set(currentPosition);
		interpolatedPosition.scl(interpolationAlpha);
		interpolatedPosition.add(
				this.lastPosition.x * (1.0f - interpolationAlpha),
				this.lastPosition.y * (1.0f - interpolationAlpha));
		return interpolatedPosition;
	}
	
	public float getInterpolatedAngle(float interpolationAlpha) {
		return (this.currentAngle * interpolationAlpha) + (this.lastAngle * (1.0f - interpolationAlpha));
	}
	
	public void dispose() {
		this.body.getWorld().destroyBody(this.body);
		this.body = null;
	}

}
