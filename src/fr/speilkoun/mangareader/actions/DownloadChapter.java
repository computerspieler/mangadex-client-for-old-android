package fr.speilkoun.mangareader.actions;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import fr.speilkoun.mangareader.background.DownloadService;
import fr.speilkoun.mangareader.data.Chapter;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.Sources;
import fr.speilkoun.mangareader.utils.HTTPException;

public class DownloadChapter implements Action {
	static String TAG = "DownloadChapter";

	@Override
	public void run(IntentService _service, Intent intent) {		
		DownloadService service = (DownloadService) _service;
		Chapter c = intent.getParcelableExtra("chapter");

        try {
			Serie s = Database.getInstance().getOneSerie("id="+c.serie_id, "id ASC");
            Sources.get(s.source).downloadChapter(c);
        } catch (HTTPException e) {
            Log.e(TAG, "Could not download the chapter", e);

			Toast.makeText(
				service.getApplicationContext(), 
				"Could not download this chapter",
				Toast.LENGTH_LONG
			).show();
		}
	}
	
	public static void execute(Context ctx, Chapter c) {
		Intent intent = new Intent(ctx, DownloadService.class);
		intent.setAction(DownloadChapter.class.getName());
		intent.putExtra("chapter", c);
		ctx.startService(intent);
	}
}
