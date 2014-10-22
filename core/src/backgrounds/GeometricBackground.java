package backgrounds;

import zone.griff.game.SceneManager;
import zone.griff.game.pools.Vector2Pool;
import zone.griff.game.util.PaletteManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GeometricBackground extends ParallaxBackground {
	
	private Array<DiamondSprite> sprites;
	
	private class DiamondSprite extends PolygonSprite {
		float size;
		DiamondSprite(PolygonRegion region, float size) {
			super(region);
			this.size = size;
		}
	}

	public GeometricBackground(SceneManager sceneManager) {
		super(sceneManager);
	}

	@Override
	public void resizeCamera(float viewportWidth, float viewportHeight) {
		this.sprites = new Array<DiamondSprite>();
	  TextureRegion texreg = new TextureRegion(PaletteManager.getPalette(),0,0,PaletteManager.getPaletteSize(),1);
		EarClippingTriangulator triangulator = 	new EarClippingTriangulator();
		
		int numDiamonds = 20;

		float size;

		for (int i = 0; i < numDiamonds; i++) {
			
			size = MathUtils.random(2.5f, 5f);
			
			float[] vertices = new float[4 * 2];
			
			vertices[0] = 0;
			vertices[1] = size;
			
			vertices[2] = -size;
			vertices[3] = 0;
			
			vertices[4] = 0;
			vertices[5] = -size;
			
			vertices[6] = size;
			vertices[7] = 0;
			
			short triangles[] = triangulator.computeTriangles(vertices).toArray();
			DiamondSprite sprite = new DiamondSprite(new PolygonRegion(texreg, vertices, triangles), size);
			
			sprite.setPosition(
					MathUtils.random(0, viewportWidth+size*2),
					MathUtils.random(0, viewportHeight+size*2));

			this.sprites.add(sprite);
		}
	}
	
	float lastParallaxX;
	float lastParallaxY;

	@Override
	public void draw(PolygonSpriteBatch spriteBatch, OrthographicCamera camera) {
		spriteBatch.setShader(null);
		
		float deltaX = this.parallaxX - this.lastParallaxX;
		float deltaY = this.parallaxY - this.lastParallaxY;
		
		for (int i = 0; i < this.sprites.size; i ++) {
			DiamondSprite sprite = this.sprites.get(i);
			float layerParallax = (i*3) / this.sprites.size;
			
			float cameraLeft = camera.position.x - camera.viewportWidth/2.0f - sprite.size;
			float cameraBottom = camera.position.y - camera.viewportHeight/2.0f - sprite.size;
					
			float xDistFromLeft = sprite.getX() - cameraLeft;
			float yDistFromBottom = sprite.getY() - cameraBottom;
			
			// Translate
			xDistFromLeft -= deltaX*layerParallax*300;
			yDistFromBottom -= deltaY*layerParallax*4;
			
			// Wrap
			xDistFromLeft %= (camera.viewportWidth + sprite.size*2);
			if (xDistFromLeft < 0) xDistFromLeft += camera.viewportWidth + sprite.size*2;

			yDistFromBottom %= (camera.viewportHeight + sprite.size*2);
			if (yDistFromBottom < 0) yDistFromBottom += camera.viewportHeight + sprite.size*2;

			sprite.setPosition(cameraLeft + xDistFromLeft, cameraBottom + yDistFromBottom);
			
			sprite.draw(spriteBatch);
		}
		
		this.lastParallaxX = this.parallaxX;
		this.lastParallaxY = this.parallaxY;
	}

}
