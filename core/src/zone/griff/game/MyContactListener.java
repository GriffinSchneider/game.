package zone.griff.game;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class MyContactListener implements ContactListener {
	
	private int groundSensorCount;
	private int remainingJumpCount;
	private int jumpsPerContact = 2;
	
	// called when two fixtures start to collide
	public void beginContact(Contact c) {
		
		Fixture fa = c.getFixtureA();
		Fixture fb = c.getFixtureB();
		
		if(fa.getUserData() != null && fa.getUserData().equals("foot")) {
			groundSensorCount++;
		} else if(fb.getUserData() != null && fb.getUserData().equals("foot")) {
			groundSensorCount++;
		}
		if (groundSensorCount > 0) {
			remainingJumpCount = jumpsPerContact;
		}
	}
	
	// called when two fixtures no longer collide
	public void endContact(Contact c) {
		
		Fixture fa = c.getFixtureA();
		Fixture fb = c.getFixtureB();
		
		if(fa.getUserData() != null && fa.getUserData().equals("foot")) {
			groundSensorCount--;
		} else if(fb.getUserData() != null && fb.getUserData().equals("foot")) {
			groundSensorCount--;
		}
	}
	
	public boolean isPlayerOnGround() { 
		return groundSensorCount > 0 || remainingJumpCount > 0; 
	}
	
	public void playerJumped() {
		remainingJumpCount--;
	}
	
	
	
	public void preSolve(Contact c, Manifold m) {}
	public void postSolve(Contact c, ContactImpulse ci) {}
	
}
