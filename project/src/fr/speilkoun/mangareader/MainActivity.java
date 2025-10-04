package fr.speilkoun.mangareader;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
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
	protected Dialog onCreateDialog(final int id) {
		Button done, cancel;
		final Dialog dialog = new Dialog(this);

		dialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface d)
			{ MainActivity.this.removeDialog(id); }
		});

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
					MainActivity.this.removeDialog(ADD_MANGA_DIALOG);

					Log.i("Done", "Got: " + MainActivity.this.add_manga_name);
					MainActivity.this.showDialog(MANGA_SELECTION_DIALOG);
				}
			});

			cancel = (Button) dialog.findViewById(R.id.cancel);
			cancel.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			break;
		
		case MANGA_SELECTION_DIALOG:
			dialog.setTitle(R.string.add_manga);
			try {
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
		Database db = Database.getInstance();
		if(db == null) {
			Log.e("refreshList", "The database is not initialized");
			return;
		}

		ListView view = (ListView) this.findViewById(R.id.serie_list);
		assert view != null;
		view.setAdapter(db.adapterSerie(this));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Database.initInstance(this);
		this.setContentView(R.layout.main);
		//this.findViewById(R.layout.main)
		//	.setBackgroundDrawable(android.R.drawable.screen_background_dark);

		this.refreshList();
		ListView view = (ListView) this.findViewById(R.id.serie_list);
		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				Serie s = (Serie) parent.getItemAtPosition(pos);
				Intent intent = new Intent(
					MainActivity.this.getApplication(),
					ChapterActivity.class
				);
				intent.putExtra("serie_id", s.id);
				MainActivity.this.startActivity(intent);
			}
		});

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
