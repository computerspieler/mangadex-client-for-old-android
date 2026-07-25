package fr.speilkoun.mangareader.sources;

import java.util.HashMap;
import java.util.Set;

public class Sources {
	private static final HashMap<String, Source> SOURCES = new HashMap<String, Source>();
	public static Source get(String name) {
		return SOURCES.get(name);
	}
	public static Set<String> sources() {
		return SOURCES.keySet();
	}
	
	private static void addSource(Source s)
	{ SOURCES.put(s.getName(), s); }
	static {
		addSource(new MangaDex());
	}

}
