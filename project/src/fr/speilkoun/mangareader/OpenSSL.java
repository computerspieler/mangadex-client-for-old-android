package fr.speilkoun.mangareader;

public class OpenSSL {
	public static native void init();
	
	static {
        System.loadLibrary("manga-reader");
    }
}
