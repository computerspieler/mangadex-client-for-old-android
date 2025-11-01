package fr.speilkoun.mangareader.data;

import android.content.ContentValues;

public class Page {
    public int page;
    public int chapter_id;
    public Integer file_id;

    public Page(
        int page,
        int chapter_id,
        Integer file_id
    ) {
        this.page = page;
        this.chapter_id = chapter_id;
        this.file_id = file_id;
    }

    public ContentValues getContentValues() {
        ContentValues output = new ContentValues(3);

        output.put("page", this.page);
        output.put("chapter_id", this.chapter_id);
        output.put("file_id", this.file_id);

        return output;
    }
}
