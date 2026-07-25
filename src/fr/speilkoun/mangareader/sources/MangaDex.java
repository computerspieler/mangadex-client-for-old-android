package fr.speilkoun.mangareader.sources;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.format.Time;
import android.util.Log;

import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Page;
import fr.speilkoun.mangareader.data.Chapter;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.utils.HTTP;
import fr.speilkoun.mangareader.utils.HTTPException;
import fr.speilkoun.mangareader.utils.ISO8601DateParser;

public class MangaDex extends Source {
	public static String TAG = "MangaDex";
	public static int MAX_RETRIES = 3;

	static String DEFAULT_DOMAIN_NAME = "https://api.mangadex.org";

	public MangaDex() {
		super("mangadex");
	}

	String getInfos(String id)
		throws HTTPException {
		//TODO: Add support for "artist" & "author" fields
		return HTTP.getJSON(DEFAULT_DOMAIN_NAME + "/manga/" + id + "?includes[]=cover_art");
	}

	String getChapterImages(String id)
		throws HTTPException {
		return HTTP.getJSON(DEFAULT_DOMAIN_NAME + "/at-home/server/" + id);
	}

	String getChapters(String id, int offset)
		throws HTTPException {
		return HTTP.getJSON(
			DEFAULT_DOMAIN_NAME +
			"/manga/" + id + "/feed?offset=" + offset +"&limit=10&translatedLanguage[]=en"
		);
	}

	public ArrayList<Serie> searchManga(String name)
		throws JSONException, HTTPException, URISyntaxException, UnsupportedEncodingException
	{
		ArrayList<Serie> output = null;

		Log.i(TAG, URLEncoder.encode(name, "utf-8"));
		String raw = HTTP.getJSON(
			DEFAULT_DOMAIN_NAME+
			"/manga?title=" + URLEncoder.encode(name, "utf-8") + "&includes[]=cover_art&limit=3"
		);
		
		JSONTokener tokener = new JSONTokener(raw);
		JSONObject resp = new JSONObject(tokener);

		if(!resp.getString("result").equals("ok")) {
			if(resp.has("error"))
				Log.e(TAG, resp.getString("error"));
			return null;
		}

		JSONArray data = resp.getJSONArray("data");
		output = new ArrayList<Serie>(data.length());
		for(int i = 0; i < data.length(); i ++)
			output.add(parseSerie(data.getJSONObject(i)));

		return output;
	}

	void parseAndAppendChapter(int manga_db_idx, JSONObject chapter)
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
			chapter_id = chapter.getString("id");
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

	public void loadChapters(Serie s)
		throws HTTPException {
		Database db = Database.getInstance();

		if(s == null) {
			Log.e(TAG, "Tried to retrieve chapter for a non-existant serie");
			return;
		}
		int manga_db_idx = s.id;
		
		int retry = 0, offset = db.getChapterCount(manga_db_idx);
		
		Log.i(TAG, "Downloading chapters of " + s.id + " \"" + s.title + "\"");
		while(true) {
			String resp = this.getChapters(s.attribute, offset);
			
			try {
				JSONTokener tokener = new JSONTokener(resp);
				JSONArray chapters = new JSONObject(tokener).getJSONArray("data");
				
				for(int i = 0; i < chapters.length(); i ++) {
					JSONObject chapter = chapters.getJSONObject(i);
					try {
						Log.i(TAG, "Downloading chapter " + i);
						parseAndAppendChapter(manga_db_idx, chapter);
					} catch(JSONException e) {
						Log.e(TAG, "Could not parse a chapter output for " + s.attribute + " n " + i, e);
					}
				}

				offset += chapters.length();
				retry = 0;
				if(chapters.length() == 0)
					break;
			} catch(JSONException e) {
				Log.e(TAG, "Could not parse all the chapters output for " + s.attribute, e);
				retry ++;
				if(retry >= MAX_RETRIES)
					break;
			}
		}
	}

