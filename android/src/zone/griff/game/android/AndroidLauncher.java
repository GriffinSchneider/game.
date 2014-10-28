package zone.griff.game.android;

import java.io.IOException;

import zone.griff.game.SceneManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new SceneManager(), config);
		
		listFiles("");
	}
	
	private void listFiles(String dirFrom) {
    Resources res = getResources(); //if you are in an activity
    AssetManager am = res.getAssets();
    String fileList[] = null;
		try {
			fileList = am.list("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fileList != null)
		{   
			for ( int i = 0;i<fileList.length;i++)
			{
				Log.d("",fileList[i]); 
			}
		}
	}
}
