package zone.griff.game.util;

import java.util.ArrayList;
import java.util.HashMap;

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
	
	
	public static boolean isPlayerBody(Body body) {
		Object ud = body.getUserData();
		return ud != null && ud instanceof String && ((String)ud).equals("player");
	}
	
	
	
	public static SpriteAndOutline polygonSpriteForBody(Body body, TextureRegion texreg) {

		ArrayList<PolygonSprite> sprites = new ArrayList<PolygonSprite>();
		for (Fixture fixture : body.getFixtureList()) {
			if (!fixture.isSensor()) {
				sprites.add(spriteForFixture(fixture, texreg));
			}
		}

		PolygonSprite outline = outlineForBody(body, texreg);

		return new SpriteAndOutline(sprites, outline);
	}

	private static PolygonSprite spriteForFixture(Fixture fixture, TextureRegion texreg) {
		Body body = fixture.getBody();
		PolygonShape shape = (PolygonShape)fixture.getShape();

		Vector2 tmp = Vector2Pool.obtain();

		// Copy the shape's vertices into an array, adjusting them into world space.
		int vertexCount = shape.getVertexCount();
		float[] vertices = new float[vertexCount * 2];
		for (int k = 0; k < vertexCount; k++) {
			shape.getVertex(k, tmp);
			adjustPointForBody(tmp, body);
			vertices[k * 2] = tmp.x;
			vertices[k * 2 + 1] = tmp.y;
		}

		short triangles[] = new EarClippingTriangulator().computeTriangles(vertices).toArray();

		Vector2Pool.release(tmp);
		
//		Gdx.app.log("", "sprite verts:"+Arrays.toString(vertices));
//		Gdx.app.log("", "sprite tris:"+Arrays.toString(triangles));

		return new PolygonSprite(new PolygonRegion(texreg, vertices, triangles));
	}

	private static PolygonSprite outlineForBody(Body body, TextureRegion texreg) {

		Vector2 p0 = Vector2Pool.obtain();
		Vector2 p1 = Vector2Pool.obtain();
		Vector2 p2 = Vector2Pool.obtain();
		
		ArrayList<PolygonShape> shapes = new ArrayList<PolygonShape>();
		for (Fixture fixture : body.getFixtureList()) {
			if (!fixture.isSensor()) {
				shapes.add((PolygonShape) fixture.getShape());
			}
		}
				
		ArrayList<PointNode> verts = thing(shapes, body);

		int vertexCount = verts.size();
		float[] vertices = new float[vertexCount * 2 * 2];
		
		for (int k = 0; k < vertexCount; k++) {
			// Calculate indices
			int index0 = (k>0 ? (k-1) : vertexCount - 1);
			int index1 = k;
			int index2 = (k+1) % vertexCount;
			
			// Index verts to get vertices
			PointNode pp0 = verts.get(index0);
			p0.set(pp0.x, pp0.y);
			adjustPointForBody(p0, body);

			PointNode pp1 = verts.get(index1);
			p1.set(pp1.x, pp1.y);
			adjustPointForBody(p1, body);

			PointNode pp2 = verts.get(index2);
			p2.set(pp2.x, pp2.y);
			adjustPointForBody(p2, body);

			// Calculate the 2 outline line vertices that we're going to make for this vertex.
			// https://forum.libcinder.org/topic/smooth-thick-lines-using-geometry-shader#23286000001269127
			Vector2 tangent = new Vector2().set(p2).sub(p1).nor().add(new Vector2().set(p1).sub(p0).nor()).nor();
			Vector2 miter = new Vector2(-tangent.y, tangent.x);
			Vector2 line = new Vector2().set(p2).sub(p1);
			Vector2 normal = new Vector2().set(-line.y, line.x).nor();
			float miterLength = 0.03f / miter.dot(normal);
			miter.scl(miterLength);

			// Fill the array
			vertices[k * 4 + 0] = p1.x + miter.x;
			vertices[k * 4 + 1] = p1.y + miter.y;
			vertices[k * 4 + 2] = p1.x - miter.x;
			vertices[k * 4 + 3] = p1.y - miter.y;
		}
		
		short[] triangles = new short[vertexCount * 2 * 3];
		int vertexCountOfOutline = vertexCount * 2;
		for (short k = 0; k < vertexCount; k++) {
			triangles[k * 6 + 0] = (short) (((k*2)+0) % vertexCountOfOutline);
			triangles[k * 6 + 1] = (short) (((k*2)+1) % vertexCountOfOutline);
			triangles[k * 6 + 2] = (short) (((k*2)+2) % vertexCountOfOutline);
			triangles[k * 6 + 3] = (short) (((k*2)+1) % vertexCountOfOutline);
			triangles[k * 6 + 4] = (short) (((k*2)+3) % vertexCountOfOutline);
			triangles[k * 6 + 5] = (short) (((k*2)+2) % vertexCountOfOutline);
		}
		
		Vector2Pool.release(p0);
		Vector2Pool.release(p1);
		Vector2Pool.release(p2);

//		Gdx.app.log("", "outline verts:"+Arrays.toString(vertices));
//		Gdx.app.log("", "outline tris:"+Arrays.toString(triangles));

		return new PolygonSprite(new PolygonRegion(texreg, vertices, triangles));
	}
	
	private static void adjustPointForBody(Vector2 point, Body body) {
		point.rotate(body.getAngle() * MathUtils.radiansToDegrees);
		point.add(body.getPosition());
	}
	
	// General algorithm:
	// - Make a graph where there's a node for every vertex in all the shapes
	// - Make a directed edge from node A to node B iff B comes directly after A
	//   in the vertices array of one of the shapes
	// - Remove all edges in 2-node-cycles, i.e. A has edge to B and B has edge to A,
	//   so remove both edges.
	// - If all the shapes form a contiguous polygon with shared vertices, then we're 
	//   left with 1 big cycle in  the graph. Read that cycle out and return it in an array.
	private static ArrayList<PointNode> thing(ArrayList<PolygonShape> shapes, Body body) {
		Vector2 point = Vector2Pool.obtain();
		Vector2 nextPoint = Vector2Pool.obtain();
		
		HashMap<String, PointNode> map = new HashMap<String, PointNode>();
		
		for (PolygonShape shape : shapes) {
			int vertexCount = shape.getVertexCount();
			for (int index = 0; index < vertexCount; index++) {
				int nextIndex = (index+1) % vertexCount;

				shape.getVertex(index, point);
				shape.getVertex(nextIndex, nextPoint);

				PointNode node = map.get(PointNode.hashKey(point.x, point.y));
				if (node == null) {
					node = new PointNode(point.x, point.y); 
					map.put(node.hashKey(), node);
				}

				PointNode nextNode = map.get(PointNode.hashKey(nextPoint.x, nextPoint.y));
				if (nextNode == null) { 
					nextNode = new PointNode(nextPoint.x, nextPoint.y); 
					map.put(nextNode.hashKey(), nextNode);
				}

				if (nextNode.nexts.contains(node)) {
					nextNode.nexts.remove(node);
				} else {
					node.nexts.add(nextNode);
				}
			}
		}
		
		PointNode node = map.entrySet().iterator().next().getValue();
		if (node.nexts.size() == 0) {
			node = map.entrySet().iterator().next().getValue();
		}
		PointNode firstNode = node;
		ArrayList<PointNode> retVal = new ArrayList<PointNode>();
		int idx = 0;
		while (true) {
			Gdx.app.log("", "" + idx + "::  " + node.x + ", " + node.y + " | " + node.nexts);
			retVal.add(node);
			if (node.nexts.size() == 0) {
				break;
			}
			node = node.nexts.get(0);
			if (node == firstNode) {
				break;
			}
			idx++;
		}
		
		Vector2Pool.release(point);
		Vector2Pool.release(nextPoint);
		return retVal;
	}
	

	private static class PointNode extends Object {
		public float x;
		public float y;

		// Nodes that this node has an edge pointed to
		public ArrayList<PointNode> nexts;
		
		public PointNode(float x, float y) {
			this.x = x;
			this.y = y;
			this.nexts = new ArrayList<PointNode>();
		}

		public String hashKey() {
			return hashKey(x, y);
		}
		public static String hashKey(float x, float y) {
			return Float.toHexString(x) + Float.toHexString(y);
		}
	}

}
