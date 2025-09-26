package fr.speilkoun.mangareader;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import fr.speilkoun.mangareader.data.Provider;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.MangaDex;

public class MainActivity extends ListActivity {

	static {
		Log.d("mangadex", "Starting");
		OpenSSL.init();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String manga_id = "2a55e420-b5c6-47cb-b189-fb9a1a7d6ab5";
		Serie s = new Serie(
			null,
			"335km",
			"mangadex",
			manga_id
		);
		getContentResolver().insert(Provider.CONTENT_SERIE_URI, s.getContentValues());
		
		MangaDex.loadChapters(getContentResolver(), manga_id);
		{
			Cursor cur = this.managedQuery(Provider.CONTENT_CHAPTER_URI,
				new String[] { "serie_id", "chapter_id" },
				null,
				null,
				null
			);
			cur.moveToPosition(-1);

			while(cur.moveToNext()) {
				Log.i("MANGADEX's Chapters",
				"Serie: " + cur.getString(cur.getColumnIndex("serie_id")) + ", " +
				"Chapters: " + cur.getString(cur.getColumnIndex("chapter_id"))
				);
			}
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
