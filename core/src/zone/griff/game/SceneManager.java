package zone.griff.game;

import java.util.Stack;

import zone.griff.game.entities.DoesItWork;
import zone.griff.game.scenes.Scene;
import zone.griff.game.scenes.box2d.Box2dScene;
import zone.griff.game.scenes.map.MapScene;
import zone.griff.game.util.PaletteManager;
import zone.griff.game.util.ShaderManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.profiling.GL20Profiler;

public class SceneManager implements ApplicationListener {

	public static final float STEP = 1 / 60f;
	public static final int V_WIDTH = 500;
	public static final int V_HEIGHT = 375;
	
	public float[] gameSizeArray;
	public int screenWidth;
	public int screenHeight;

	public float gameTime;
	private float timeSinceLastLog;
	
	private PolygonSpriteBatch spriteBatch;
	public PolygonSpriteBatch getSpriteBatch() { return this.spriteBatch; }
	
	private Stack<Scene> gameStates;

	private FPSLogger fpsLogger;


	@Override
	public void create() {
		this.gameTime = 0;
		this.fpsLogger = new FPSLogger();
		this.gameStates = new Stack<Scene>();
		this.spriteBatch = new PolygonSpriteBatch();
	}
	
	boolean didStartup;
	public void startup() {
		if (didStartup) {
			return;
		}

		DoesItWork diw = new DoesItWork();
		Gdx.app.log("sdf", diw.thing());
		
		GL20Profiler.enable();
		
		didStartup = true;
//		this.pushState(new ShaderScene(this));
//		this.pushState(new Box2dScene(this));
		this.pushState(new MapScene(this));
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
		PaletteManager.dispose();
		ShaderManager.dispose();
	}

	@Override
	public void resize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		this.gameSizeArray = new float[]{this.screenWidth, this.screenHeight, 1.0f};

		this.startup();

		for (Scene s : this.gameStates) {
			s.resize(this.screenWidth, this.screenHeight);
		}
		
	}
	
	@Override
	public void render() {
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		float dt = Gdx.graphics.getDeltaTime();
		this.timeSinceLastLog += dt;
		this.gameTime += dt;
		
//		if (this.timeSinceLastLog > 1) {
//			this.timeSinceLastLog = 0;
//			Gdx.app.log("Profile", "Texture Bindings: "+GL20Profiler.textureBindings);
//			Gdx.app.log("Profile", "Shader Switches: "+GL20Profiler.shaderSwitches);
//			Gdx.app.log("Profile", "Draw Calls: "+GL20Profiler.drawCalls);
//			Gdx.app.log("Profile", "Calls: "+GL20Profiler.calls);
//			Gdx.app.log("Profile", "----------------------------------------------------------------------------------------------------");
//		}
//		fpsLogger.log();
		GL20Profiler.reset();

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
