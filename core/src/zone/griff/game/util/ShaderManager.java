package zone.griff.game.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderManager {
  private static ShaderManager instance = null;
  public static ShaderManager getInstance() {
     if(instance == null) {
        instance = new ShaderManager();
     }
     return instance;
  }
  
  public enum Shader {
  	WOBBLY_CIRCLES("wobblyCircles"),
  	OUTLINE("outline"),
  	VERT_COLOR("vertColor"),
  	STARFIELD("starfield");
  	
    private final String fileName;
    private Shader(final String fileName) {
      this.fileName = fileName;
    }
  }
  
	private HashMap<Shader, ShaderProgram> shaders;

  private ShaderManager() {
  	this.shaders = new HashMap<Shader, ShaderProgram>();
  }
  
  public static ShaderProgram get(Shader shader) {
  	ShaderProgram s = getInstance().shaders.get(shader);
  	if (s == null) {
  		s = compileShader(shader.fileName);
  		instance.shaders.put(shader, s);
  	}
  	return s;
  }

	private static ShaderProgram compileShader(String frag) {
		return compileShader("default", frag);
	}

	private static ShaderProgram compileShader(String vert, String frag) {
		String vertFile = "shaders/"+vert+".vert";
		String fragFile = "shaders/"+frag+".frag";
		ShaderProgram retVal = new ShaderProgram(Gdx.files.internal(vertFile), Gdx.files.internal(fragFile));

		Gdx.app.log("Shader",  "\ncompiled shader " + vertFile + " + " + fragFile + ":\n" + retVal.getLog() + "-----");

		if (!retVal.isCompiled()) {
			Gdx.app.log("Shader",  "SHADER FAILED COMPILATION!");
		}

		return retVal;
	}
	
	public static void dispose() {
		for (ShaderProgram shader : getInstance().shaders.values()) {
			shader.dispose();
		}
		instance.shaders = null;
		instance = null;
	}

}
