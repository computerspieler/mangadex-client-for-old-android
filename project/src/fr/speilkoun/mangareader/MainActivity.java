package fr.speilkoun.mangareader;

import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.sources.MangaDex;
import fr.speilkoun.mangareader.utils.HTTP;

public class MainActivity extends Activity {

	static {
		Log.d("mangadex", "Starting");
		HTTP.init();
	}

	static final int ADD_MANGA_DIALOG = 0;
	static final int MANGA_SELECTION_DIALOG = 1;

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog = new Dialog(this);

		switch(id) {
		case ADD_MANGA_DIALOG:
			dialog.setTitle(R.string.add_manga);
			dialog.setContentView(R.layout.add_manga_popup);

			Button done = (Button) dialog.findViewById(R.id.done);
			done.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					String name = ((TextView) dialog.findViewById(R.id.manga_name))
						.getText()
						.toString();
					dialog.dismiss();
					Log.i("Done", "Got: " + name);
				}
			});
			break;
		
		case MANGA_SELECTION_DIALOG:
			break;
		}
		
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Database.initInstance(this);
		this.setContentView(R.layout.main);

		try {
			MangaDex.findOrAddManga(this, "2a55e420-b5c6-47cb-b189-fb9a1a7d6ab5");
			//MangaDex.findOrAddManga(this, "13e03584-eb63-4545-af36-89040751c075");
		} catch(JSONException e) {
			Log.e("addManga", e.getLocalizedMessage());
		}
		/*
		 */
		{
			ListView view = (ListView) this.findViewById(R.id.serie_list);
			view.setAdapter(Database.getInstance().adapterSerie(this));
		}

		{
			Button button = (Button) this.findViewById(R.id.add);
			button.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					Log.i("Button", "Clicked!");
					MainActivity.this.showDialog(ADD_MANGA_DIALOG);
				}
			});
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
