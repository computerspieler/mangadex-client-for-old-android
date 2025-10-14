package fr.speilkoun.mangareader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import fr.speilkoun.mangareader.data.Chapter;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.MangaDex;
import fr.speilkoun.mangareader.utils.HTTPException;

public class ChapterActivity extends Activity {
    static String TAG = "ChapterActivity";
    Serie mSerie;

	void refreshList() {
		ListView view = (ListView) this.findViewById(R.id.chapters_list);
		view.setAdapter(Database.getInstance().adapterChapter(this, mSerie.id));
	}

    void refreshChapters() {
        try {
            MangaDex.loadChapters(mSerie.attribute);
            this.refreshList();
        } catch (HTTPException e) {
            Toast.makeText(this, "Could not refresh", Toast.LENGTH_LONG)
                .show();
            Log.e(TAG, "Could not refresh: ", e);
        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        Database db = Database.getInstance();
        super.onCreate(savedInstanceState);
		
        int serie_id = getIntent().getExtras().getInt("serie_id");
        mSerie = db.getOneSerie("id = " + serie_id, "id ASC");

		this.setContentView(R.layout.chapters);
        this.refreshList();

        Button button = (Button) this.findViewById(R.id.refresh);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChapterActivity.this.refreshChapters();
            }
        });

        ListView view = (ListView) this.findViewById(R.id.chapters_list);
        view.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                final Chapter c = (Chapter) parent.getItemAtPosition(pos);
                if(!Database.getInstance().hasPages(c.id)) {
                    try {
                        MangaDex.downloadChapter(ChapterActivity.this, c);
                    } catch (HTTPException e) {
                        Log.e(TAG, "Could not download the chapter", e);
                    }
                }

                Intent intent = new Intent(
                    ChapterActivity.this.getApplication(),
                    PageActivity.class
                );
                intent.putExtra("chapter_id", c.id);
                ChapterActivity.this.startActivity(intent);
            }
        });
    }
}
