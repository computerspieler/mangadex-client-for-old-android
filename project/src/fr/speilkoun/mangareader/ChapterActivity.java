package fr.speilkoun.mangareader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;

public class ChapterActivity extends Activity {
    
    Serie mSerie;

	void refreshList() {
		ListView view = (ListView) this.findViewById(R.id.serie_list);
		view.setAdapter(Database.getInstance().adapterChapter(this, mSerie.id));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        Database db = Database.getInstance();
        super.onCreate(savedInstanceState);
		
        int serie_id = getIntent().getExtras().getInt("serie_id");
        mSerie = db.getOneSerie("id = " + serie_id, "id ASC");

		this.setContentView(R.layout.main);
        this.refreshList();
    }
}
