package zone.griff.game.scenes;

import java.util.Arrays;

import zone.griff.game.pools.Vector2Pool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Box2DHelper {
	
	public static class SpriteAndOutline {
		public PolygonSprite sprite;
		public PolygonSprite outline;
		public void setRotation (float degrees) {
			this.sprite.setRotation(degrees);
			this.outline.setRotation(degrees);
		}
		public void setPosition (float x, float y) {
			this.sprite.setPosition(x, y);
			this.outline.setPosition(x, y);
		}
		public void setOrigin (float x, float y) {
			this.sprite.setOrigin(x, y);
			this.outline.setOrigin(x, y);
		}
	}

	public static SpriteAndOutline polygonSpriteForFixture(Fixture fixture, TextureRegion texreg) {
		PolygonShape shape = (PolygonShape)fixture.getShape();
		Body body = fixture.getBody();
		return polygonSpriteForShapeOnBody(shape, body, texreg);
	}

	public static SpriteAndOutline polygonSpriteForShapeOnBody(PolygonShape shape, Body body, TextureRegion texreg) {
		SpriteAndOutline retVal = new SpriteAndOutline();
		retVal.sprite = sprite(shape, body, texreg);
		retVal.outline = outline(shape, body, texreg);
		return retVal;
	}
	
	private static PolygonSprite sprite(PolygonShape shape, Body body, TextureRegion texreg) {
		Vector2 tmp = Vector2Pool.obtain();

		int vertexCount = shape.getVertexCount();
		float[] vertices = new float[vertexCount * 2];
		for (int k = 0; k < vertexCount; k++) {
			shape.getVertex(k, tmp);
			tmp.rotate(body.getAngle() * MathUtils.radiansToDegrees);
			tmp.add(body.getPosition());
			vertices[k * 2] = tmp.x;
			vertices[k * 2 + 1] = tmp.y;
		}
		short triangles[] = new EarClippingTriangulator().computeTriangles(vertices).toArray();

		Vector2Pool.release(tmp);

		return new PolygonSprite(new PolygonRegion(texreg, vertices, triangles));
	}

	private static PolygonSprite outline(PolygonShape shape, Body body, TextureRegion texreg) {
		// https://forum.libcinder.org/topic/smooth-thick-lines-using-geometry-shader#23286000001269127

		Vector2 p0 = Vector2Pool.obtain();
		Vector2 p1 = Vector2Pool.obtain();
		Vector2 p2 = Vector2Pool.obtain();
				
		int vertexCount = shape.getVertexCount();
		float[] vertices = new float[vertexCount * 2 * 2];
		for (int k = 0; k < vertexCount; k++) {
			int index0 = (k>0 ? (k-1) : vertexCount - 1);
			int index1 = k;
			int index2 = (k+1) % vertexCount;
			
			shape.getVertex(index0, p0);
			p0.rotate(body.getAngle() * MathUtils.radiansToDegrees);
			p0.add(body.getPosition());

			shape.getVertex(index1, p1);
			p1.rotate(body.getAngle() * MathUtils.radiansToDegrees);
			p1.add(body.getPosition());

			shape.getVertex(index2, p2);
			p2.rotate(body.getAngle() * MathUtils.radiansToDegrees);
			p2.add(body.getPosition());

			////
			Vector2 tangent = new Vector2().set(p2).sub(p1).nor().add(new Vector2().set(p1).sub(p0).nor()).nor();
			Vector2 miter = new Vector2(-tangent.y, tangent.x);

			Vector2 line = new Vector2().set(p2).sub(p1);
			Vector2 normal = new Vector2().set(-line.y, line.x).nor();
			float miterLength = 0.02f / miter.dot(normal);
			
			miter.scl(miterLength);
			
			////

			vertices[k * 4] = p1.x + miter.x;
			vertices[k * 4 + 1] = p1.y + miter.y;

			vertices[k * 4 + 2] = p1.x - miter.x;
			vertices[k * 4 + 3] = p1.y - miter.y;
		}
		
		
		short[] triangles = new short[vertexCount * 2 * 3];
		int vertexCountOfOutline = vertexCount * 2;
		for (short k = 0; k < vertexCount; k++) {
			triangles[k*6] = (short) (k*2);
			triangles[k*6 + 1] = (short) (((k*2)+1)%vertexCountOfOutline);
			triangles[k*6 + 2] = (short) (((k*2)+2)%vertexCountOfOutline);
			triangles[k*6 + 3] = (short) (((k*2)+1)%vertexCountOfOutline);
			triangles[k*6 + 4] = (short) (((k*2)+3)%vertexCountOfOutline);
			triangles[k*6 + 5] = (short) (((k*2)+2)%vertexCountOfOutline);
		}
		
		Vector2Pool.release(p0);
		Vector2Pool.release(p1);
		Vector2Pool.release(p2);

//		Gdx.app.log("", "verts::"+Arrays.toString(vertices));
//		Gdx.app.log("", "tris:"+Arrays.toString(triangles));

		return new PolygonSprite(new PolygonRegion(texreg, vertices, triangles));
	}

}
