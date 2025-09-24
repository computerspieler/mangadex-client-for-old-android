package fr.speilkoun.mangareader.sources;

public class MangaDex {
	public static native String getChapters(String id, int offset);
	public static native String getChapterImages(String id);
}