	String getTitleFromAttributes(JSONObject titles) {
		final String languages[] = {"en", "jp", "fr"};
		String title = null;
		for(String lang : languages) {
			try {
				title = titles.getString(lang);
			} catch(JSONException e) {}

			if(title != null)
				return title;
		}
		return null;
	}

	Serie parseSerie(JSONObject manga) 
		throws JSONException
	{
		JSONObject attributes = manga.getJSONObject("attributes");
		String id = manga.getString("id");
		
		String title = null;
		if(attributes.has("title"))
			title = getTitleFromAttributes(attributes.getJSONObject("title"));

		if(attributes.has("altTitles")) {
			JSONArray titles = attributes.getJSONArray("altTitles");
			for(int i = 0; i < titles.length(); i ++) {
				if(title != null)
					break;

				title = getTitleFromAttributes(titles.getJSONObject(i));
			}
		}

		String cover_filename = null;
		JSONArray relationships = manga.getJSONArray("relationships");
		for(int i = 0; i < relationships.length(); i ++) {
			JSONObject obj = relationships.getJSONObject(i);
			if(!obj.getString("type").equals("cover_art"))
				continue;
			
			if(!obj.has("attributes"))
				continue;
			
			Log.i(TAG, "Found a cover art");
			cover_filename = obj.getJSONObject("attributes")
				.getString("fileName");
		}

		Long cover_image_id = null;
		if(cover_filename != null) {
			try {
				Log.i(TAG, "Loading cover");
				cover_image_id = HTTP.downloadFileAndAddToDatabase(cover_filename,
					"https://uploads.mangadex.org/covers/"
					+ id + "/" + cover_filename + ".256.jpg"
				);
			} catch (Exception e) {
				Log.e(TAG, "Unable to download the cover image of " + title);
				Log.e(TAG, e.toString());
			}
		}
		
		return new Serie(
			null,
			title,
			cover_image_id,
			"mangadex",
			id
		);
	}

	public Serie findOrAddManga(String id)
		throws JSONException, HTTPException {
		Serie output = Database.getInstance()
			.getOneSerie("attribute = '"+id+"' AND source='"+this.name+"'", "id ASC");
		
		if(output != null)
			return output;
		
		String resp = this.getInfos(id);
		JSONTokener tokener = new JSONTokener(resp);
		JSONObject manga = new JSONObject(tokener).getJSONObject("data");

		output = parseSerie(manga);
		Database.getInstance().addSerie(output);
		this.loadChapters(output);

		return output;
	}

	public void downloadChapter(Chapter c)
		throws HTTPException
	{
		Log.i(TAG, "Downloading a chapter: " + c.title + "(" + c.id + "," + c.custom_attributes + ")");
		String images = getChapterImages(c.custom_attributes);
		try {
			JSONTokener tokener = new JSONTokener(images);
			JSONObject obj = new JSONObject(tokener);

			String base_url = obj.getString("baseUrl");
			JSONObject chapters = obj.getJSONObject("chapter");
			String chapter_hash = chapters.getString("hash");
			String path_to_use = chapters.has("dataSaver") ?
				"/data-saver/" : "/data/";
			JSONArray pages = chapters.has("dataSaver") ?
			 	chapters.getJSONArray("dataSaver") :
				chapters.getJSONArray("data");
			
			for(int i = 0; i < pages.length(); i ++) {
				String filename = pages.getString(i);
				Log.i(TAG, "Loading page " + (i+1) + "/" + pages.length() + ": " + filename);
				long file_idx = HTTP.downloadFileAndAddToDatabase(filename,
					base_url + path_to_use + chapter_hash + "/" + filename
				);

				Database.getInstance().addPage(
					new Page(i, c.id, (int) file_idx)
				);
			}
		} catch(JSONException e) {
			Log.e(TAG, "Could not load " + c.toString(), e);
		}
	}
}
