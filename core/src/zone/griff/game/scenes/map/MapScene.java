package zone.griff.game.scenes.map;

import static zone.griff.game.scenes.box2d.B2DVars.PPM;
import zone.griff.game.SceneManager;
import zone.griff.game.levelgeneration.FloorGenerator.RoomGraph;
import zone.griff.game.levelgeneration.GeneratedDoor;
import zone.griff.game.levelgeneration.GeneratedDoor.DoorDirection;
import zone.griff.game.levelgeneration.GeneratedRoom;
import zone.griff.game.levelgeneration.RoomGrowingFloorGenerator;
import zone.griff.game.scenes.Scene;
import zone.griff.game.util.Box2DHelper;
import zone.griff.game.util.PaletteManager;
import zone.griff.game.util.ShaderManager;
import zone.griff.game.util.ShaderManager.Shader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class MapScene extends Scene {
	
	private class RoomSprite {
		PolygonSprite sprite;
		GeneratedRoom room;
	}

	private RoomGraph roomGraph;

	private Array<RoomSprite> roomSprites;
	private Array<PolygonSprite> doorSprites;

	private OrthographicCamera camera;

	public MapScene(SceneManager game) {
		super(game);
		this.roomGraph = RoomGrowingFloorGenerator.getInstance().generateFloor();
		this.camera = new OrthographicCamera();
		this.constructSprites();
	}

	private void constructSprites() {
		this.roomSprites = new Array<RoomSprite>();
		this.doorSprites = new Array<PolygonSprite>();

		// TODO: crazy error if I don't do this and noone else makes a world.
		World w = new World(new Vector2(), true);

		for (GeneratedRoom room : this.roomGraph.vertexSet()) {
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(
					room.w()/2.0f - 0.1f,
					room.h()/2.0f - 0.1f,
					new Vector2(
							room.x() + room.w()/2.0f, 
							room.y() + room.h()/2.0f),
					0);
			
			RoomSprite roomSprite = new RoomSprite();
			roomSprite.sprite = Box2DHelper.spriteForShape(shape, PaletteManager.getPaletteTextureRegion());
			roomSprite.room = room;
			int c = PaletteManager.getPaletteColorAtIndex(MathUtils.random(PaletteManager.getPaletteSize() - 1));
			roomSprite.sprite.setColor(new Color(c));
			this.roomSprites.add(roomSprite);
		}
		
		for (GeneratedDoor door : this.roomGraph.edgeSet()) {
			float doorSize = 0.2f;
			PolygonShape shape = new PolygonShape();
			Vector2 center;
			if (door.dir == DoorDirection.DOOR_RIGHT) {
				center = new Vector2(door.x + 1.0f, door.y + 0.5f);
			} else {
				center = new Vector2(door.x + 0.5f, door.y + 1.0f);
			}

			shape.setAsBox(doorSize, doorSize, center, 0);
			
			PolygonSprite roomSprite = Box2DHelper.spriteForShape(shape, PaletteManager.getPaletteTextureRegion());
			roomSprite.setColor(Color.WHITE);
			this.doorSprites.add(roomSprite);
		}
	}

	@Override
	public void resize(int width, int height) {
		this.camera.zoom = 2.5f;
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		this.camera.setToOrtho(false, camWidth, camHeight);
		this.camera.position.set(14, 6.5f, 1);
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
		this.spriteBatch.setShader(ShaderManager.get(Shader.VERT_COLOR));
		
		for (RoomSprite sprite : this.roomSprites) {
			sprite.sprite.draw(this.spriteBatch);
		}

		for (PolygonSprite sprite : this.doorSprites) {
			sprite.draw(this.spriteBatch);
		}
		
		this.spriteBatch.end();
	}
	
	@Override
	public void dispose() {
	}


}
