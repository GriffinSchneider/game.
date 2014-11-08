package zone.griff.game.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import zone.griff.game.util.FloorGenerator.GeneratedRoom;
import zone.griff.game.util.FloorGenerator.GrowDirection;

public class AdjacentGridsIterator implements Iterator<IntVector2> {
	private GeneratedRoom room;
	private GrowDirection dir;
	private int index = 0;
	private final int numAdjacent;
	private final IntVector2 v = new IntVector2();
	
	public AdjacentGridsIterator(GeneratedRoom room, GrowDirection dir) {
		this.room = room;
		this.dir = dir;
		if (dir == GrowDirection.GROW_LEFT || dir == GrowDirection.GROW_RIGHT) {
			this.numAdjacent = room.h();
		} else {
			this.numAdjacent = room.w();
		}
	}

  @Override
  public IntVector2 next() {
		if (!hasNext()) {
			throw new NoSuchElementException("");
		}

		switch (dir) {
		case GROW_LEFT:
			//
			// ·XX
			// ·XX
			//
			v.set(room.x() - 1, room.y() + index);
			break;
		case GROW_RIGHT:
			//
			// XX·
			// XX·
			//
			v.set(room.x() + room.w(), room.y() + index);
			break;
		case GROW_UP:
			// ··
			// XX
			// XX
			//
			v.set(room.x() + index, room.y() + room.h());
			break;
		case GROW_DOWN:
			// 
			// XX
			// XX
			// ··
			v.set(room.x() + index, room.y() - 1);
			break;
		}

		index++;
		return v;
  }

  @Override
  public boolean hasNext() {
		return index < numAdjacent;
  }
}
