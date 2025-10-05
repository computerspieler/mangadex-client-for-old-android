package fr.speilkoun.mangareader;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.MangaDex;
import fr.speilkoun.mangareader.utils.HTTPException;

public class SerieGroupTab extends ListActivity {
    
    static String TAG = "SerieGroupTab";
    int group_id;

	void refreshList() {
		Database db = Database.getInstance();
		if(db == null) {
			Log.e("refreshList", "The database is not initialized");
			return;
		}

        setListAdapter(db.adapterSerie(this, this.group_id));
	}

    void refreshChapters() {
        for(int i = 0; i < this.getListAdapter().getCount(); i ++) {
            Serie s = (Serie) this.getListAdapter().getItem(i);
            try {
                MangaDex.loadChapters(s.attribute);
            } catch(HTTPException e) {
                Log.e(TAG, "Unable to refresh \"" + s.title + "\"", e);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.group_id = this.getIntent().getIntExtra("group", 0);
        this.refreshList();

        this.getListView()
            .setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                    Serie s = (Serie) parent.getItemAtPosition(pos);
                    Intent intent = new Intent(
                        SerieGroupTab.this.getApplication(),
                        ChapterActivity.class
                    );
                    intent.putExtra("serie_id", s.id);
                    SerieGroupTab.this.getParent().startActivity(intent);
                }
            });
    }
}
