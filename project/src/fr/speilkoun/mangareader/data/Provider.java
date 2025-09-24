package fr.speilkoun.mangareader.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class Provider extends ContentProvider {
    public static final String AUTHORITY = "fr.speilkoun.MangaReader";
    final String TAG = "MRProvider";

    private static final UriMatcher sUriMatcher;

    private static final int SERIES = 1;
    private static final int SERIES_ID = 2;
    private static final int CHAPTERS = 3;
    private static final int CHAPTERS_ID = 4;

    public static final Uri CONTENT_SERIE_URI   = Uri.parse("content://" + AUTHORITY + "/series");
    public static final Uri CONTENT_CHAPTER_URI = Uri.parse("content://" + AUTHORITY + "/chapters");

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Provider.AUTHORITY, "series", SERIES);
        sUriMatcher.addURI(Provider.AUTHORITY, "series/#", SERIES_ID);
        sUriMatcher.addURI(Provider.AUTHORITY, "chapters", CHAPTERS);
        sUriMatcher.addURI(Provider.AUTHORITY, "chapters/#", CHAPTERS_ID);
    }

    class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context ctx) {
            super(ctx, "manga_reader.db", null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                "CREATE TABLE serie (" +
                    "id INTEGER PRIMARY KEY NOT NULL," +
                    "status TEXT CHECK(status IN ('FINISHED', 'RUNNING', 'UNKNOWN'))," +
                    "title  TEXT" +
                    "source  TEXT" +
                    "attribute  TEXT" +
                ");"
            );
            db.execSQL(
                "CREATE TABLE chapter (" +
                    "id INTEGER PRIMARY KEY NOT NULL," +
                    "serie_id   INTEGER ," +

                    "volume_id  INTEGER," +
                    "chapter_id INTEGER NOT NULL," +
                    "title TEXT," +
                    "publisher  TEXT," +
                    "release_date TEXT," +
                    "custom_attributes TEXT," +

                    "FOREIGN KEY(serie_id) REFERENCES serie(id)" +
                ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        }

    }

    DatabaseHelper mDBHelper;

    @Override
    public boolean onCreate() {
        mDBHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
        case SERIES: return "fr.speilkoun.mangareader/dir.Serie";
        case SERIES_ID: return "fr.speilkoun.mangareader/elt.Serie";
        case CHAPTERS: return "fr.speilkoun.mangareader/dir.Chapter";
        case CHAPTERS_ID: return "fr.speilkoun.mangareader/elt.Chapter";

        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        final String table;
        final Uri baseUri;

        switch(sUriMatcher.match(uri)) {
        case SERIES:
            baseUri = Provider.CONTENT_SERIE_URI;
            table = "serie";
            break;
        case CHAPTERS:
            baseUri = Provider.CONTENT_CHAPTER_URI;
            table = "chapter";
            break;
        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        
        Cursor cur = db.rawQuery("SELECT COUNT(*) AS count FROM " + table, null);
        cur.moveToFirst();
        int count = cur.getInt(cur.getColumnIndex("count"));
        initialValues.put("id", count);
        
        final long rowId = db.insert(table, "title", initialValues);
        final Uri outputUri = ContentUris.withAppendedId(baseUri, rowId);

        if (rowId < 0)
            throw new SQLException("Failed to insert row into " + uri);
        
        getContext().getContentResolver().notifyChange(outputUri, null);
        return outputUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
        String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int match = sUriMatcher.match(uri);

        switch (match) {
        case SERIES:
        case SERIES_ID:
            builder.setTables("serie");
            break;
        case CHAPTERS:
        case CHAPTERS_ID:
            builder.setTables("chapter");
            break;
        default:
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        if(match == SERIES_ID || match == CHAPTERS_ID)
            builder.appendWhere("id = " + uri.getPathSegments().get(1));
        
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }
}
