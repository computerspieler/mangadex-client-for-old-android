package fr.speilkoun;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		Log.d("mangadex", "Starting");
		OpenSSL.init();
		Log.d("mangadex", OpenSSL.getChapterImages("a54c491c-8e4c-4e97-8873-5b79e59da210"));
	}
}
