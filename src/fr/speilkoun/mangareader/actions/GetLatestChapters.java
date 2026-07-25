package fr.speilkoun.mangareader.actions;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import fr.speilkoun.mangareader.R;
import fr.speilkoun.mangareader.background.DownloadService;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.sources.Sources;
import fr.speilkoun.mangareader.utils.HTTPException;

public class GetLatestChapters implements Action {
	public static String TAG = "GetLatestChapters";

	@Override
	public void run(IntentService _service, Intent intent) {
		DownloadService service = (DownloadService) _service;
		Serie s = intent.getParcelableExtra("serie");

		Log.d(TAG, "Processing "+s.title);
		service.notification.setLatestEventInfo(
			service.getApplicationContext(),
			service.getString(R.string.loading_serie),
			"Loading " + s.title,
			service.contentIntent
		);
		service.nm.notify(1, service.notification);

		try {
			Sources.get(s.source).loadChapters(s);
		} catch (HTTPException e) {
			Log.e(TAG, "Unable to retrieve the latest chapters", e);
		}
	}
	
	public static void execute(Context ctx, Serie s) {
		Intent intent = new Intent(ctx, DownloadService.class);
		intent.setAction(GetLatestChapters.class.getName());
		intent.putExtra("serie", s);
		ctx.startService(intent);
	}
}
