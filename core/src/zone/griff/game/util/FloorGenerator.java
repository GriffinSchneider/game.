package zone.griff.game.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class FloorGenerator {
	
	public static class GeneratedRoom {
		private int x;
		public int x() {return x;}
		private int y;
		public int y() {return y;}
		private int w;
		public int w() {return w;}
		private int h;
		public int h() {return h;}
		
		public void update(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
		
		public int maxX() {
			return x + w - 1;
		}

		public int maxY() {
			return y + h - 1;
		}
		
		public int i;
		@Override
		public String toString() {
			return Character.toString((char)i);
		}
	}

	public static enum DoorDirection {
		DOOR_UP,
		DOOR_RIGHT
	}
	
	public static class GeneratedDoor extends DefaultEdge {
		private static final long serialVersionUID = -6681123121309612867L;
		public int x;
		public int y;
		public DoorDirection dir;
		
		public GeneratedRoom getSource() {
			return (GeneratedRoom) super.getSource();
		}
		public GeneratedRoom getTarget() {
			return (GeneratedRoom) super.getTarget();
		}
		public GeneratedDoor() {
			super();
		}
	}
	
	public static class RoomGraph extends ListenableUndirectedGraph<GeneratedRoom, GeneratedDoor> {
		private static final long serialVersionUID = 6334361573551242717L;
		public RoomGraph() {
			super(GeneratedDoor.class);
		}
	}
	
	static final int GRID_WIDTH = 25;
	static final int GRID_HEIGHT = 15;
	static final int MAX_ROOM_HEIGHT = 3;
	static final int MAX_ROOM_WIDTH = 3;

	static final int SEED_ROOM_COUNT = 100;
	static final int MIN_CONNECTED_ROOM_COUNT = 40;
	static final int MAX_DOORS_PER_ROOM = 2;
	static final int GROW_ITERATIONS = 40;
	
	public static RoomGraph generateFloor() {
		GeneratedRoom[][] roomMatrix = new GeneratedRoom[GRID_WIDTH][GRID_HEIGHT];

		final RoomGraph roomGraph = new RoomGraph();
		
		for (int i = 0; i < roomMatrix.length; i++) {
			roomMatrix[i] = new GeneratedRoom[GRID_HEIGHT];
		}

		// Place rooms
		for (int i = 47; i < 47 + SEED_ROOM_COUNT; i++) {
			GeneratedRoom room = new GeneratedRoom();
			room.i = i;
			room.w = 1;
			room.h = 1;
			do { // Look for an empty square for this room
				room.x = MathUtils.random(GRID_WIDTH - 1);
				room.y = MathUtils.random(GRID_HEIGHT - 1);
			} while (roomMatrix[room.x][room.y] != null);
			// Put the room there
			roomMatrix[room.x][room.y] = room;
			roomGraph.addVertex(room);
		}
		
		// Grow rooms
		for (int i = 0; i < GROW_ITERATIONS; i++) {
			for (GeneratedRoom room : roomGraph.vertexSet()) {
				growRoom(room, roomMatrix);
			}
		}
		
		// Find connections (adjacent rooms)
		for (final GeneratedRoom room : roomGraph.vertexSet()) {
			iterateAdjacent(room, roomMatrix, new RoomIterator() {
				@Override
				public boolean run(int x, int y, GeneratedRoom[][] roomMatrix) {
					GeneratedRoom adjacentRoom = roomMatrix[x][y];
					if (adjacentRoom != null) {
						if (roomGraph.edgesOf(room).size() < MAX_DOORS_PER_ROOM &&
								roomGraph.edgesOf(adjacentRoom).size() < MAX_DOORS_PER_ROOM) {
							roomGraph.addEdge(room, adjacentRoom);
						}
					}
					return true;
				}
			});
		}
		
		ConnectivityInspector<GeneratedRoom, GeneratedDoor> connectivity = 
				new ConnectivityInspector<GeneratedRoom, GeneratedDoor>(roomGraph);

		// Find the largest connected set of rooms
		final Set<GeneratedRoom> maxConnectedSet;
		{
			int maxConnectedSetSize = 0;
			Set<GeneratedRoom> maxConnectedSetTemp = null;
			for (Set<GeneratedRoom> set : connectivity.connectedSets()) {
				int size = set.size();
				if (size > maxConnectedSetSize) {
					maxConnectedSetSize = size;
					maxConnectedSetTemp = set;
				}
			}
			maxConnectedSet = maxConnectedSetTemp;
		}
		
		// If the largest connected set is too small, then reject this floor
		// and generate again.
		if (maxConnectedSet.size() < MIN_CONNECTED_ROOM_COUNT) {
			return generateFloor();
		}
		
		// Cull rooms not in the largest connected set
		Array<GeneratedRoom> roomsToRemove = new Array<GeneratedRoom>();
		for (final GeneratedRoom room : roomGraph.vertexSet()) {
			if (!maxConnectedSet.contains(room)) {
				roomsToRemove.add(room);
			}
		}
		for (GeneratedRoom room : roomsToRemove) {
			roomGraph.removeVertex(room);
			iterateContained(room, roomMatrix, new RoomIterator() {
				@Override
				public boolean run(int x, int y, GeneratedRoom[][] roomMatrix) {
					roomMatrix[x][y] = null;
					return true;
				}
			});
		}
		
		// Cull doors that don't break connectivity
		roomGraph.addGraphListener(connectivity);
		GeneratedDoor[] doors = roomGraph.edgeSet().toArray(new GeneratedDoor[roomGraph.edgeSet().size()]);
		for (int i = 0; i < doors.length; i++) {
			GeneratedDoor door = (GeneratedDoor) doors[i];
			roomGraph.removeEdge(door);
			if (!connectivity.isGraphConnected()) {
				roomGraph.addEdge(door.getSource(), door.getTarget());
			}
		}
		
		// Setup door positions
		for (GeneratedDoor door : roomGraph.edgeSet()) {
			GeneratedRoom room1 = roomGraph.getEdgeSource(door);
			GeneratedRoom room2 = roomGraph.getEdgeTarget(door);
			// If there's overlap on the x axis
			if (room1.x() <= room2.maxX() && room1.maxX() >= room2.x) {
				int minOverlap = Math.max(room1.x(), room2.x());
				int maxOverlap = Math.min(room1.maxX(), room2.maxX());
				int middle = (minOverlap + maxOverlap) / 2;
				door.dir = DoorDirection.DOOR_UP;
				door.x = middle;
				door.y = Math.min(room1.maxY(), room2.maxY());
			}
			// If there's overlap on the y axis
			if (room1.y() <= room2.maxY() && room1.maxY() >= room2.y) {
				int minOverlap = Math.max(room1.y(), room2.y());
				int maxOverlap = Math.min(room1.maxY(), room2.maxY());
				int middle = (minOverlap + maxOverlap) / 2;
				door.dir = DoorDirection.DOOR_RIGHT;
				door.x = Math.min(room1.maxX(), room2.maxX());
				door.y = middle;
			}
		}

		// Log
		printLevel(roomMatrix);
		printStats(roomGraph);
		
		return roomGraph;
	}
	
	private static enum GrowDirection {
		GROW_LEFT,
		GROW_RIGHT,
		GROW_UP,
		GROW_DOWN,
	}
	
	public static void growRoom(final GeneratedRoom room, GeneratedRoom[][] roomMatrix) {
		// Sometimes, don't grow.
		if (MathUtils.randomBoolean(0.8f)) {
			return;
		}
		
		GrowDirection[] dirs = GrowDirection.values();
		GrowDirection dir = dirs[MathUtils.random(dirs.length - 1)];
		
		// Calculate what the room's dimensions will be after growing
		int newX = room.x;
		int newY = room.y;
		int newW = room.w;
		int newH = room.h;
		switch (dir) {
		case GROW_LEFT:
			newX = room.x - 1;
			newW = room.w + 1;
			break;
		case GROW_RIGHT:
			newW = room.w + 1;
			break;
		case GROW_UP:
			newH = room.h + 1;
			break;
		case GROW_DOWN:
			newH = room.h + 1;
			newY = room.y - 1;
			break;
		}

		// If the growth would cause the room to go over the max room size, abort.
		if (newW > MAX_ROOM_WIDTH || newH > MAX_ROOM_HEIGHT) {
			return;
		}
		
		// If the growth would cause the room to overlap with another room, abort.
		boolean canGrow = iterateAdjacent(room, dir, roomMatrix, new RoomIterator() {
			@Override
			public boolean run(int x, int y, GeneratedRoom[][] roomMatrix) {
				return roomMatrix[x][y] == null;
			}
		});
		if (!canGrow) {
			return;
		}

		// Ok, now we can actually grow the room.
		iterateAdjacent(room, dir, roomMatrix, new RoomIterator() {
			@Override
			public boolean run(int x, int y, GeneratedRoom[][] roomMatrix) {
				roomMatrix[x][y] = room;
				return true;
			}
		});
		room.update(newX, newY, newW, newH);
	}

	public static interface RoomIterator {
		boolean run(int x, int y, GeneratedRoom[][]roomMatrix);
	}

	public static void iterateContained(GeneratedRoom room, GeneratedRoom[][] mat, RoomIterator iter) {
		for (int x = room.x; x < room.x + room.w; x++) {
			for (int y = room.y; y < room.y + room.h; y++) {
				iter.run(x, y, mat);
			}
		}
	}

	public static boolean iterateAdjacent(GeneratedRoom room, GeneratedRoom[][] mat, RoomIterator iter) {
		boolean retVal = true;
		for (GrowDirection dir : GrowDirection.values()) {
			retVal = retVal && iterateAdjacent(room, dir, mat, iter);
		}
		return retVal;
	}

	public static boolean iterateAdjacent(GeneratedRoom room, GrowDirection dir, GeneratedRoom[][] mat, RoomIterator iter) {
		boolean retVal = false;
		switch (dir) {
		case GROW_LEFT:
			//
			// ·XX
			// ·XX
			//
			retVal = room.x > 0;
			if (retVal) {
				for (int y = room.y; y < room.y + room.h; y++) {
					retVal = retVal && iter.run(room.x - 1, y, mat);
				}
			}
			break;
		case GROW_RIGHT:
			//
			// XX·
			// XX·
			//
			retVal = room.x + room.w < GRID_WIDTH;
			if (retVal) {
				for (int y = room.y; y < room.y + room.h; y++) {
					retVal = retVal && iter.run(room.x + room.w, y, mat);
				}
			}
			break;
		case GROW_UP:
			// ··
			// XX
			// XX
			//
			retVal = room.y + room.h < GRID_HEIGHT;
			if (retVal) {
				for (int x = room.x; x < room.x + room.w; x++) {
					retVal = retVal && iter.run(x, room.y + room.h, mat);
				}
			}
			break;
		case GROW_DOWN:
			// 
			// XX
			// XX
			// ··
			retVal = room.y > 0;
			if (retVal) {
				for (int x = room.x; x < room.x + room.w; x++) {
					retVal = retVal && iter.run(x, room.y - 1, mat);
				}
			}
			break;
		}
		return retVal;
	}
	
	public static void printLevel(GeneratedRoom[][] roomMatrix) {
		String string = "-------\n";
		for (int y = roomMatrix[0].length - 1; y >= 0; y--) {
			for (int x = 0; x < roomMatrix.length; x++) {
				GeneratedRoom room = roomMatrix[x][y];
				string += room==null ? " " : room.toString();
			}
			string += "\n";
		}
		string += "---------";
		Gdx.app.log("", string);
	}

	public static void printStats(Graph<GeneratedRoom, GeneratedDoor> rooms) {
		String string = "----\n";
		for (GeneratedRoom room : rooms.vertexSet()) {
			string += room.w + "x" + room.h + "\n";
		}
		Gdx.app.log("", string);
	}

}
