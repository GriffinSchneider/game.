package zone.griff.game.scenes;

import static zone.griff.game.B2DVars.PPM;
import zone.griff.game.B2DVars;
import zone.griff.game.MyContactListener;
import zone.griff.game.SceneManager;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Box2dScene extends Scene {
	
	private World world;
	private MyContactListener cl;
	private Box2DDebugRenderer b2dr;
	private Body playerBody;
	private OrthographicCamera b2dCam;
	
	public Box2dScene(SceneManager sceneManager) {
		super(sceneManager);

		world = new World(new Vector2(0, -9.81f), true);
		
		cl = new MyContactListener();
		world.setContactListener(cl);
		
		b2dr = new Box2DDebugRenderer();
		
		// create platform
		BodyDef bdef = new BodyDef();
		bdef.position.set(160 / PPM, 120 / PPM);
		bdef.type = BodyType.StaticBody;
		Body body = world.createBody(bdef);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(50 / PPM, 5 / PPM);
		FixtureDef fdef = new FixtureDef();
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_GROUND;
		fdef.filter.maskBits = B2DVars.BIT_PLAYER;
		body.createFixture(fdef).setUserData("ground");
		
		// create player
		bdef.position.set(160 / PPM, 200 / PPM);
		bdef.type = BodyType.DynamicBody;
		playerBody = world.createBody(bdef);
		
		shape.setAsBox(5 / PPM, 5 / PPM);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		playerBody.createFixture(fdef).setUserData("player");
		
		// create foot sensor
		shape.setAsBox(2 / PPM, 2 / PPM, new Vector2(0, -5 / PPM), 0);
		fdef.shape = shape;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_GROUND;
		fdef.isSensor = true;
		playerBody.createFixture(fdef).setUserData("foot");
		
		// set up box2d cam
		b2dCam = new OrthographicCamera();
	}
	 
	@Override
	public void resize (int width, int height) {
		float camWidth = SceneManager.V_WIDTH / PPM;
		float camHeight = ((float)height)/((float)width)*camWidth;
		b2dCam.setToOrtho(false, camWidth, camHeight);
	}
	
	public void update(float dt) {
		world.step(dt, 6, 2);
	}
	
	@Override
	public void render() {
		b2dr.render(world, b2dCam.combined);
	}
	
	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

}
