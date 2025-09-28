package fr.speilkoun.mangareader.data;

import android.content.ContentValues;

public class Serie {
    public enum MangaSerieStatus {
        FINISHED("FINISHED"),
        RUNNING("RUNNING"),
        UNKNOWN("UNKNOWN");

        String s;
        MangaSerieStatus(String s) {
            this.s = s;
        }

        @Override
        public String toString()
        { return this.s; }
    }

    public final int id;
    
    public MangaSerieStatus status = MangaSerieStatus.UNKNOWN;
    public Long cover_image_id;
    public String title;
    public String source;
    public String attribute;

    public Serie(
        Integer id,
        String title,
        Long cover_image_id,
        String source,
        String attribute
    ) {
        this.id = id == null ? -1 : id;
        this.status = MangaSerieStatus.UNKNOWN;

        this.cover_image_id = cover_image_id;
        this.title = title;
        this.source = source;
        this.attribute = attribute;
    }

    public ContentValues getContentValues() {
        ContentValues output = new ContentValues();

        output.put("title", this.title);
        output.put("status", this.status.toString());
        output.put("source", this.source);
        output.put("attribute", this.attribute);
        output.put("cover_image_id", this.cover_image_id);

        return output;
    }

    
}
