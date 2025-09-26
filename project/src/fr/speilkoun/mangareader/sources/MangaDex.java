package fr.speilkoun.mangareader.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentResolver;
import android.database.Cursor;
import android.text.format.Time;
import android.util.Log;
import fr.speilkoun.mangareader.data.Chapter;
import fr.speilkoun.mangareader.data.Provider;
import fr.speilkoun.mangareader.utils.ISO8601DateParser;

public class MangaDex {
	public static String TAG = "MangaDex";
	public static int MAX_RETRIES = 3;

	public static native String getChapters(String id, int offset);
	public static native String getChapterImages(String id);

	public static Integer getSerieIdFromMangadexId(ContentResolver resolver, String id) {
		Cursor cur = resolver.query(Provider.CONTENT_SERIE_URI,
			new String[] { "id" },
			"attribute = '"+id+"' AND source='mangadex'",
			null,
			"id ASC"
		);
		if(cur.getCount() < 1) {
			Log.e("Mangadex", "Unable to find a serie with " + id + " as an id");
			return null;
		}
		
		cur.moveToPosition(0);
		return cur.getInt(cur.getColumnIndex("id"));
	}

	static void parseAndAppendChapter(ContentResolver resolver, int manga_db_idx, JSONObject chapter)
		throws JSONException {
		JSONObject attrs = chapter.getJSONObject("attributes");

		Time publishedAt = ISO8601DateParser.parse(attrs.getString("publishAt"));
		int chapter_no = attrs.getInt("chapter");
		
		int volume = 0;
		try {
			volume = attrs.getInt("volume");
		} catch(JSONException e) {}

		String title = "";
		try {
			title = attrs.getString("title");
		} catch(JSONException e) {}

		String chapter_id = "";
		try {
			chapter_id = attrs.getString("id");
		} catch(JSONException e) {}
		
		Chapter c = new Chapter(
			null,
			manga_db_idx,
			title,
			"",
			chapter_id,
			volume,
			chapter_no,
			publishedAt
		);
		resolver.insert(Provider.CONTENT_CHAPTER_URI, c.getContentValues());
	}

	public static void loadChapters(ContentResolver resolver, String id) {
		Integer manga_db_idx = MangaDex.getSerieIdFromMangadexId(resolver, id);
		if(manga_db_idx == null) {
			Log.e(TAG, "Could not find a manga with following id in db: " + id);
			return;
		}
		
		int offset = 0, retry = 0;
		{
			Cursor cur = resolver.query(Provider.CONTENT_CHAPTER_URI,
				new String[] { "COUNT(*) AS count" },
				"serie_id = "+manga_db_idx,
				null,
				null
			);

			if(cur.getCount() >= 1) {
				cur.moveToPosition(0);
				offset = cur.getInt(cur.getColumnIndex("count"));
				Log.i(TAG, ""+offset);
			} else 
				Log.e("Mangadex", "Unable to find a serie with " + id + " as an id");
		}
		
		while(true) {
			String resp = MangaDex.getChapters(id, offset);
			
			try {
				JSONTokener tokener = new JSONTokener(resp);
				JSONArray chapters = new JSONObject(tokener).getJSONArray("data");
				
				for(int i = 0; i < chapters.length(); i ++) {
					JSONObject chapter = chapters.getJSONObject(i);
					try {
						parseAndAppendChapter(resolver, manga_db_idx, chapter);
					} catch(JSONException e) {
						Log.e(TAG, "Could not parse a chapter output for " + id + " n " + i);
						e.printStackTrace();
					}
				}

				offset += chapters.length();
				retry = 0;
				if(chapters.length() == 0)
					break;
			} catch(JSONException e) {
				Log.e(TAG, "Could not parse all the chapters output for " + id);
				e.printStackTrace();
				retry ++;
				if(retry >= MAX_RETRIES)
					break;
			}
		}
	}
}
