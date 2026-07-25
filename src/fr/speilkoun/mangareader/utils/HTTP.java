package fr.speilkoun.mangareader.utils;

import android.util.Log;
import fr.speilkoun.mangareader.data.Database;

public class HTTP {
    static String TAG = "HTTP";

    public static native void init();
    static native void rawDownloadFile(
        String output_path,
        String url
    )
        throws HTTPException;
    public static native String getJSON(
        String url
    )
        throws HTTPException;

    public static long downloadFileAndAddToDatabase(
        String filename,
        String url
    )
        throws HTTPException
    {
        Database db = Database.getInstance();
        String output_path = db.getFileFullPath(filename);
        Log.i(TAG, "Looking for " + output_path);
        Long idx = db.findFile(output_path);
        
        if(idx == null) {
            Log.i(TAG, "Downloading " + url + " to " + output_path);
            rawDownloadFile(output_path, url);
            idx = db.addFile(output_path);
        }
        return idx;
    }

	static {
        System.loadLibrary("http-parser");
    }
}
