package zone.griff.game.pools;

import java.util.Stack;

import com.badlogic.gdx.Gdx;

public abstract class Pool<T> {

	private final Stack<T> mAvailableItems = new Stack<T>();
	private int mUnrecycledCount;

	public Pool() {

	}

	public Pool(final int initialSize) {
		final Stack<T> availableItems = this.mAvailableItems;
		for(int i = initialSize - 1; i >= 0; i--) {
			availableItems.push(this.onAllocatePoolItem());
		}
	}

	protected abstract T onAllocatePoolItem();

	public synchronized int getUnrecycledCount() {
		return this.mUnrecycledCount;
	}

	public synchronized T obtainPoolItem() {
		final T item;

		if (this.mAvailableItems.size() > 0) {
			item = this.mAvailableItems.pop();
		} else {
			Gdx.app.log("", "Pool exhausted, with " + this.mUnrecycledCount + " unrecycled items. Allocating one more...");
			item = this.onAllocatePoolItem();
		}

		this.mUnrecycledCount++;
		return item;
	}

	public synchronized void recylePoolItem(final T pItem) {
		if (pItem == null) {
			throw new IllegalArgumentException("Cannot recycle null item!");
		}

		this.mAvailableItems.push(pItem);

		this.mUnrecycledCount--;

		if (this.mUnrecycledCount < 0) {
			Gdx.app.log("", "More items recycled than obtained!");
		}
	}

}