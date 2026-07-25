package fr.speilkoun.mangareader.background;

import java.util.HashMap;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import fr.speilkoun.mangareader.MainActivity;
import fr.speilkoun.mangareader.R;
import fr.speilkoun.mangareader.actions.Action;
import fr.speilkoun.mangareader.actions.GetLatestChapters;
import fr.speilkoun.mangareader.actions.DownloadChapter;
import fr.speilkoun.mangareader.utils.HTTP;

public class DownloadService extends IntentService {
	final static String TAG = "DownloadService";

	public Notification notification;
	public NotificationManager nm;
	public PendingIntent contentIntent;

	private static final HashMap<String, Action> ACTIONS = new HashMap<String, Action>();
	static void addAction(Action action) {
		ACTIONS.put(action.getClass().getName(), action);
	}
	static {
		addAction(new GetLatestChapters());
		addAction(new DownloadChapter());
	}

	public DownloadService() { super(TAG); }

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(TAG, "Starting HTTP");
		HTTP.init();

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(
			android.R.drawable.ic_notification_overlay,
			"",
			System.currentTimeMillis()
		);
		contentIntent = PendingIntent.getActivity(
			DownloadService.this,
			0,
			new Intent(DownloadService.this, MainActivity.class),
			0
		);
		notification.setLatestEventInfo(getApplicationContext(),
			getString(R.string.loading_serie),
			"Loading",
			contentIntent
		);

		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		nm.notify(1, notification);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Handling " + intent.getAction());
		Action a = ACTIONS.get(intent.getAction());
		if(a != null)
			a.run(this, intent);
	}

	@Override
	public void onDestroy() {
		nm.cancelAll();
	}
}
