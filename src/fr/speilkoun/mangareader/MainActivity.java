package fr.speilkoun.mangareader;

import java.util.ArrayList;

import android.app.ActivityGroup;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import fr.speilkoun.mangareader.actions.GetLatestChapters;
import fr.speilkoun.mangareader.background.SearchSerie;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.data.SerieArray;
import fr.speilkoun.mangareader.data.SerieGroup;

public class MainActivity extends ActivityGroup {
	public String add_manga_name = null;
	public ArrayList<Serie> series = null;

	enum DialogState {
		ADD_MANGA_DIALOG(0) {
			@Override
			Dialog createDialog(final MainActivity activity) {
				final Dialog dialog = super.createDialog(activity);
				dialog.setTitle(R.string.add_manga);
				dialog.setContentView(R.layout.add_manga_popup);

				Button done = (Button) dialog.findViewById(R.id.done);
				done.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						activity.add_manga_name = ((TextView) dialog.findViewById(R.id.manga_name))
							.getText()
							.toString();
						activity.removeDialog(ADD_MANGA_DIALOG);

						Log.i("Done", "Got: " + activity.add_manga_name);
						activity.showDialog(MANGA_RETRIEVING_MANGA_LIST);
					}
				});

				Button cancel = (Button) dialog.findViewById(R.id.cancel);
				cancel.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				return dialog;
			}
		},

		MANGA_RETRIEVING_MANGA_LIST(1) {
			@Override
			Dialog createDialog(final MainActivity activity) {
				final ProgressDialog dialog = new ProgressDialog(activity);
				dialog.setOnCancelListener(null);
				dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				dialog.setTitle("Search: " + activity.add_manga_name);
				dialog.setMessage("Searching...\nPlease wait");

				activity.series = null;

				// Creates a backgroud task which will retrieve
				// entries, and once it's done, will call onPostExecute
				// FROM THE SAME THREAD AS THE ACTIVITY
				final AsyncTask<String, Void, ArrayList<Serie>> task = new SearchSerie(){
					@Override
					protected void onPostExecute(ArrayList<Serie> result) {
						super.onPostExecute(result);

						activity.series = result;
						activity.removeDialog(MANGA_RETRIEVING_MANGA_LIST);
						if(!isCancelled())
							activity.showDialog(MANGA_SELECTION_DIALOG);
					}
				}.execute(activity.add_manga_name);

				dialog.setOnCancelListener(new Dialog.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface d) {
						task.cancel(false);
						activity.removeDialog(MANGA_RETRIEVING_MANGA_LIST);
					}
				});
				
				return dialog;
			}
		},

		MANGA_SELECTION_DIALOG(2) {
			@Override
			Dialog createDialog(final MainActivity activity) {
				final Dialog dialog = super.createDialog(activity);
				dialog.setTitle(R.string.add_manga);
				try {
					ListView selection = new ListView(activity);
					selection.setAdapter(new SerieArray(activity, activity.series));
					selection.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
							final Serie s = (Serie) parent.getItemAtPosition(pos);
							Database.getInstance().addSerie(s);
							activity.refreshSelectedList();
							
							GetLatestChapters.execute(activity, s);
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
				return dialog;
			}
		}
		;
		
		Dialog createDialog(final MainActivity activity) {
			final Dialog dialog = new Dialog(activity);

			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface d)
				{ activity.removeDialog(id); }
			});
			
			return dialog;
		}

		public final int id;
		DialogState(int id) {
			this.id = id;
		} 
	} 

	@Override
	protected Dialog onCreateDialog(final int id) {
		for(DialogState state: DialogState.values())
			if(id == state.id)
				return state.createDialog(this);

		return null;
	}

	public void removeDialog(DialogState state) { this.removeDialog(state.id); }
	public void showDialog(DialogState state)   { this.showDialog(state.id); }

	void refreshSelectedList() {
		TabHost tabHost = (TabHost) this.findViewById(R.id.serie_group_list);
		String tag = tabHost.getCurrentTabTag();
		SerieGroupTab current_tab =
			(SerieGroupTab) getLocalActivityManager()
				.getActivity(tag);
		

		current_tab.refreshList();
	}

	void refreshSelectedListChapters() {
		TabHost tabHost = (TabHost) this.findViewById(R.id.serie_group_list);
		String tag = tabHost.getCurrentTabTag();
		SerieGroupTab current_tab =
			(SerieGroupTab) getLocalActivityManager()
				.getActivity(tag);
		

		current_tab.refreshChapters();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
			tabHost.addTab(
				tabHost.newTabSpec("tab" + group.id)
					.setIndicator(group.group)
					.setContent(intent)
			);
		}

		{
			ImageButton button = (ImageButton) this.findViewById(R.id.add);
			button.setOnClickListener(new ImageButton.OnClickListener() {
				public void onClick(View v) {
					MainActivity.this.showDialog(DialogState.ADD_MANGA_DIALOG.id);
				}
			});
		}
		{
			ImageButton button = (ImageButton) this.findViewById(R.id.refresh);
			button.setOnClickListener(new ImageButton.OnClickListener() {
				public void onClick(View v) {
					Toast.makeText(MainActivity.this, "Refreshing chapters", Toast.LENGTH_LONG)
						.show();
					MainActivity.this.refreshSelectedListChapters();
				}
			});
		}
		{
			ImageButton button = (ImageButton) this.findViewById(R.id.settings);
			button.setOnClickListener(new ImageButton.OnClickListener() {
				public void onClick(View v) {
                    Intent intent = new Intent(
                        MainActivity.this.getApplication(),
                        ParameterActivity.class
                    );
                    MainActivity.this.startActivity(intent);
				}
			});
		}
	}
}
