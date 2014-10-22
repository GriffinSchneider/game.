package backgrounds;

import zone.griff.game.SceneManager;
import zone.griff.game.util.PaletteManager;

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
	
	ShaderProgram shader;
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

		this.shader = new ShaderProgram(
				Gdx.files.internal("shaders/default.vert"), 
				Gdx.files.internal("shaders/starfield.frag"));

		if (!this.shader.isCompiled()) {
			Gdx.app.log("Shader",  "compile errors!\n-----\n" + this.shader.getLog() + "-----");
		}
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
		this.shader.begin();
		
		this.shader.setUniformMatrix("u_projTrans", this.idMatrix);
		this.shader.setUniformf("iGlobalTime", this.sceneManager.gameTime);
		this.shader.setUniform3fv("iResolution", this.sceneManager.gameSizeArray, 0, 3);
//		this.shader.setUniformi("previousFrame", 0);
//		this.shader.setUniformi("palatte", 0);
//		this.shader.setUniformf("palatteSize", PaletteManager.getPaletteSize());
		
		this.shader.setUniformf("parallaxX", this.parallaxX);
		this.shader.setUniformf("parallaxY", this.parallaxY);
		
		this.mesh.render(this.shader, GL20.GL_TRIANGLES);
		
		this.shader.end();
		
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
		this.shader.dispose();
		this.fbo.dispose();
		this.fboBatch.dispose();
	}
}
