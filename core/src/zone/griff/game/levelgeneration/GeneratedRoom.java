package zone.griff.game.levelgeneration;

import java.util.Set;

import zone.griff.game.levelgeneration.FloorGenerator.RoomGraph;
import zone.griff.game.pools.Vector2Pool;

public class GeneratedRoom {
	
	public GeneratedRoom(int x, int y) {
		this.x = x;
		this.y = y;
		this.w = 1;
		this.h = 1;
	}
	
	private int x;
	public int x() {return x;}
	private int y;
	public int y() {return y;}
	private int w;
	public int w() {return w;}
	private int h;
	public int h() {return h;}

	private boolean isPlaceholder = false;
	
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
	
	public String doorString(RoomGraph roomGraph) {

		final StringBuilder string = new StringBuilder("");
		
		final Set<GeneratedDoor> doors = roomGraph.edgesOf(this);

		AdjacentGridsIterator iters[] = {
				new AdjacentGridsIterator(this, FloorGenerator.GrowDirection.GROW_DOWN),
				new AdjacentGridsIterator(this, FloorGenerator.GrowDirection.GROW_LEFT),
				new AdjacentGridsIterator(this, FloorGenerator.GrowDirection.GROW_RIGHT),
				new AdjacentGridsIterator(this, FloorGenerator.GrowDirection.GROW_UP)
		};

		for (AdjacentGridsIterator iter : iters) {
			while (iter.hasNext()) {
				IntVector2 grid = iter.next();
				boolean hasDoor = false;
				for (GeneratedDoor door : doors) {
					if (door.overlapsGridPosition(grid)) {
						hasDoor = true;
						break;
					}
				}
				string.append(hasDoor ? 'd' : 'o');
			}
			string.append("c");
		}

		return string.toString();
	}

	public static IntVector2 sizeForDoorString(String doorString) {
		String[] edges = doorString.split("c");
		IntVector2 retVal = new IntVector2();
		retVal.x = edges[0].length();
		retVal.y = edges[1].length();
		return retVal;
	}

	public static GeneratedRoom fromDoorString(String doorString, int x, int y, RoomGraph roomGraph) {
		GeneratedRoom retVal = new GeneratedRoom(x, y);

		IntVector2 size = sizeForDoorString(doorString);
		retVal.w = size.x;
		retVal.h = size.y;

		String[] edges = doorString.split("c");

		final Set<GeneratedDoor> doors = roomGraph.edgesOf(retVal);

		AdjacentGridsIterator iters[] = {
				new AdjacentGridsIterator(retVal, FloorGenerator.GrowDirection.GROW_DOWN),
				new AdjacentGridsIterator(retVal, FloorGenerator.GrowDirection.GROW_LEFT),
				new AdjacentGridsIterator(retVal, FloorGenerator.GrowDirection.GROW_RIGHT),
				new AdjacentGridsIterator(retVal, FloorGenerator.GrowDirection.GROW_UP)
		};

		for (AdjacentGridsIterator iter : iters) {
			while (iter.hasNext()) {

				// Is there a door at this position?
				GeneratedDoor foundDoor = null;
				IntVector2 grid = iter.next();
				for (GeneratedDoor door : doors) {
					if (door.overlapsGridPosition(grid)) {
						foundDoor = door;
						break;
					}
				}

				if (foundDoor != null) {

					// Check that there's an 'o' at the right place in the string

					// Get the 'placeholder room' that should be on one end of the door.
					GeneratedRoom placeholderRoom = null;
					GeneratedRoom otherRoom = null;
					if (foundDoor.getSource().isPlaceholder) {
						placeholderRoom = foundDoor.getSource();
						otherRoom = foundDoor.getTarget();
					} else {
						placeholderRoom = foundDoor.getTarget();
						otherRoom = foundDoor.getSource();
					}
					assert placeholderRoom.isPlaceholder;

					// Remove the placeholder, put this room at the other end of the door.
					int foundDoorX = foundDoor.x;
					int foundDoorY = foundDoor.y;
					GeneratedDoor.DoorDirection foundDoorDir = foundDoor.dir;
					roomGraph.removeVertex(placeholderRoom);
					roomGraph.addVertex(retVal);
					GeneratedDoor newDoor = roomGraph.addEdge(otherRoom, retVal);
					newDoor.x = foundDoorX;
					newDoor.y = foundDoorY;
					newDoor.dir = foundDoorDir;
					
				} else {
					// There's no preexisting door.
					// If this room has a door in its string, then make a new placeholder room and the door
					// Otherwise, that's a non-overlapping door, so assert.
				}

			}
		}

		return retVal;
	}
}
