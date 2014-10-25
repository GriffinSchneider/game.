package zone.griff.game.util;

import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class FloorGenerator {
	
	private static class GeneratedRoom {
		int x;
		int y;
		int w;
		int h;
		int i;
		@Override
		public String toString() {
			return Character.toString((char)i);
		}
	}
	
	static final int GRID_WIDTH = 30;
	static final int GRID_HEIGHT = 30;
	static final int MAX_ROOM_HEIGHT = 4;
	static final int MAX_ROOM_WIDTH = 4;
	
	public static void doo() {
		
		GeneratedRoom[][] roomMatrix = new GeneratedRoom[GRID_WIDTH][GRID_HEIGHT];

		final ListenableUndirectedGraph<GeneratedRoom, DefaultEdge> roomGraph = 
				new ListenableUndirectedGraph<GeneratedRoom, DefaultEdge>(DefaultEdge.class);
//		ConnectivityInspector<GeneratedRoom, DefaultEdge> connectivity = 
//				new ConnectivityInspector<GeneratedRoom, DefaultEdge>(roomGraph);
		
		for (int i = 0; i < roomMatrix.length; i++) {
			roomMatrix[i] = new GeneratedRoom[GRID_HEIGHT];
		}

		// Place rooms
		for (int i = 47; i < 126; i++) {
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
		
		// Grow
		for (int i = 0; i < 30; i++) {
			for (GeneratedRoom room : roomGraph.vertexSet()) {
				growRoom(room, roomMatrix);
			}
		}
		
		// Find connections
		for (final GeneratedRoom room : roomGraph.vertexSet()) {
			iterateAdjacent(room, roomMatrix, new RoomIterator() {
				@Override
				public boolean run(int x, int y, GeneratedRoom[][] roomMatrix) {
					GeneratedRoom adjacentRoom = roomMatrix[x][y];
					if (adjacentRoom != null) {
						roomGraph.addEdge(room, adjacentRoom);
					}
					return true;
				}
			});
		}
		
		// Log
		printLevel(roomMatrix);
		printStats(roomGraph);
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
			newY = room.y - 1;
			newH = room.h + 1;
			break;
		case GROW_DOWN:
			newH = room.h + 1;
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
		room.x = newX;
		room.y = newY;
		room.w = newW;
		room.h = newH;
	}

	public static interface RoomIterator {
		boolean run(int x, int y, GeneratedRoom[][]roomMatrix);
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
			retVal = room.y > 0;
			if (retVal) {
				for (int x = room.x; x < room.x + room.w; x++) {
					retVal = retVal && iter.run(x, room.y - 1, mat);
				}
			}
			break;
		case GROW_DOWN:
			// 
			// XX
			// XX
			// ··
			retVal = room.y + room.h < GRID_HEIGHT;
			if (retVal) {
				for (int x = room.x; x < room.x + room.w; x++) {
					retVal = retVal && iter.run(x, room.y + room.h, mat);
				}
			}
			break;
		}
		return retVal;
	}
	
	public static void printLevel(GeneratedRoom[][] roomMatrix) {
		String string = "-------\n";
		for (int x = 0; x < roomMatrix.length; x++) {
			for (int y = 0; y < roomMatrix[0].length; y++) {
				GeneratedRoom room = roomMatrix[x][y];
				string += room==null ? " " : room.toString();
			}
			string += "\n";
		}
		string += "---------";
		Gdx.app.log("", string);
	}

	public static void printStats(Graph<GeneratedRoom, DefaultEdge> rooms) {
		String string = "----\n";
		for (GeneratedRoom room : rooms.vertexSet()) {
			string += room.w + "x" + room.h + "\n";
		}
		Gdx.app.log("", string);
	}

}
