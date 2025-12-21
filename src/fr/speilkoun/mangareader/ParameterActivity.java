package fr.speilkoun.mangareader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;

public class ParameterActivity extends Activity {
    static String TAG = "ParameterActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(new LinearLayout(this));

        Log.d(TAG, "State: " + Environment.getExternalStorageState());
        Log.d(TAG, "Root: " + Environment.getRootDirectory().getAbsolutePath());
        Log.d(TAG, "Data: " + Environment.getDataDirectory().getAbsolutePath());
        Log.d(TAG, "Cache: " + Environment.getDownloadCacheDirectory().getAbsolutePath());
        Log.d(TAG, "External sotrage: " + Environment.getExternalStorageDirectory().getAbsolutePath());
    }
}
