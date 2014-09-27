package zone.griff.game.util;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;

public  class SpriteAndOutline {

	private ArrayList<PolygonSprite> sprites;
	private PolygonSprite outline;
	
	public SpriteAndOutline(ArrayList<PolygonSprite> sprites, PolygonSprite outline) {
		this.sprites = sprites;
		this.outline = outline;
	}

	public ArrayList<PolygonSprite> getSprites() {
		return this.sprites;
	}

	public void drawSprite(PolygonSpriteBatch batch) {
		for (PolygonSprite sprite : this.sprites) {
			sprite.draw(batch);
		}
	}

	public void drawOutline(PolygonSpriteBatch batch) {
		this.outline.draw(batch);
	}

	public void setRotation (float degrees) {
		for (PolygonSprite sprite : this.sprites) {
			sprite.setRotation(degrees);
		}
		this.outline.setRotation(degrees);
	}

	public void setPosition (float x, float y) {
		for (PolygonSprite sprite : this.sprites) {
			sprite.setPosition(x, y);
		}
		this.outline.setPosition(x, y);
	}

	public void setOrigin (float x, float y) {
		for (PolygonSprite sprite : this.sprites) {
			sprite.setOrigin(x, y);
		}
		this.outline.setOrigin(x, y);
	}

}
