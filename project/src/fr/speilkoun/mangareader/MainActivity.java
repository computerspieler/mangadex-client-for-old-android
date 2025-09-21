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
		
		String resp = OpenSSL.getChapters("f98660a1-d2e2-461c-960d-7bd13df8b76d", 0);
		try {
			JSONTokener tokener = new JSONTokener(resp);
			JSONArray chapters = new JSONObject(tokener).getJSONArray("data");
			for(int i = 0; i < chapters.length(); i ++) {
				JSONObject chapter = chapters.getJSONObject(i);
				JSONObject attrs = chapter.getJSONObject("attributes");

				String volume = attrs.getString("volume");
				String chapter_no = attrs.getString("chapter");
				String title = attrs.getString("title");
				
				String output = "(" + chapter.getString("id") + ")";
				if(volume != null)
					output += "Volume " + volume + " ";
				output += "Chapter " + chapter_no;
				if(title != null && title != "null" && title.length() > 0)
					output += ": " + title;
				Log.d("mangadex", output);
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
		/*
		String images = OpenSSL.getChapterImages("a54c491c-8e4c-4e97-8873-5b79e59da210");
		try {
			JSONTokener tokener = new JSONTokener(images);
			String obj = new JSONObject(tokener).getString("baseUrl");
			Log.d("mangadex", obj);
		} catch(JSONException e) {
			e.printStackTrace();
		}
		*/
	}
}
