package backgrounds;

import zone.griff.game.SceneManager;
import zone.griff.game.util.PaletteManager;
import zone.griff.game.util.ShaderManager;
import zone.griff.game.util.ShaderManager.Shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class ShaderBackground extends ParallaxBackground {
	
	Mesh mesh;
	FrameBuffer fbo;
	SpriteBatch fboBatch;

	public ShaderBackground(SceneManager sceneManager) {
		super(sceneManager);
		this.setupFBO();
	}
	
	@Override
	public void resizeCamera(float width, float height) {
		this.setupFBO();

		short indices[] = new short[] {0, 1, 2, 0, 2, 3};

		float[] verts = this.squareVertices(-1, -1, 2, 2);
		Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position());
		mesh.setVertices(verts);
		mesh.setIndices(indices);
		this.mesh = mesh;
	}

	public float[] squareVertices(float x, float y, float width, float height) {
		float left = x;
		float right = x + width;
		float top = y;
		float bottom = y + height;
		return new float[] {
				left, top, 0,
				left, bottom, 0,
				right, bottom, 0,
				right, top, 0,
		};
	}
		
	public void setupFBO() {
	   if (fbo != null) {
	  	 fbo.dispose();
	   }

	   this.fbo = new FrameBuffer(
	  		 Pixmap.Format.RGB888, 
	  		 this.sceneManager.screenWidth / 4,
	  		 this.sceneManager.screenHeight / 4,
	  		 true);
	   this.fbo.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	 
	   if(fboBatch != null) {
	  	 fboBatch.dispose();
	   }

	   this.fboBatch = new SpriteBatch();
	}
	
	
	private Matrix4 idMatrix = new Matrix4().idt();
	@Override
	public void draw(PolygonSpriteBatch spriteBatch, OrthographicCamera camera) {
		this.fbo.begin();
		
		PaletteManager.getPaletteTexture().bind();
		this.fbo.getColorBufferTexture().bind();
		
		ShaderProgram shader = ShaderManager.get(Shader.STARFIELD);
		
		shader.begin();
		
		shader.setUniformMatrix("u_projTrans", this.idMatrix);
		shader.setUniformf("iGlobalTime", this.sceneManager.gameTime);
		shader.setUniform3fv("iResolution", this.sceneManager.gameSizeArray, 0, 3);
//		this.shader.setUniformi("previousFrame", 0);
//		this.shader.setUniformi("palatte", 0);
//		this.shader.setUniformf("palatteSize", PaletteManager.getPaletteSize());
		
		shader.setUniformf("parallaxX", this.parallaxX);
		shader.setUniformf("parallaxY", this.parallaxY);
		
		this.mesh.render(shader, GL20.GL_TRIANGLES);
		
		shader.end();
		
		this.fbo.end();
		this.fboBatch.begin();
		this.fboBatch.draw(
				this.fbo.getColorBufferTexture(), 
				0, 0, 
				this.sceneManager.screenWidth, this.sceneManager.screenHeight, 
				0, 0, 1, 1);
		this.fboBatch.end();
	}

	public void dispose() {
		this.fbo.dispose();
		this.fboBatch.dispose();
	}
}
