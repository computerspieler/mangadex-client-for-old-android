package fr.speilkoun.mangareader.utils;

import android.content.Context;
import android.util.Log;
import fr.speilkoun.mangareader.data.Database;

public class HTTP {
    static String TAG = "HTTP";

    public static native void init();
    static native boolean rawDownloadFile(
        String output_path,
        String domain,
        String path
    );
    public static native String getJSON(
        String domain,
        String path
    );

    public static long downloadFileAndAddToDatabase(
        Context ctx,
        String filename,
        String domain,
        String path
    )
        throws Exception
    {
        Database db = Database.getInstance();
        String output_path = ctx.getFileStreamPath(filename).getAbsolutePath();
        Log.i(TAG, "Looking for " + output_path);
        Long idx = db.findFile(output_path);
        
        if(idx == null) {
            Log.i(TAG, "Downloading " + domain + path + " to " + output_path);
            if(!rawDownloadFile(output_path, domain, path))
                throw new Exception("Unable to download " + output_path);
            idx = db.addFile(output_path);
        }
        return idx;
    }

	static {
        System.loadLibrary("http-parser");
    }
}
