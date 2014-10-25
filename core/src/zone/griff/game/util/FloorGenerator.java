package zone.griff.game.util;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

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
		Array<GeneratedRoom> rooms = new Array<GeneratedRoom>();
		GeneratedRoom[][] roomMatrix = new GeneratedRoom[GRID_WIDTH][GRID_HEIGHT];
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
			rooms.add(room);
		}
		
		// Grow
		for (int i = 0; i < 30; i++) {
			for (GeneratedRoom room : rooms) {
				growRoom(room, roomMatrix);
			}
		}
		
		// Log
		printLevel(roomMatrix);
		printStats(rooms);
	}
	
	private static enum GrowDirection {
		GROW_LEFT,
		GROW_RIGHT,
		GROW_UP,
		GROW_DOWN,
	}
	
	public static void growRoom(GeneratedRoom room, GeneratedRoom[][] roomMatrix) {
		// Sometimes, don't grow.
		if (MathUtils.randomBoolean(0.7f)) {
			return;
		}

		GrowDirection[] dirs = GrowDirection.values();
		GrowDirection dir = dirs[MathUtils.random(dirs.length - 1)];
		
		boolean canGrow = true;
		switch (dir) {
		case GROW_LEFT:
			//
			// ·XX
			// ·XX
			//
			if (room.x > 0) {
				for (int y = room.y; y < room.y + room.h; y++) {
					canGrow = canGrow && (roomMatrix[room.x - 1][y] == null);
				}
				canGrow = canGrow && room.w < MAX_ROOM_WIDTH;
				if (!canGrow) return;
				for (int y = room.y; y < room.y + room.h; y++) {
					roomMatrix[room.x - 1][y] = room;
				}
				room.x--;
				room.w++;
			}
			break;
		case GROW_RIGHT:
			//
			// XX·
			// XX·
			//
			if (room.x + room.w < GRID_WIDTH) {
				for (int y = room.y; y < room.y + room.h; y++) {
					canGrow = canGrow && (roomMatrix[room.x + room.w][y] == null);
				}
				canGrow = canGrow && room.w < MAX_ROOM_WIDTH;
				if (!canGrow) return;
				for (int y = room.y; y < room.y + room.h; y++) {
					roomMatrix[room.x + room.w][y] = room;
				}
				room.w++;
			}
			break;
		case GROW_UP:
			// ··
			// XX
			// XX
			//
			if (room.y > 0) {
				for (int x = room.x; x < room.x + room.w; x++) {
					canGrow = canGrow && (roomMatrix[x][room.y - 1] == null);
				}
				canGrow = canGrow && room.h < MAX_ROOM_HEIGHT;
				if (!canGrow) return;
				for (int x = room.x; x < room.x + room.w; x++) {
					roomMatrix[x][room.y - 1] = room;
				}
				room.y--;
				room.h++;
			}
			break;
		case GROW_DOWN:
			// 
			// XX
			// XX
			// ··
			if (room.y + room.h < GRID_HEIGHT) {
				for (int x = room.x; x < room.x + room.w; x++) {
					canGrow = canGrow && (roomMatrix[x][room.y + room.h] == null);
				}
				canGrow = canGrow && room.h < MAX_ROOM_HEIGHT;
				if (!canGrow) return;
				for (int x = room.x; x < room.x + room.w; x++) {
					roomMatrix[x][room.y + room.h] = room;
				}
				room.h++;
			}
			
			break;
		}
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

	public static void printStats(Array<GeneratedRoom> rooms) {
		String string = "----\n";
		for (GeneratedRoom room : rooms) {
			string += room.w + "x" + room.h + "\n";
		}
		Gdx.app.log("", string);
	}

}
