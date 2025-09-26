package fr.speilkoun.mangareader.data;

import android.text.format.Time;

import android.content.ContentValues;

public class Chapter {
    public final int id;
    public final int serie_id;
    public final Integer chapter_id;
    public final String publisher;
    public final String custom_attributes;
    public final Time release_date;
    
    public String title;
    public Integer volume_id;

    public Chapter(
        Integer id,
        int serie_id,
        String title,
        String publisher,
        String custom_attributes,
        Integer volume_id,
        Integer chapter_id,
        Time release_date
    ) {
        this.id = id == null ? -1 : id;
        
        this.serie_id = serie_id;
        this.title = title;
        this.publisher = publisher;
        this.custom_attributes = custom_attributes;
        this.volume_id = volume_id;
        this.chapter_id = chapter_id;
        this.release_date = release_date;
    }

    public static final String SERIE_ID = "serie_id";
    public static final String TITLE = "title";
    public static final String PUBLISHER = "publisher";
    public static final String CUSTOM_ATTRIBUTES = "custom_attributes";
    public static final String VOLUME_ID = "volume_id";
    public static final String CHAPTER_ID = "chapter_id";
    public static final String RELEASE_DATE = "release_date";

    public ContentValues getContentValues() {
        ContentValues output = new ContentValues(7);
        
        output.put(SERIE_ID, this.serie_id);
        output.put(TITLE, this.title);
        output.put(PUBLISHER, this.publisher);
        output.put(CUSTOM_ATTRIBUTES, this.custom_attributes);
        output.put(VOLUME_ID, this.volume_id);
        output.put(CHAPTER_ID, this.chapter_id);
        /* TODO: Change this for something parseable */
        output.put(RELEASE_DATE, this.release_date.format3339(true));

        return output;
    }
}
