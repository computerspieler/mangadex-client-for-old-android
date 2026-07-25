package fr.speilkoun.mangareader.sources;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONException;


import fr.speilkoun.mangareader.data.Chapter;
import fr.speilkoun.mangareader.data.Serie;
import fr.speilkoun.mangareader.utils.HTTPException;

public abstract class Source {
	
	String name;
	public String getName() { return name; }

	public Source(String name) {
		this.name = name;
	}

	public abstract ArrayList<Serie> searchManga(String name)
		throws JSONException, HTTPException, URISyntaxException, UnsupportedEncodingException;

	public abstract void loadChapters(Serie s)
		throws HTTPException;

	public abstract Serie findOrAddManga(String id)
		throws JSONException, HTTPException;

	public abstract void downloadChapter(Chapter c)
		throws HTTPException;
}
