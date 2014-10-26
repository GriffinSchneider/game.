package zone.griff.game.scenes.box2d;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;
import zone.griff.game.MyContactListener;
import zone.griff.game.SceneManager;
import zone.griff.game.entities.Floor;
import zone.griff.game.entities.Floor.DoorNode;
import zone.griff.game.entities.Player;
import zone.griff.game.entities.Player.MoveDirection;
import zone.griff.game.entities.Room;
import zone.griff.game.scenes.Scene;
import zone.griff.game.util.PaletteManager;
import backgrounds.GeometricBackground;
import backgrounds.ParallaxBackground;
import backgrounds.ShaderBackground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
 
public class Box2dScene extends Scene {
	
	// Tuning
	private static Vector2 GRAVITY = new Vector2(0, -50);

	private static float CAMERA_LERP_FACTOR = 4.0f;
	
	private static float WORLD_STEP_TIME = 1.0f/60.0f;
	
	private World world;
	private Box2DDebugRenderer b2dr;
	private OrthographicCamera b2dCam;
	
	private Room currentRoom;

	private Floor currentFloor;

	private Player player;
	
	
	private ParallaxBackground background;
	
	private MyContactListener contactListener;
	
	// "buttons"
	static final float moveLeftEnd = 0.1f;
	static final float moveRightEnd = 0.3f;
	static final float jumpStart = 0.9f;
	static final float dashStart = 0.7f;
	
	
	public Box2dScene(SceneManager sceneManager) {
		super(sceneManager);

		this.world = new World(GRAVITY, true);
		
		this.b2dr = new Box2DDebugRenderer();
		
		this.b2dCam = new OrthographicCamera();

//		this.background = new ShaderBackground(sceneManager);
		this.background = new GeometricBackground(sceneManager);

		this.player = new Player(this.world);
		
		this.contactListener = new MyContactListener(this.player);
		this.world.setContactListener(this.contactListener);

		this.currentFloor = new Floor();
		this.currentFloor.generate();
		this.currentRoom = new Room(this.currentFloor.firstRoom, world);
		this.currentRoom.loadFromFile();


		final Box2dScene t = this;
		Gdx.input.setInputProcessor(new InputProcessor() {
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) { 
				if (screenX/(float)t.sceneManager.screenWidth > jumpStart) {
					t.jumpPressed();
				} else if (screenX/(float)t.sceneManager.screenWidth > dashStart) {
					t.dashPressed();
				}
				return false;
			}
			@Override
			public boolean scrolled(int amount) { return false; }
			@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
			@Override
			public boolean keyUp(int keycode) { return false; }
			@Override
			public boolean keyTyped(char character) { return false; }
			@Override
			public boolean keyDown(int keycode) {
				if (keycode == Keys.SPACE) {
					t.jumpPressed();
				} else if (keycode == Keys.ENTER) {
					t.dashPressed();
				}
				return false;
			}
		});
	}

	@Override
	public void resize (int width, int height) {
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		b2dCam.setToOrtho(false, camWidth, camHeight);
		this.background.resizeCamera(camWidth, camHeight);
	}


	private DoorNode doorJustEntered;
	private float accumulator;
	@Override
	public void update(float dt) {
		accumulator += dt;
		
		// If the time to simulate is less than 120% of the target step time,
		// then just simulate the whole thing.
		// Otherwise, go in steps of the target step time.
		float stepTime = dt;
		stepTime = Math.max(WORLD_STEP_TIME * .8f, stepTime);
		stepTime = Math.min(WORLD_STEP_TIME * 1.2f, stepTime);
		
		while (accumulator >= stepTime) {
			world.step(stepTime, 6, 2);
			this.updateInput(stepTime);
			this.currentRoom.update(stepTime);
			this.player.update(stepTime, this.contactListener.currentPlayerKinematicGround());
			this.updateCamera(stepTime);
			accumulator -= stepTime;
		}
		
		Body collidedDoor = this.contactListener.collidedDoor;
		if (collidedDoor == null) {
			this.doorJustEntered = null;
		} else {

			Vector2 offset = this.player.body.getWorldCenter();
			offset.sub(this.contactListener.collidedDoor.getWorldCenter());
			offset.scl(-1, 1);

			DoorNode collidedNode = this.currentRoom.nodeForDoorBody(collidedDoor);
			
			if (collidedNode != this.doorJustEntered) {
				DoorNode linkedNode = collidedNode.getLinkedNode();
				this.currentRoom.dispose();
				this.currentRoom = new Room(linkedNode.getRoom(), this.world);
				this.currentRoom.loadFromFile();
				this.currentRoom.putPlayerAtDoor(player, linkedNode, offset);
				this.doorJustEntered = linkedNode;
			}
		}
	}
	
	public void updateCamera(float dt) {
		Vector2 playerCenter = this.player.body.getWorldCenter();
		Vector3 cameraCenter = this.b2dCam.position;
		
		cameraCenter.add(
				(playerCenter.x - cameraCenter.x) * (CAMERA_LERP_FACTOR * dt),
				(playerCenter.y - cameraCenter.y) * (CAMERA_LERP_FACTOR * dt),
				0);
		
		if (this.currentRoom.cameraBounds != null) {
			cameraCenter.x = Math.max(this.currentRoom.cameraBounds.minX + (this.b2dCam.viewportWidth/2f), cameraCenter.x);
			cameraCenter.y = Math.max(this.currentRoom.cameraBounds.minY + (this.b2dCam.viewportHeight/2f), cameraCenter.y);
			cameraCenter.x = Math.min(this.currentRoom.cameraBounds.maxX - (this.b2dCam.viewportWidth/2f), cameraCenter.x);
			cameraCenter.y = Math.min(this.currentRoom.cameraBounds.maxY - (this.b2dCam.viewportHeight/2f), cameraCenter.y);
		}

		this.b2dCam.update();
	}
	
	public void jumpPressed() {
		if (this.contactListener.isPlayerOnGround()) {
			this.contactListener.playerJumped();
			this.player.jump();
		}
	}
	
	public void dashPressed() {
		this.player.dash();
	}

	public void updateInput(float dt) {

		boolean foundLeftOrRight = false;

		for (int i = 0; i < 4; i++) {
			if (Gdx.input.isTouched(i)) {
				float touchUV = Gdx.input.getX(i) / (float)this.sceneManager.screenWidth;
				if (touchUV < moveLeftEnd) {
					foundLeftOrRight = true;
					this.player.setMoveDir(MoveDirection.LEFT);
				} else if (touchUV < moveRightEnd) {
					foundLeftOrRight = true;
					this.player.setMoveDir(MoveDirection.RIGHT);
				}
			}
		}

		if (!foundLeftOrRight) {
			// Move left/right or stop
			if (Gdx.input.isKeyPressed(Keys.J)) {
				this.player.setMoveDir(MoveDirection.LEFT);
			} else if (Gdx.input.isKeyPressed(Keys.L)) {
				this.player.setMoveDir(MoveDirection.RIGHT);
			} else {
				this.player.setMoveDir(MoveDirection.NONE);
			}
			// Zoom
			if (Gdx.input.isKeyPressed(Keys.EQUALS)) {
				this.b2dCam.zoom += 0.03f;
			} else if (Gdx.input.isKeyPressed(Keys.MINUS)) {
				this.b2dCam.zoom -= 0.03f;
			}
		}
	}

	@Override
	public void render() {
		this.background.parallaxX = this.b2dCam.position.x*0.0004f;
		this.background.parallaxY = (this.b2dCam.position.y + 500f)*0.0004f;

		this.spriteBatch.begin();

		this.spriteBatch.setProjectionMatrix(this.b2dCam.combined);
		this.spriteBatch.disableBlending();
		this.background.draw(this.spriteBatch, this.b2dCam);
		this.currentRoom.draw(this.spriteBatch, this.player, this.b2dCam, this.sceneManager);
		this.spriteBatch.end();
//		b2dr.render(world, this.b2dCam.combined);
	}
	
	
	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		this.background.dispose();
		this.currentRoom.dispose();
		this.player.dispose();
	}

}
