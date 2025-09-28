package fr.speilkoun.mangareader.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
    private static String TAG = "Database";

    private static Database mInstance = null;
    public static Database getInstance()
    { return mInstance; }

    public static void initInstance(Context ctx) {
        if(mInstance == null)
            mInstance = new Database(ctx);
    }

    SQLiteDatabase mDB;
    private Database(Context ctx) {
        String path = ctx.getFileStreamPath("manga_reader.db").getAbsolutePath();
        mDB = SQLiteDatabase.openDatabase(
            path,
            null,
            SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READWRITE
        );
        Log.i(TAG, "Database stored at " + path);
        //mDB = SQLiteDatabase.create(null);

        mDB.execSQL(
            "CREATE TABLE files (" +
                "id INTEGER PRIMARY KEY NOT NULL," +
                "path TEXT NOT NULL UNIQUE" +
            ");");
        mDB.execSQL(
            "CREATE TABLE serie (" +
                "id INTEGER PRIMARY KEY NOT NULL," +
                "status TEXT CHECK(status IN ('FINISHED', 'RUNNING', 'UNKNOWN'))," +
                "title TEXT," +
                "source TEXT," +
                "cover_image_id INTEGER," +
                "attribute TEXT," +

                "FOREIGN KEY(cover_image_id) REFERENCES files(id)" +
            ");");
        mDB.execSQL("CREATE TABLE chapter (" +
                "id INTEGER PRIMARY KEY NOT NULL," +
                "serie_id INTEGER," +

                "volume_id  INTEGER," +
                "chapter_id INTEGER NOT NULL," +
                "title TEXT," +
                "publisher  TEXT," +
                "release_date TEXT," +
                "custom_attributes TEXT," +

                "FOREIGN KEY(serie_id) REFERENCES serie(id)" +
            ");");
        mDB.execSQL("CREATE TABLE pages (" +
                "chapter_id INTEGER NOT NULL," +
                "file_id INTEGER," +
                "page INTEGER NOT NULL," +

                "FOREIGN KEY(chapter_id) REFERENCES chapter(id)" +
                "FOREIGN KEY(file_id) REFERENCES files(id)" +
            ");"
        );
    }

    public void addSerie(Serie s) {
        mDB.insertOrThrow("serie", null, s.getContentValues());
    }

    public Serie getOneSerie(String filter, String order) {
        Cursor cur = mDB.query("serie",
            new String[] {
                "id",
                "title",
                "cover_image_id",
                "source",
                "attribute"
            },
            filter,
            null,
            null,
            null,
            order
        );

        if(cur.getCount() < 1) {
            Log.e(Database.TAG, "Unable to find a serie");
            return null;
        }
        
        cur.moveToPosition(0);
        Serie s = new Serie(
            cur.getInt(cur.getColumnIndex("id")),
            cur.getString(cur.getColumnIndex("title")),
            cur.getLong(cur.getColumnIndex("cover_image_id")),
            cur.getString(cur.getColumnIndex("source")),
            cur.getString(cur.getColumnIndex("attribute"))
        );
        cur.close();
        return s;
    }

    public Integer getChapterCount(int manga_id) {
        Cursor cur = mDB.query("chapter",
            new String[] { "COUNT(*) AS count" },
            "serie_id = "+manga_id,
            null,
            null,
            null,
            null
        );

        if(cur.getCount() < 1) {
            Log.w(TAG, "Unable to find a serie with " + manga_id + " as an id");
            return 0;
        }
        cur.moveToPosition(0);
        int output = cur.getInt(cur.getColumnIndex("count"));
        cur.close();
        return output;
    }

    public void addChapter(Chapter c) {
        mDB.insertOrThrow("chapter", null, c.getContentValues());
    }

    public long getFilesCount() {
        Cursor cur = mDB.query("files",
            new String[] { "COUNT(*) AS count" },
            null,
            null,
            null,
            null,
            null
        );

        if(cur.getCount() < 1) {
            return 0;
        }
        cur.moveToPosition(0);
        int output = cur.getInt(cur.getColumnIndex("count"));
        cur.close();
        return output;
    }

    public long addFile(String path) {
        ContentValues values = new ContentValues(1);
        values.put("path", path);
        return mDB.insert("files", null, values);
    }

    public Long findFile(String path) {
        Cursor cur = mDB.query("files",
            new String[] { "id" },
            "path = ?",
            new String[] { path },
            null,
            null,
            null
        );

        if(cur.getCount() < 1) {
            Log.w(TAG, "Unable to find a file " + path);
            return null;
        }
        cur.moveToPosition(0);
        long idx = cur.getLong(cur.getColumnIndex("id"));
        cur.close();
        Log.i(TAG, "Found " + idx);
        return idx;
    }

    public void logChapters() {
        Cursor cur = mDB.query("chapter",
            new String[] { "serie_id", "chapter_id" },
            null,
            null,
            null,
            null,
            null
        );
        cur.moveToPosition(-1);

        while(cur.moveToNext()) {
            Log.i("MANGADEX's Chapters",
            "Serie: " + cur.getString(cur.getColumnIndex("serie_id")) + ", " +
            "Chapters: " + cur.getString(cur.getColumnIndex("chapter_id"))
            );
        }

        cur.close();
    }

	public String getFilePath(long id) {
        Cursor cur = mDB.query("files",
            new String[] { "path" },
            "id = ?",
            new String[] { ""+id },
            null,
            null,
            null
        );

        if(cur.getCount() < 1) {
            Log.w(TAG, "Unable to find file " + id);
            return null;
        }
        cur.moveToPosition(0);
        String output = cur.getString(cur.getColumnIndex("path"));
        cur.close();
        return output;
	}
}
