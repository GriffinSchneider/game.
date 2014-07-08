package zone.griff.game.scenes;

import zone.griff.game.SceneManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class ShaderScene extends Scene {
	
	ShaderProgram shader;
//	Mesh mesh;
	Mesh meshes[][];
	float centers[][][];
	Texture palatte;
	int palatteSize;

	public ShaderScene(SceneManager game) {
		super(game);
		
		this.palatteSize = 5;
		Pixmap pixmap = new Pixmap(palatteSize, 1, Format.RGBA8888);
		pixmap.drawPixel(0, 0, 0x69D2E7FF);
		pixmap.drawPixel(1, 0, 0xA7DBD8FF);
		pixmap.drawPixel(2, 0, 0xE0E4CCFF);
		pixmap.drawPixel(3, 0, 0xF38630FF);
		pixmap.drawPixel(4, 0, 0xFA6900FF);
		this.palatte = new Texture(pixmap);
		pixmap.dispose();

	}

	@Override
	public void resize(int w, int h) {
		short indices[] = new short[] {0, 1, 2, 0, 2, 3};
		float width = 2.0f / 5.0f;
		float height = 2.0f / 5.0f;

		this.meshes = new Mesh[5][5];
		this.centers = new float[5][5][2];

		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {

				float screenX = x * width - 1;
				float screenY = y * height - 1;
				float[] verts = this.squareVertices(screenX, screenY, width, height);
				Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position());
				mesh.setVertices(verts);
				mesh.setIndices(indices);
				this.meshes[x][y] = mesh;
				
				float gameWidth = this.sceneManager.gameSizeArray[0];
				float gameHeight = this.sceneManager.gameSizeArray[1];
				this.centers[x][y] = new float[]{
						x*gameWidth/5.0f+(gameWidth/5.0f)/2.0f, 
						y*gameHeight/5.0f+(gameHeight/5.0f)/2.0f
				};
			}
		}
		
		this.shader = new ShaderProgram(
				Gdx.files.internal("shaders/default.vert"), 
				Gdx.files.internal("shaders/wobblyCircles.frag"));

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

	@Override
	public void handleInput() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub
	}


	private Matrix4 idMatrix = new Matrix4().idt();
	@Override
	public void render() {
		this.palatte.bind();
		this.shader.begin();
		
		this.shader.setUniformMatrix("u_worldView", this.idMatrix);
		this.shader.setUniformf("iGlobalTime", this.sceneManager.gameTime);
//		this.shader.setUniform3fv("iResolution", this.sceneManager.gameSizeArray, 0, 3);
		this.shader.setUniformi("palatte", 0);
		this.shader.setUniformf("palatteSize", palatteSize);
		
		for (int x = 0; x < meshes.length; x++) {
			for (int y = 0; y < meshes.length; y++) {
				Mesh mesh = this.meshes[x][y];
				this.shader.setUniform2fv("cente", this.centers[x][y], 0, 2);
				mesh.render(this.shader, GL20.GL_TRIANGLES);
			}
		}
		
		this.shader.end();
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}


}
