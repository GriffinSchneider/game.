package zone.griff.game.scenes.shader;

import zone.griff.game.SceneManager;
import zone.griff.game.scenes.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class ShaderScene extends Scene {
	
	ShaderBackground background;

	public ShaderScene(SceneManager game) {
		super(game);
		this.background = new ShaderBackground(game);
	}

	@Override
	public void resize(int w, int h) {
		this.background.resize(w, h);
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
		this.background.draw();
	}
	
	@Override
	public void dispose() {
		this.background.dispose();
	}
}
