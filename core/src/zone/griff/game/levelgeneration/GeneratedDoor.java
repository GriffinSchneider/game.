package zone.griff.game.levelgeneration;

import org.jgrapht.graph.DefaultEdge;

public class GeneratedDoor extends DefaultEdge {
	
	public static enum DoorDirection {
		DOOR_UP,
		DOOR_RIGHT
	}

	private static final long serialVersionUID = -6681123121309612867L;
	public int x;
	public int y;
	public DoorDirection dir;
	
	public GeneratedDoor() {
		super();
	}
	
	public GeneratedRoom getSource() {
		return (GeneratedRoom) super.getSource();
	}
	
	public GeneratedRoom getTarget() {
		return (GeneratedRoom) super.getTarget();
	}
	
	// Is the given grid square one of the squares linked by this door?
	public boolean overlapsGridPosition(IntVector2 grid) {
		return 
				(this.x == grid.x && this.y == grid.y) ||
				(this.dir == DoorDirection.DOOR_UP && this.x == grid.x && this.y+1 == grid.y) ||
				(this.dir == DoorDirection.DOOR_RIGHT && this.x+1 == grid.x && this.y == grid.y);
	}
	
}
