package fr.speilkoun.mangareader.actions;

import android.content.Context;
import android.util.Log;
import fr.speilkoun.mangareader.data.Chapter;
import fr.speilkoun.mangareader.sources.MangaDex;
import fr.speilkoun.mangareader.utils.HTTPException;

public abstract class DownloadChapterAction extends Action {
    static String TAG = "DownloadChapterAction";

    Context context;
    Chapter chapter;
    
    public DownloadChapterAction(Context ctx, Chapter c) {
        context = ctx;
        chapter = c;
    }

    @Override
    public void process() {
        try {
            MangaDex.downloadChapter(context, chapter);
            onSuccess(chapter);
        } catch (HTTPException e) {
            Log.e(TAG, "Could not download the chapter", e);
            onFailure(chapter);
        }
    }
    
    public abstract void onSuccess(Chapter c);
    public abstract void onFailure(Chapter c);
}
