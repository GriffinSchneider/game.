package zone.griff.game.scenes;

import zone.griff.game.SceneManager;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Scene {
	
	public abstract void handleInput();
	public abstract void update(float dt);
	public abstract void render();
	public abstract void dispose();
	public abstract void resize(int width, int height);
	
	public SceneManager sceneManager;
	SpriteBatch sb;
	
	public Scene(SceneManager game) {
		this.sceneManager= game;
		this.sb = game.getSpriteBatch();
	}
}
