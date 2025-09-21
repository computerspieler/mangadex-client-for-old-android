package fr.speilkoun.mangareader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import fr.speilkoun.mangareader.R;

import org.json.*;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		Log.d("mangadex", "Starting");
		OpenSSL.init();
		String images = OpenSSL.getChapterImages("a54c491c-8e4c-4e97-8873-5b79e59da210");
		try {
			JSONTokener tokener = new JSONTokener(images);
			String obj = new JSONObject(tokener).getString("baseUrl");
			Log.d("mangadex", obj);
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
}
