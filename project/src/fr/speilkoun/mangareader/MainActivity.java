package fr.speilkoun.mangareader;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.data.SerieArray;
import fr.speilkoun.mangareader.sources.MangaDex;
import fr.speilkoun.mangareader.utils.HTTP;

public class MainActivity extends Activity {

	static {
		Log.d("mangadex", "Starting");
		HTTP.init();
	}

	static final int ADD_MANGA_DIALOG = 0;
	static final int MANGA_SELECTION_DIALOG = 1;
	String add_manga_name = null;

	@Override
	protected Dialog onCreateDialog(int id) {
		Button done;
		final Dialog dialog = new Dialog(this);

		switch(id) {
		case ADD_MANGA_DIALOG:
			dialog.setTitle(R.string.add_manga);
			dialog.setContentView(R.layout.add_manga_popup);

			done = (Button) dialog.findViewById(R.id.done);
			done.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					MainActivity.this.add_manga_name = ((TextView) dialog.findViewById(R.id.manga_name))
						.getText()
						.toString();
					dialog.dismiss();

					Log.i("Done", "Got: " + MainActivity.this.add_manga_name);
					MainActivity.this.showDialog(MANGA_SELECTION_DIALOG);
				}
			});
			break;
		
		case MANGA_SELECTION_DIALOG:
			try {
				dialog.setTitle(R.string.add_manga);
				ListView selection = new ListView(this);
				selection.setAdapter(new SerieArray(this,
					MangaDex.searchManga(this, MainActivity.this.add_manga_name)
				));
				selection.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
						Serie s = (Serie) parent.getItemAtPosition(pos);
						Database.getInstance().addSerie(s);
						MainActivity.this.refreshList();
						dialog.dismiss();
						try {
							MangaDex.loadChapters(s.attribute);
						} catch(Exception e) {
							Log.e("onItemClick",
								"Unable to load chapters: " + e.getClass().getCanonicalName(),
								e);
						}
					}
				});
				dialog.setContentView(selection);
				
			} catch(Exception e) {
				Log.e("onCreateDialog",
					"Unable to create the dialog: " + e.getClass().getCanonicalName(),
					e);
				dialog.dismiss();
			}
			break;
		}
		
		return dialog;
	}

	void refreshList() {
		ListView view = (ListView) this.findViewById(R.id.serie_list);
		view.setAdapter(Database.getInstance().adapterSerie(this));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Database.initInstance(this);
		this.setContentView(R.layout.main);

		/*
		try {
			MangaDex.findOrAddManga(this, "2a55e420-b5c6-47cb-b189-fb9a1a7d6ab5");
			//MangaDex.findOrAddManga(this, "13e03584-eb63-4545-af36-89040751c075");
		} catch(Exception e) {
			Log.e("addManga", e.getLocalizedMessage());
		}
		 */
		this.refreshList();

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
		String images = Mangadex.getChapterImages("a54c491c-8e4c-4e97-8873-5b79e59da210");
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
