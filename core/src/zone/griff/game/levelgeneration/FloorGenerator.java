package zone.griff.game.levelgeneration;

import org.jgrapht.graph.ListenableUndirectedGraph;

import com.badlogic.gdx.Gdx;

public abstract class FloorGenerator {

	public static enum GrowDirection {
		GROW_DOWN,
		GROW_LEFT,
		GROW_RIGHT,
		GROW_UP,
	}

	public static class RoomGraph extends ListenableUndirectedGraph<GeneratedRoom, GeneratedDoor> {
		private static final long serialVersionUID = 6334361573551242717L;
		public RoomGraph() {
			super(GeneratedDoor.class);
		}
	}
	
	protected static final int GRID_WIDTH = 25;
	protected static final int GRID_HEIGHT = 15;
	
	public RoomGraph generateFloor() {
		final GeneratedRoom[][] roomMatrix = new GeneratedRoom[GRID_WIDTH][GRID_HEIGHT];
		final RoomGraph roomGraph = new RoomGraph();
		for (int i = 0; i < roomMatrix.length; i++) {
			roomMatrix[i] = new GeneratedRoom[GRID_HEIGHT];
		}
		return generateFloor(roomMatrix, roomGraph);
	}

	protected abstract RoomGraph generateFloor(GeneratedRoom[][] roomMatrix, RoomGraph roomGraph);	
	
	protected static boolean isInGrid(IntVector2 v) {
		return v.x > 0 && v.y > 0 && v.x < GRID_WIDTH && v.y < GRID_HEIGHT;
	}
	
	protected static int maxDoorsForRoom(GeneratedRoom room) {
		if (room.h() + room.w() > 4) {
			return 3;
		} else {
			return 2;
		}
	}

	protected void removeRoom(GeneratedRoom room, final GeneratedRoom[][] roomMatrix, RoomGraph roomGraph) {
		roomGraph.removeVertex(room);
		iterateContained(room, new RoomIterator() {
			@Override
			public boolean run(int x, int y) {
				roomMatrix[x][y] = null;
				return true;
			}
		});
	}

	protected interface RoomIterator {
		boolean run(int x, int y);
	}

	protected void iterateContained(GeneratedRoom room, RoomIterator iter) {
		for (int x = room.x(); x < room.x() + room.w(); x++) {
			for (int y = room.y(); y < room.y() + room.h(); y++) {
				iter.run(x, y);
			}
		}
	}
	
	protected void printLevel(GeneratedRoom[][] roomMatrix) {
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

	protected void printStats(final RoomGraph rooms, final GeneratedRoom[][] roomMatrix) {
 		final StringBuilder string = new StringBuilder("----\n");
 		for (GeneratedRoom room : rooms.vertexSet()) {
 			string.append(room.toString() + ": ");
 			string.append(room.w() + "x" + room.h() + " ");
 			string.append(room.doorString(rooms));
 			string.append("\n");
 		}
 		Gdx.app.log("", string.toString());
	}

}
