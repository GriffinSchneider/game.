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
  
	private Texture palette;
	private int paletteSize = 8;

  private PaletteManager() {
		Pixmap pixmap = new Pixmap(this.paletteSize, 1, Format.RGBA8888);
		pixmap.drawPixel(0, 0, 0x69D2E7FF);
		pixmap.drawPixel(1, 0, 0xA7DBD8FF);
		pixmap.drawPixel(2, 0, 0xE0E4CCFF);
		pixmap.drawPixel(3, 0, 0xF38630FF);
		pixmap.drawPixel(4, 0, 0xFA6900FF);
		pixmap.drawPixel(5, 0, 0xFF4E50FF);
		pixmap.drawPixel(6, 0, 0x48A940FF);
		pixmap.drawPixel(7, 0, 0x67C827FF);
		pixmap.drawPixel(8, 0, 0xFA6900FF);

		this.palette = new Texture(pixmap);
		this.palette.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		pixmap.dispose();
  }

  public static Texture getPalette() {
  	return getInstance().palette;
  }

  public static int getPaletteSize() {
  	return getInstance().paletteSize;
  }

}
