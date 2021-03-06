package zone.griff.game;

import zone.griff.game.entities.Player;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class MyContactListener implements ContactListener {
	
	private int jumpsPerContact = 2;
	
	private int groundSensorCount;
	private int remainingJumpCount;

	private Body currentPlayerKinematicGround;
	public Body currentPlayerKinematicGround() { return currentPlayerKinematicGround; }

	private Player player;
	
	// TODO: refactor.
	public Body collidedDoor;
	
	public MyContactListener(Player player) {
		this.player = player;
	}
	
	private boolean isFoot(Fixture fixture) {
		return fixture.getUserData() != null &&
				fixture.getUserData().equals("foot");
	}
	private boolean isPlayer(Fixture fixture) {
		return fixture.getUserData() != null &&
				((String)fixture.getUserData()).startsWith("player");
	}
	private boolean isDoor(Fixture fixture) {
		return fixture.getUserData() != null &&
				((String)fixture.getUserData()).equals("door");
	}
	
	// called when two fixtures start to collide
	public void beginContact(Contact c) {
		
		Fixture fa = c.getFixtureA();
		Fixture fb = c.getFixtureB();
		
		if(isFoot(fa) && !fb.isSensor()) {
			groundSensorCount++;
			if (fb.getBody().getType() == BodyType.KinematicBody) {
				currentPlayerKinematicGround = fb.getBody();
			}
		} else if(isFoot(fb) && !fa.isSensor()) {
			groundSensorCount++;
			if (fa.getBody().getType() == BodyType.KinematicBody) {
				currentPlayerKinematicGround = fa.getBody();
			}

		} else if (isPlayer(fa) && isDoor(fb)) {
			this.collidedDoor = fb.getBody();
		} else if (isPlayer(fb) && isDoor(fa)) {
			this.collidedDoor = fa.getBody();
		}
		
		if (groundSensorCount > 0) {
			remainingJumpCount = jumpsPerContact;
		}
	} 
	
	// called when two fixtures no longer collide
	public void endContact(Contact c) {
		
		Fixture fa = c.getFixtureA();
		Fixture fb = c.getFixtureB();
		
		if(isFoot(fa) && !fb.isSensor()) {
			groundSensorCount--;
			if (currentPlayerKinematicGround == fb.getBody()) {
				currentPlayerKinematicGround = null;
			}
		} else if(isFoot(fb) && !fa.isSensor()) {
			groundSensorCount--;
			if (currentPlayerKinematicGround == fa.getBody()) {
				currentPlayerKinematicGround = null;
			}

		} else if (isPlayer(fa) && isDoor(fb)) {
			this.collidedDoor = null;
		} else if (isPlayer(fb) && isDoor(fa)) {
			this.collidedDoor = null;
		}
	}
	
	
	public void preSolve(Contact c, Manifold m) {
		Fixture fa = c.getFixtureA();
		Fixture fb = c.getFixtureB();

		if(isPlayer(fa) || isPlayer(fb)) {
			if (this.player.getMoveDir() != Player.MoveDirection.NONE) {
				c.setFriction(0.0f);
			} else {
				c.setFriction(this.player.mainFixture.getFriction());
			}
		}
	}
	
	public void postSolve(Contact c, ContactImpulse ci) {}
	
	public boolean isPlayerOnGround() { 
		return groundSensorCount > 0 || remainingJumpCount > 0; 
	}
	
	public void playerJumped() {
		remainingJumpCount--;
	}
	
}
