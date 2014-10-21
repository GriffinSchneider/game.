package backgrounds;

import zone.griff.game.SceneManager;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;

public abstract class ParallaxBackground {
	
	public float parallaxX;
	public float parallaxY;
	
	protected SceneManager sceneManager;
	
	public ParallaxBackground(SceneManager sceneManager) {
		this.sceneManager = sceneManager;
	}

	public void draw(PolygonSpriteBatch spriteBatch, OrthographicCamera camera) {
	}
	
	public void dispose() {
	}
	
	public void resize(int width, int height) {
	}

}
