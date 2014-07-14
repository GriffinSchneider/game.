package zone.griff.game.pools;

import com.badlogic.gdx.math.Vector2;

public class Vector2Pool {

	private static final Pool<Vector2> POOL = new Pool<Vector2>() {
		@Override
		protected Vector2 onAllocatePoolItem() {
			return new Vector2();
		}
	};

	public static Vector2 obtain() {
		return POOL.obtainPoolItem();
	}

	public static Vector2 obtain(final Vector2 pCopyFrom) {
		return POOL.obtainPoolItem().set(pCopyFrom);
	}

	public static Vector2 obtain(final float pX, final float pY) {
		return POOL.obtainPoolItem().set(pX, pY);
	}

	public static void release(final Vector2 pVector2) {
		POOL.recylePoolItem(pVector2);
	}

}