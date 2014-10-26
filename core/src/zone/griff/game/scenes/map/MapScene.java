package zone.griff.game.scenes.map;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;
import zone.griff.game.SceneManager;
import zone.griff.game.scenes.Scene;
import zone.griff.game.util.Box2DHelper;
import zone.griff.game.util.FloorGenerator;
import zone.griff.game.util.FloorGenerator.GeneratedRoom;
import zone.griff.game.util.FloorGenerator.RoomGraph;
import zone.griff.game.util.PaletteManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class MapScene extends Scene {
	
	private class RoomSprite {
		PolygonSprite sprite;
		PolygonSprite outlineSprite;
		GeneratedRoom room;
	}

	private RoomGraph roomGraph;
	private Array<RoomSprite> roomSprites;
	private OrthographicCamera camera;
	private ShaderProgram outlineShader;

	public MapScene(SceneManager game) {
		super(game);
		this.roomGraph = FloorGenerator.generateFloor();
		this.camera = new OrthographicCamera();
		this.constructSprites();
		
		this.outlineShader = new ShaderProgram(
				Gdx.files.internal("shaders/default.vert"), 
				Gdx.files.internal("shaders/outline.frag"));

		if (!this.outlineShader.isCompiled()) {
			Gdx.app.log("Shader",  "compile errors!\n-----\n" + this.outlineShader.getLog() + "-----");
		}
	}

	private void constructSprites() {
		this.roomSprites = new Array<RoomSprite>();

		// TODO: crazy error if I don't do this and noone else makes a world.
		World w = new World(new Vector2(), true);

		for (GeneratedRoom room : this.roomGraph.vertexSet()) {
			
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(
					((float)room.w)/2.0f - 0.1f, 
					((float)room.h)/2.0f - 0.1f,
					new Vector2(
							room.x+((float)room.w/2.0f), 
							room.y+((float)room.h/2.0f)),
					0);
			
			RoomSprite roomSprite = new RoomSprite();
			roomSprite.sprite = Box2DHelper.spriteForShape(shape, PaletteManager.getPaletteTextureRegion());
			roomSprite.room = room;
			this.roomSprites.add(roomSprite);
		}
	}

	@Override
	public void resize(int width, int height) {
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		this.camera.setToOrtho(false, camWidth, camHeight);
		this.camera.zoom = 4f;
		this.camera.translate(4, 0);
		this.camera.update();
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
		this.spriteBatch.begin();

		this.spriteBatch.setProjectionMatrix(this.camera.combined);
		
		for (RoomSprite sprite : this.roomSprites) {
			sprite.sprite.draw(this.spriteBatch);
		}
		
		this.spriteBatch.end();
	}
	
	@Override
	public void dispose() {
	}


}
