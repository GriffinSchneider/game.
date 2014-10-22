package zone.griff.game.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class PaletteManager {
  private static PaletteManager instance = null;
  public static PaletteManager getInstance() {
     if(instance == null) {
        instance = new PaletteManager();
     }
     return instance;
  }
  
	private Texture paletteTexture;

	private int[] palette;

  private PaletteManager() {
  	
  	this.palette = new int[] {
  		0x69D2E7FF, // Blue
  		0xA7DBD8FF, // Light Blue
  		0xE0E4CCFF, // Off-white
  		0xF38630FF, // Light Orange
  		0xFA6900FF, // Orange
  		0xFF4E50FF, // Red
  		0x48A940FF, // Dark Green
  		0x67C827FF, // Green
  	};
  	
		Pixmap pixmap = new Pixmap(this.palette.length, 1, Format.RGBA8888);
		for (int i = 0; i < this.palette.length; i++) {
			pixmap.drawPixel(i, 0, this.palette[i]);
		}

		this.paletteTexture = new Texture(pixmap);
		this.paletteTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		pixmap.dispose();
  }

  public static Texture getPaletteTexture() {
  	return getInstance().paletteTexture;
  }

  public static int getPaletteSize() {
  	return getInstance().palette.length;
  }

  public static int getPaletteColorAtIndex(int index) {
  	return getInstance().palette[index];
  }

}
