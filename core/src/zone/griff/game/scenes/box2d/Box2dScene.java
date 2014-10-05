package zone.griff.game.scenes.box2d;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;

import java.util.HashMap;

import zone.griff.game.MyContactListener;
import zone.griff.game.SceneManager;
import zone.griff.game.entities.Player;
import zone.griff.game.entities.Player.MoveDirection;
import zone.griff.game.entities.Room;
import zone.griff.game.entities.Room.DoorNode;
import zone.griff.game.entities.Room.RoomNode;
import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.scenes.Scene;
import zone.griff.game.scenes.shader.ShaderBackground;

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

	private static float CAMERA_LERP_FACTOR = 0.1f;
	
	private static float WORLD_STEP_TIME = 1.0f/60.0f;
	
	private World world;
	private Box2DDebugRenderer b2dr;
	private OrthographicCamera b2dCam;
	
	private Room currentRoom;

	private Player player;
	
	private PolygonSpriteBatch polyBatch;
	
	private ShaderBackground background;
	
	private MyContactListener contactListener;
	
	// "buttons"
	static final float moveLeftEnd = 0.1f;
	static final float moveRightEnd = 0.3f;
	static final float jumpStart = 0.7f;
	
	
	public Box2dScene(SceneManager sceneManager) {
		super(sceneManager);

		this.world = new World(GRAVITY, true);
		
		this.b2dr = new Box2DDebugRenderer();
		
		this.b2dCam = new OrthographicCamera();

		this.polyBatch = new PolygonSpriteBatch();

		this.background = new ShaderBackground(sceneManager);
		

		this.player = new Player(this.world);
		
		this.contactListener = new MyContactListener(this.player);
		this.world.setContactListener(this.contactListener);
		
		this.setupDoorGraph();
		
		
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
	
	public void setupDoorGraph() {
		RoomNode r0 = new RoomNode(Gdx.files.internal("levels/json/room0.json"));
		RoomNode r1 = new RoomNode(Gdx.files.internal("levels/json/room1.json"));
		RoomNode r2 = new RoomNode(Gdx.files.internal("levels/json/room1.json"));
		RoomNode r3 = new RoomNode(Gdx.files.internal("levels/json/room1.json"));

		DoorNode d00 = new DoorNode(3, 1, r0);

		DoorNode d10 = new DoorNode(0, 1, r1);
		DoorNode d11 = new DoorNode(3, 1, r1);

		DoorNode d20 = new DoorNode(0, 1, r2);
		DoorNode d21 = new DoorNode(3, 1, r2);

		DoorNode d30 = new DoorNode(0, 1, r3);
		DoorNode d31 = new DoorNode(3, 1, r3);
		
		d00.link(d10);
		d11.link(d20);
		d21.link(d30);
		
		this.currentRoom = new Room(r0, world);
		this.currentRoom.loadFromFile();
	}
	
	@Override
	public void resize (int width, int height) {
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		b2dCam.setToOrtho(false, camWidth, camHeight);
		this.background.resize(width, height);
	}
	
	
	private DoorNode doorJustEntered;
	public void update(float dt) {
		this.updateInput(WORLD_STEP_TIME);
		this.currentRoom.update(WORLD_STEP_TIME);
		this.player.update(WORLD_STEP_TIME, this.contactListener.currentPlayerKinematicGround());
		world.step(WORLD_STEP_TIME, 6, 2);
		this.updateCamera(dt);
		
		Body collidedDoor = this.contactListener.collidedDoor;
		if (collidedDoor == null) {
			this.doorJustEntered = null;
		} else {

			Vector2 offset = Vector2Pool.obtain().set(this.player.body.getWorldCenter());
			offset.sub(this.contactListener.collidedDoor.getWorldCenter());
			offset.scl(-1, 1);

			DoorNode collidedNode = this.currentRoom.nodeForDoorBody(collidedDoor);
			
			if (collidedNode != this.doorJustEntered) {
				this.currentRoom.dispose();
				this.currentRoom = new Room(collidedNode.linkedNode.room, this.world);
				this.currentRoom.loadFromFile();
				this.currentRoom.putPlayerAtDoor(player, collidedNode.linkedNode, offset);
				this.doorJustEntered = collidedNode.linkedNode;
				Vector2Pool.release(offset);
			}
		}
	}
	
	public void updateCamera(float dt) {
		Vector2 playerCenter = this.player.body.getWorldCenter();
		Vector3 cameraCenter = this.b2dCam.position;
		
		cameraCenter.add(
				(playerCenter.x - cameraCenter.x) * CAMERA_LERP_FACTOR,
				(playerCenter.y - cameraCenter.y) * CAMERA_LERP_FACTOR,
				0);
		
		if (this.currentRoom.cameraBounds != null) {
			cameraCenter.x = Math.max(this.currentRoom.cameraBounds.minX + (this.b2dCam.viewportWidth/2f), cameraCenter.x);
			cameraCenter.y = Math.max(this.currentRoom.cameraBounds.minY + (this.b2dCam.viewportHeight/2f), cameraCenter.y);
			cameraCenter.x = Math.min(this.currentRoom.cameraBounds.maxX - (this.b2dCam.viewportWidth/2f), cameraCenter.x);
			cameraCenter.y = Math.min(this.currentRoom.cameraBounds.maxY - (this.b2dCam.viewportHeight/2f), cameraCenter.y);
		}

		this.b2dCam.update();

		this.background.parallaxX = cameraCenter.x*0.0004f;
		this.background.parallaxY = (cameraCenter.y + 500f)*0.0004f;
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
		this.background.draw();
		this.currentRoom.draw(this.polyBatch, this.player, this.b2dCam, this.sceneManager);
//		b2dr.render(world, this.b2dCam.combined);
	}
	
	
	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		this.polyBatch.dispose();
		this.background.dispose();
		this.currentRoom.dispose();
	}

}
