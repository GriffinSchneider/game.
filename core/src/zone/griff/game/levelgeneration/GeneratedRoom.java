package zone.griff.game.levelgeneration;

import java.util.Set;

import zone.griff.game.levelgeneration.FloorGenerator.RoomGraph;

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
		
		AllAdjacentGridsIterator iter = new AllAdjacentGridsIterator(this);
		while(iter.hasNext()) {
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

		return string.toString();
	}
}
