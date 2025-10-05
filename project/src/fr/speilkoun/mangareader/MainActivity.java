package fr.speilkoun.mangareader;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.widget.TabHost;
import android.widget.TextView;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.data.SerieArray;
import fr.speilkoun.mangareader.data.SerieGroup;
import fr.speilkoun.mangareader.sources.MangaDex;
import fr.speilkoun.mangareader.utils.HTTP;

public class MainActivity extends ActivityGroup {

	static {
		Log.d("mangadex", "Starting");
		HTTP.init();
	}

	static final int ADD_MANGA_DIALOG = 0;
	static final int MANGA_SELECTION_DIALOG = 1;
	String add_manga_name = null;

	NotificationManager nm;

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
						final Serie s = (Serie) parent.getItemAtPosition(pos);

						Database.getInstance().addSerie(s);
						MainActivity.this.refreshSelectedList();
						
						Notification notification = new Notification(
							android.R.drawable.ic_notification_overlay,
							"Loading " + s.title,
							System.currentTimeMillis()
						);
						PendingIntent contentIntent = PendingIntent.getActivity(
							MainActivity.this,
							0,
							new Intent(MainActivity.this, MainActivity.class),
							0
						);
						notification.setLatestEventInfo(getApplicationContext(),
							getString(R.string.loading_serie),
							"Loading " + s.title,
							contentIntent
						);
						notification.flags |= Notification.FLAG_NO_CLEAR;
						notification.flags |= Notification.FLAG_ONGOING_EVENT;

						final int notif_id = 1;
						nm.notify(notif_id, notification);
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									MangaDex.loadChapters(s.attribute);
								} catch(Exception e) {
									Log.e("onItemClick",
										"Unable to load chapters: " + e.getClass().getCanonicalName(),
										e);
								}
								nm.cancel(notif_id);
							}
						},
						"load_" + s.id).start();
						
						dialog.dismiss();
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

	void refreshSelectedList() {
		//TabHost tabHost = (TabHost) this.findViewById(R.id.serie_group_list);

		//tabHost.get
		//ListView 
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Database.initInstance(this);
		this.setContentView(R.layout.main);
		//this.findViewById(R.layout.main)
		//	.setBackgroundDrawable(android.R.drawable.screen_background_dark);

		//this.refreshLists();

		ArrayList<SerieGroup> groups = Database.getInstance().getSerieGroups(); 
		TabHost tabHost = (TabHost) this.findViewById(R.id.serie_group_list);
		tabHost.setup(MainActivity.this.getLocalActivityManager());
		
		for(int i = 0; i < groups.size(); i ++) {
			SerieGroup group = groups.get(i);
			Intent intent = new Intent(MainActivity.this, SerieGroupTab.class);
			intent.putExtra("group", group.id);
			Log.i("Groups", group.group);
			//intent.
			tabHost.addTab(
				tabHost.newTabSpec("tab" + group.id)
					.setIndicator(group.group)
					.setContent(intent)
			);
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
