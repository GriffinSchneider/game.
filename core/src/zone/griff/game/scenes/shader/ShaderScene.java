package zone.griff.game.scenes.shader;

import zone.griff.game.SceneManager;
import zone.griff.game.scenes.Scene;

import backgrounds.ShaderBackground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;

public class ShaderScene extends Scene {
	
	private ShaderBackground background;
	private PolygonSpriteBatch polyBatch;

	public ShaderScene(SceneManager game) {
		super(game);
		this.background = new ShaderBackground(game);
		this.polyBatch = new PolygonSpriteBatch();
	}

	@Override
	public void resize(int w, int h) {
		this.background.resizeCamera(w, h);
	}
	

	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub
	}


	@Override
	public void render() {
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			return;
		}
		this.background.draw(this.polyBatch, null);
	}
	
	@Override
	public void dispose() {
		this.background.dispose();
	}
}
