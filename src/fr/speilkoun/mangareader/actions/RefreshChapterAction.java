package fr.speilkoun.mangareader.actions;

import android.util.Log;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.MangaDex;
import fr.speilkoun.mangareader.utils.HTTPException;

public class RefreshChapterAction extends Action {
    static String TAG = "RefreshChapterAction";
    Serie serie;
    
    public RefreshChapterAction(Serie s) {
        this.serie = s;
    }

    @Override
    public void process() {
        try {
            MangaDex.loadChapters(serie.attribute);
            onSuccess(serie);
        } catch(HTTPException e) {
            Log.e(TAG, "Unable to refresh \"" + serie.title + "\"", e);
            onFailure(serie);
        }
    }
 
    public void onSuccess(Serie s) {}
    public void onFailure(Serie s) {}
}
