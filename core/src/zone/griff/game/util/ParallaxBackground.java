package zone.griff.game.util;

import zone.griff.game.SceneManager;

public abstract class ParallaxBackground {
	
	public float parallaxX;
	public float parallaxY;
	
	protected SceneManager sceneManager;
	
	public ParallaxBackground(SceneManager sceneManager) {
		this.sceneManager = sceneManager;
	}

	public void draw() {
	}
	
	public void resize(int width, int height) {
	}

}
