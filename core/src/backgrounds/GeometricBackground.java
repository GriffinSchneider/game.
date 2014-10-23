package backgrounds;

import zone.griff.game.SceneManager;
import zone.griff.game.util.PaletteManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GeometricBackground extends ParallaxBackground {
	
	private Array<DiamondSprite> sprites;
	private ShaderProgram shader;
	private Color backgroundColor;
	
	private class DiamondSprite extends PolygonSprite {
		float size;
		PolygonSprite outlineSprite;
		DiamondSprite(PolygonRegion region, PolygonRegion outlineRegion, float size) {
			super(region);
			this.size = size;
			this.outlineSprite = new PolygonSprite(outlineRegion);
		}
		@Override
		public void setColor (Color tint) {
			super.setColor(tint);
			tint.mul(0.7f, 0.7f, 0.7f, 1);
			this.outlineSprite.setColor(tint);
		}
	}

	public GeometricBackground(SceneManager sceneManager) {
		super(sceneManager);
		
		this.shader = new ShaderProgram(
				Gdx.files.internal("shaders/default.vert"), 
				Gdx.files.internal("shaders/vertcolor.frag"));
		
		this.backgroundColor = new Color(PaletteManager.getPaletteColorAtIndex(0));
	}

	@Override
	public void resizeCamera(float viewportWidth, float viewportHeight) {
		this.sprites = new Array<DiamondSprite>();
		EarClippingTriangulator triangulator = 	new EarClippingTriangulator();
		
		int numDiamonds = 60;

		float size;
		float xPos;
		float yPos;

		for (int i = 0; i < numDiamonds; i++) {
			
			size = MathUtils.random(1f, 2f);
			xPos = MathUtils.random(0, viewportWidth+size*2);
			yPos = MathUtils.random(0, viewportHeight+size*2);
			Color color = new Color(PaletteManager.getPaletteColorAtIndex(MathUtils.random(1, 2)));
			
			PolygonRegion backgroundRegion = null;
			
			float thisSize = size;
			for (int k = 0; k < 2; k++) {
				float[] vertices = new float[4 * 2];

				vertices[0] = 0;
				vertices[1] = thisSize;

				vertices[2] = -thisSize;
				vertices[3] = 0;

				vertices[4] = 0;
				vertices[5] = -thisSize;

				vertices[6] = thisSize;
				vertices[7] = 0;

				short triangles[] = triangulator.computeTriangles(vertices).toArray();
				if (backgroundRegion == null) {
					backgroundRegion = new PolygonRegion(PaletteManager.getPaletteTextureRegion(), vertices, triangles);
				} else {
					DiamondSprite sprite = new DiamondSprite(new PolygonRegion(PaletteManager.getPaletteTextureRegion(), vertices, triangles), backgroundRegion, size);
					sprite.setPosition(xPos, yPos);
					sprite.setColor(color);
					this.sprites.add(sprite);
				}

				thisSize -= 0.08f;
			}
		}
	}

	float lastParallaxX;
	float lastParallaxY;

	@Override
	public void draw(PolygonSpriteBatch spriteBatch, OrthographicCamera camera) {
		Gdx.graphics.getGL20().glClearColor(this.backgroundColor.r, this.backgroundColor.g, this.backgroundColor.b, 1 );
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.setShader(this.shader);
		
		float deltaX = this.parallaxX - this.lastParallaxX;
		float deltaY = this.parallaxY - this.lastParallaxY;
		
		for (int i = 0; i < this.sprites.size; i ++) {
			DiamondSprite sprite = this.sprites.get(i);
			float layerParallax = ((float)(i*3)) / this.sprites.size;
			
			float cameraLeft = camera.position.x - camera.viewportWidth/2.0f - sprite.size;
			float cameraBottom = camera.position.y - camera.viewportHeight/2.0f - sprite.size;
					
			float xDistFromLeft = sprite.getX() - cameraLeft;
			float yDistFromBottom = sprite.getY() - cameraBottom;
			
			// Translate
			xDistFromLeft -= deltaX*layerParallax*40;
			yDistFromBottom -= deltaY*layerParallax*40;
			
			// Wrap
			xDistFromLeft %= (camera.viewportWidth + sprite.size*2);
			if (xDistFromLeft < 0) xDistFromLeft += camera.viewportWidth + sprite.size*2;

			yDistFromBottom %= (camera.viewportHeight + sprite.size*2);
			if (yDistFromBottom < 0) yDistFromBottom += camera.viewportHeight + sprite.size*2;

			sprite.outlineSprite.setPosition(cameraLeft + xDistFromLeft, cameraBottom + yDistFromBottom);
			sprite.outlineSprite.draw(spriteBatch);

			sprite.setPosition(cameraLeft + xDistFromLeft, cameraBottom + yDistFromBottom);
			sprite.draw(spriteBatch);
		}
		
		spriteBatch.flush();
		
		this.lastParallaxX = this.parallaxX;
		this.lastParallaxY = this.parallaxY;
	}
	
	@Override
	public void dispose() {
		this.sprites = null;
		this.shader.dispose();
	}

}
