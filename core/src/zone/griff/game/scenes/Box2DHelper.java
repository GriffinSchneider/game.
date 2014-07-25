package zone.griff.game.scenes;

import zone.griff.game.pools.Vector2Pool;

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

	public static PolygonSprite polygonSpriteForFixture(Fixture fixture, TextureRegion texreg) {
		PolygonShape shape = (PolygonShape)fixture.getShape();
		Body body = fixture.getBody();
		
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

		Vector2Pool.release(tmp);

		short triangles[] = new EarClippingTriangulator().computeTriangles(vertices).toArray();
		PolygonRegion region = new PolygonRegion(texreg, vertices, triangles);

		return new PolygonSprite(region);
	}

}
