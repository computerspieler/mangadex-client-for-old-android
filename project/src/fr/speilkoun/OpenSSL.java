package fr.speilkoun;

public class OpenSSL {
	public static native void init();
	public static native String getChapterImages(String id);
	
	static {
        System.loadLibrary("manga-reader");
    }
}
