package fr.speilkoun.mangareader;

import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.sources.MangaDex;

public class MainActivity extends Activity {

	static {
		Log.d("mangadex", "Starting");
		OpenSSL.init();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Database.initInstance(this);
		this.setContentView(R.layout.main);

		try {
			MangaDex.findOrAddManga(this, "2a55e420-b5c6-47cb-b189-fb9a1a7d6ab5");
			MangaDex.findOrAddManga(this, "13e03584-eb63-4545-af36-89040751c075");
		} catch(JSONException e) {
			Log.e("addManga", e.getLocalizedMessage());
		}
		
		{
			ListView view = (ListView) this.findViewById(R.id.serie_list);
			view.setAdapter(Database.getInstance().adapterSerie(this));
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
