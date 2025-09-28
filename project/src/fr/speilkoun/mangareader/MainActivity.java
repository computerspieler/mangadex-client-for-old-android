package fr.speilkoun.mangareader;

import org.json.JSONException;

import android.app.Activity;
import android.app.ListActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.MangaDex;

public class MainActivity extends Activity {

	static {
		Log.d("mangadex", "Starting");
		OpenSSL.init();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Database.initInstance(getApplicationContext());
		this.setContentView(R.layout.main);
		
		String manga_id = "2a55e420-b5c6-47cb-b189-fb9a1a7d6ab5";
		try {
			Serie s = MangaDex.addManga(getApplicationContext(), manga_id);
			if(s.cover_image_id != null) {
				String cover_path = Database.getInstance()
					.getFilePath(s.cover_image_id);
				Log.i("addManga", "Loading " + cover_path);

				ImageView v = (ImageView) this.findViewById(R.id.img);
				Bitmap image = BitmapFactory.decodeFile(cover_path);
				if(image != null && v != null)
					v.setImageBitmap(image);
				else if(image != null)
					Log.e("addManga", "Unable to load the view");
				else
					Log.e("addManga", "Unable to load the image");

			} else 
				Log.i("addManga", "No covers");
		} catch(JSONException e) {
			Log.e("addManga", e.getLocalizedMessage());
		}

		Database.getInstance().logChapters();
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
