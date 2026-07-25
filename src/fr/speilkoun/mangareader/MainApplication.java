package fr.speilkoun.mangareader;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import fr.speilkoun.mangareader.background.DownloadService;
import fr.speilkoun.mangareader.data.Database;

public class MainApplication extends Application {
	final String TAG = "MainApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "Starting the database");
		Database.initInstance(this);
	}

	@Override
	public void onTerminate() {
		Log.d(TAG, "Closing the download manager");
		stopService(new Intent(MainApplication.this, DownloadService.class));

		super.onTerminate();
	}
}
