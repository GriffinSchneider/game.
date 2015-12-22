package zone.griff.game.levelgeneration;

import java.util.Iterator;
import java.util.NoSuchElementException;

import zone.griff.game.levelgeneration.FloorGenerator.GrowDirection;

import javax.naming.OperationNotSupportedException;

// Adjacent squares are ordered like:
//
//    7 8
//  4 x x 6
//  3 x x 5
//    1 2
//
public  class AllAdjacentGridsIterator implements Iterator<IntVector2> {
	private GeneratedRoom room;
	private int index = 0;
	private final int numAdjacent;
	
	private final AdjacentGridsIterator down;
	private final AdjacentGridsIterator left;
	private final AdjacentGridsIterator right;
	private final AdjacentGridsIterator up;
	
	public AllAdjacentGridsIterator(GeneratedRoom room) {
		this.room = room;
		this.numAdjacent = room.w()*2 + room.h()*2;
		
		this.down = new AdjacentGridsIterator(room, GrowDirection.GROW_DOWN);
		this.left = new AdjacentGridsIterator(room, GrowDirection.GROW_LEFT);
		this.right = new AdjacentGridsIterator(room, GrowDirection.GROW_RIGHT);
		this.up = new AdjacentGridsIterator(room, GrowDirection.GROW_UP);
	}

  @Override
  public IntVector2 next() {
		if (!hasNext()) {
			throw new NoSuchElementException("");
		}
		
		IntVector2 v;
		if (index < room.w()) {
			v = down.next();
		} else if (index < room.w() + room.h()) {
			v = left.next();
		} else if (index < room.w() + room.h() + room.h()) {
			v = right.next();
		} else {
			v = up.next();
		}

		index++;
		return v;
  }

  @Override
  public boolean hasNext() {
		return index < numAdjacent;
  }

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
