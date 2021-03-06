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

	public abstract void draw(PolygonSpriteBatch spriteBatch, OrthographicCamera camera);
	
	public abstract void dispose();
	
	public void resizeCamera(float viewportWidth, float viewportHeight) {}

}
