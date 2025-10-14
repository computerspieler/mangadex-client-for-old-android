package fr.speilkoun.mangareader;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Page;

public class PageActivity extends Activity {
    static final String TAG = "PageActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        int chapter_id = getIntent().getExtras().getInt("chapter_id");
        ArrayList<Page> pages = Database.getInstance().getPages(chapter_id);
        if(pages.size() == 0) {
            Toast.makeText(
                this.getApplicationContext(),
                R.string.empty_chapter_toast,
                Toast.LENGTH_SHORT
            ).show();
            this.finish();
            return;
        }

        Log.i(TAG, "We survived ! " + pages.size());
    }		
}
