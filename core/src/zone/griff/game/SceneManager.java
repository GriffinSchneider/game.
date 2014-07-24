package zone.griff.game;

import java.util.Stack;

import zone.griff.game.scenes.Box2dScene;
import zone.griff.game.scenes.Scene;
import zone.griff.game.scenes.ShaderScene;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SceneManager implements ApplicationListener {

	public static final float STEP = 1 / 60f;
	public static final int V_WIDTH = 500;
	public static final int V_HEIGHT = 375;
	
	public float[] gameSizeArray;

	public float gameTime;
	
	private SpriteBatch sb;
	public SpriteBatch getSpriteBatch() { return sb; }
	
	private Stack<Scene> gameStates;

	private FPSLogger fpsLogger;


	@Override
	public void create() {
		this.gameTime = 0;
		this.fpsLogger = new FPSLogger();
		this.sb = new SpriteBatch();
		this.gameStates = new Stack<Scene>();
		
	}
	
	boolean didStartup;
	public void startup() {
		if (didStartup) {
			return;
		}
		didStartup = true;
//		this.pushState(new ShaderScene(this));
		this.pushState(new Box2dScene(this));
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void dispose() {
		for (Scene s : this.gameStates) {
			s.dispose();
		}
	}

	@Override
	public void resize(int width, int height) {
		this.gameSizeArray = new float[]{width, height, 1.0f};

		this.startup();

		for (Scene s : this.gameStates) {
			s.resize(width, height);
		}
		
	}
	
	@Override
	public void render() {
		fpsLogger.log();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float dt = Gdx.graphics.getDeltaTime();
		this.gameTime += dt;

		for (Scene scene : gameStates) {
			scene.update(dt);
			scene.render();
		}
	}

	public void setState(Scene state) {
		popState();
		pushState(state);
	}

	public void pushState(Scene state) {
		this.gameStates.push(state);
	}

	public void popState() {
		Scene s = this.gameStates.pop();
		s.dispose();
	}

}
