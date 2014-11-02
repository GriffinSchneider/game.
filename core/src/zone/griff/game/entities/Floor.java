package zone.griff.game.entities;

import zone.griff.game.util.Box2DHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class Floor {
	
	public static class RoomNode {
		private Array<DoorNode> doors;
		
		private RoomNode(FileHandle file) {
			this.file = file;
			doors = new Array<DoorNode>();
		}
		
		private FileHandle file;
		public FileHandle getFile() {
			return file;
		}
		
		public DoorNode doorNodeForBodyPosition(Vector2 bodyPos) {
			int gridX = Box2DHelper.gridifyX(bodyPos);
			int gridY = Box2DHelper.gridifyY(bodyPos);
			for (DoorNode door : this.doors) {
				if (door.gridX == gridX && door.gridY == gridY) {
					return door;
				}
			}
			throw new IllegalArgumentException("Couldn't find a door at " + gridX + ", " + gridY);
		}

		private void addDoor(DoorNode door) {
			this.doors.add(door);
			door.room = this;
		}
	}
	
	public static class DoorNode {
		private int gridX;
		private int gridY;

		private RoomNode room;
		public RoomNode getRoom() {
			return room;
		}

		private Body body;
		public Body getBody() {
			return body;
		}
		public void setBody(Body body) {
			this.body = body;
		}
		
		private DoorNode linkedNode;
		public DoorNode getLinkedNode() {
			return linkedNode;
		}
		
		private DoorNode(int gridX, int gridY, RoomNode room) {
			this.gridX = gridX;
			this.gridY = gridY;
			room.addDoor(this);
		}
		private void link(DoorNode other) {
			this.linkedNode = other;
			other.linkedNode = this;
		}
	}
	
	
	
	
	public RoomNode firstRoom;
	
	
	public void generate() {
		RoomNode r0 = new RoomNode(Gdx.files.internal("levels/json/untitled1.json"));
		RoomNode r1 = new RoomNode(Gdx.files.internal("levels/json/room1.json"));
		RoomNode r2 = new RoomNode(Gdx.files.internal("levels/json/room1.json"));
		RoomNode r3 = new RoomNode(Gdx.files.internal("levels/json/room1.json"));

		DoorNode d00 = new DoorNode(0, -1, r0);

		DoorNode d10 = new DoorNode(-1, 0, r1);
		DoorNode d11 = new DoorNode(1, 0, r1);

		DoorNode d20 = new DoorNode(-1, 0, r2);
		DoorNode d21 = new DoorNode(1, 0, r2);

		DoorNode d30 = new DoorNode(-1, 0, r3);
		DoorNode d31 = new DoorNode(1, 0, r3);
		
		d00.link(d10);
		d11.link(d20);
		d21.link(d30);
		
		this.firstRoom = r0;
	}
}
