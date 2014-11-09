package zone.griff.game.levelgeneration;

import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;

import zone.griff.game.levelgeneration.GeneratedDoor.DoorDirection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class RoomGrowingFloorGenerator extends FloorGenerator {
	
  private static RoomGrowingFloorGenerator instance = null;
  public static RoomGrowingFloorGenerator getInstance() {
     if(instance == null) {
        instance = new RoomGrowingFloorGenerator();
     }
     return instance;
  }
	
	static final int MAX_ROOM_HEIGHT = 3;
	static final int MAX_ROOM_WIDTH = 3;
	static final int SEED_ROOM_COUNT = 80;
	static final int MIN_CONNECTED_ROOM_COUNT = 40;
	static final int GROW_ITERATIONS = 40;
	
	@Override
	protected RoomGraph generateFloor(GeneratedRoom[][] roomMatrix, RoomGraph roomGraph) {
//		MathUtils.random = new Random(420);

		// Place rooms
		for (int i = 33; i < 33 + SEED_ROOM_COUNT; i++) {
			if (i == 127) { continue; }
			int x, y;
			do { // Look for an empty square for this room
				x = MathUtils.random(GRID_WIDTH - 1);
				y = MathUtils.random(GRID_HEIGHT - 1);
			} while (roomMatrix[x][y] != null);
			// Put the room there
			GeneratedRoom room = new GeneratedRoom(x, y);
			room.i = i;
			roomMatrix[x][y] = room;
			roomGraph.addVertex(room);
		}
		
		// Grow rooms. 1x1 rooms are more likely to grow.
		for (int i = 0; i < GROW_ITERATIONS; i++) {
			for (GeneratedRoom room : roomGraph.vertexSet()) {
				float chanceToGrow = (room.h() == 1 && room.w() == 1) ? 0.7f : 0.2f;
				if (MathUtils.randomBoolean(chanceToGrow)) {
					growRoom(room, roomMatrix);
				}
			}
		}
		
		// Cull 1x1 rooms
		{
			GeneratedRoom[] roomsArray = new GeneratedRoom[roomGraph.vertexSet().size()];
			roomGraph.vertexSet().toArray(roomsArray);
			for (int i = 0; i < roomsArray.length; i++) {
				final GeneratedRoom room = roomsArray[i];
				if (room.w() == 1 && room.h() == 1) {
					removeRoom(room, roomMatrix, roomGraph);
				}
			}
		}

		// Find connections (adjacent rooms)
		for (final GeneratedRoom room : roomGraph.vertexSet()) {
			AllAdjacentGridsIterator iter = new AllAdjacentGridsIterator(room);
			while(iter.hasNext()) {
				IntVector2 grid = iter.next();
				if (!isInGrid(grid)) continue;
				GeneratedRoom adjacentRoom = roomMatrix[grid.x][grid.y];
				if (adjacentRoom != null) {
					if (roomGraph.edgesOf(room).size() < maxDoorsForRoom(room) &&
							roomGraph.edgesOf(adjacentRoom).size() < maxDoorsForRoom(adjacentRoom)) {
						roomGraph.addEdge(room, adjacentRoom);
					}
				}
			}
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
			Gdx.app.log("", String.format("Only %d rooms, regenerating floor!", maxConnectedSet.size()));
			return generateFloor();
		}
		
		// Cull rooms not in the largest connected set
		{
			GeneratedRoom[] roomsArray = new GeneratedRoom[roomGraph.vertexSet().size()];
			roomGraph.vertexSet().toArray(roomsArray);
			for (int i = 0; i < roomsArray.length; i++) {
				final GeneratedRoom room = roomsArray[i];
				if (!maxConnectedSet.contains(room)) {
					removeRoom(room, roomMatrix, roomGraph);
				}
			}
		}

		roomGraph.addGraphListener(connectivity);

		// Cull doors that don't break connectivity
//		{
//			GeneratedDoor[] doors = roomGraph.edgeSet().toArray(new GeneratedDoor[roomGraph.edgeSet().size()]);
//			for (int i = 0; i < doors.length; i++) {
//				GeneratedDoor door = doors[i];
//				roomGraph.removeEdge(door);
//				if (!connectivity.isGraphConnected()) {
//					roomGraph.addEdge(door.getSource(), door.getTarget());
//				}
//			}
//		}
		
		// Setup door positions
		for (GeneratedDoor door : roomGraph.edgeSet()) {
			GeneratedRoom room1 = roomGraph.getEdgeSource(door);
			GeneratedRoom room2 = roomGraph.getEdgeTarget(door);
			// If there's overlap on the x axis
			if (room1.x() <= room2.maxX() && room1.maxX() >= room2.x()) {
				int minOverlap = Math.max(room1.x(), room2.x());
				int maxOverlap = Math.min(room1.maxX(), room2.maxX());
				int middle = (minOverlap + maxOverlap) / 2;
				door.dir = DoorDirection.DOOR_UP;
				door.x = middle;
				door.y = Math.min(room1.maxY(), room2.maxY());
			}
			// If there's overlap on the y axis
			if (room1.y() <= room2.maxY() && room1.maxY() >= room2.y()) {
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
		printStats(roomGraph, roomMatrix);
		
		return roomGraph;
	}
	
	public static void growRoom(final GeneratedRoom room, final GeneratedRoom[][] roomMatrix) {
		GrowDirection[] dirs = GrowDirection.values();
		GrowDirection dir = dirs[MathUtils.random(dirs.length - 1)];
		
		// Calculate what the room's dimensions will be after growing
		int newX = room.x();
		int newY = room.y();
		int newW = room.w();
		int newH = room.h();
		switch (dir) {
		case GROW_LEFT:
			newX--;
		case GROW_RIGHT:
			newW++;
			break;

		case GROW_DOWN:
			newY--;
		case GROW_UP:
			newH++;
			break;
		}

		// If the growth would cause the room to go over the max room size, abort.
		if (newW > MAX_ROOM_WIDTH || newH > MAX_ROOM_HEIGHT) {
			return;
		}
		
		// If the growth would cause the room to overlap with another room, abort.
		boolean canGrow = true;
		AdjacentGridsIterator iter = new AdjacentGridsIterator(room, dir);
		while(canGrow && iter.hasNext()) {
			IntVector2 grid = iter.next();
			canGrow = isInGrid(grid) && roomMatrix[grid.x][grid.y] == null;
		}
		if (!canGrow) {
			return;
		}

		// Ok, now we can actually grow the room.
		iter = new AdjacentGridsIterator(room, dir);
		while(iter.hasNext()) {
			IntVector2 grid = iter.next();
			roomMatrix[grid.x][grid.y] = room;
		}
		room.update(newX, newY, newW, newH);
	}

}
