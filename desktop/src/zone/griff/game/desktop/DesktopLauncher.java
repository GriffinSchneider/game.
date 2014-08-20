package zone.griff.game.desktop;

import zone.griff.game.SceneManager;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		config.vSyncEnabled = false;
		config.foregroundFPS = 0;
		new LwjglApplication(new SceneManager(), config);
	}
}
