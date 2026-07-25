package fr.speilkoun.mangareader.background;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.Sources;

public class SearchSerie extends AsyncTask<String, Void, ArrayList<Serie>> {
	// TODO: Split results per sources
	final static String TAG = "SearchSerie";

	@Override
	public ArrayList<Serie> doInBackground(String... searches) {
		ArrayList<Serie> output = new ArrayList<Serie>();
		for(int i = 0; i < searches.length; i ++) {
			for(String source_name: Sources.sources()) {
				if(isCancelled())
					break;
				try {
					output.addAll(Sources.get(source_name).searchManga(searches[i]));
				} catch (Exception e) {
					Log.e(TAG, "Unable to find the output", e);
				}
			}
		}
		return output;
    }
}
