package fr.speilkoun.mangareader.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Chapter;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.utils.FileDownloader;
import fr.speilkoun.mangareader.utils.ISO8601DateParser;

public class MangaDex {
	public static String TAG = "MangaDex";
	public static int MAX_RETRIES = 3;

	public static native String getInfos(String id);
	public static native String getChapterImages(String id);
	public static native String getChapters(String id, int offset);

	static void parseAndAppendChapter(int manga_db_idx, JSONObject chapter)
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
		
		Database.getInstance().addChapter(
			new Chapter(
				null,
				manga_db_idx,
				title,
				"",
				chapter_id,
				volume,
				chapter_no,
				publishedAt
			)
		);
	}

	public static void loadChapters(String id) {
		Database db = Database.getInstance();

		Serie s = db.getOneSerie("attribute = '"+id+"' AND source='mangadex'", "id ASC");
		if(s == null) {
			Log.e(TAG, "Could not find a manga with following id in db: " + id);
			return;
		}
		int manga_db_idx = s.id;
		
		int retry = 0, offset = db.getChapterCount(manga_db_idx);
		
		Log.i(TAG, "Downloading chapters of " + s.id);
		while(true) {
			String resp = MangaDex.getChapters(id, offset);
			
			try {
				JSONTokener tokener = new JSONTokener(resp);
				JSONArray chapters = new JSONObject(tokener).getJSONArray("data");
				
				for(int i = 0; i < chapters.length(); i ++) {
					JSONObject chapter = chapters.getJSONObject(i);
					try {
						Log.i(TAG, "Downloading chapter " + i);
						parseAndAppendChapter(manga_db_idx, chapter);
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

	public static Serie addManga(Context ctx, String id)
		throws JSONException {
		String resp = MangaDex.getInfos(id);
		JSONTokener tokener = new JSONTokener(resp);
		JSONObject manga = new JSONObject(tokener).getJSONObject("data");

		JSONObject titles = manga.getJSONObject("attributes").getJSONObject("title");
		String title = null;

		try {
			if(title == null)
				title = titles.getString("en");
		} catch(JSONException e) {}
		try {
			if(title == null)
				title = titles.getString("jp");
		} catch(JSONException e) {}
		try {
			if(title == null)
				title = titles.getString("fr");
		} catch(JSONException e) {}

		String cover_filename = null;
		JSONArray relationships = manga.getJSONArray("relationships");
		for(int i = 0; i < relationships.length(); i ++) {
			JSONObject obj = relationships.getJSONObject(i);
			if(!obj.getString("type").equals("cover_art"))
				continue;
			
			Log.i(TAG, "Found a cover art");
			cover_filename = obj.getJSONObject("attributes")
				.getString("fileName");
		}

		Long cover_image_id = null;
		if(cover_filename != null) {
			try {
				Log.i(TAG, "Loading cover");
				cover_image_id = FileDownloader.downloadFileAndAddToDatabase(ctx,
					cover_filename,
					"uploads.mangadex.org",
					"/covers/" + id + "/" + cover_filename + ".256.jpg"
				);
			} catch (Exception e) {
				Log.e(TAG, "Unable to download the cover image of " + title);
				Log.e(TAG, e.toString());
			}
		}
		
		Serie s = new Serie(
			null,
			title,
			cover_image_id,
			"mangadex",
			id
		);
		Database.getInstance().addSerie(s);
		MangaDex.loadChapters(id);

		return s;
	}
}
