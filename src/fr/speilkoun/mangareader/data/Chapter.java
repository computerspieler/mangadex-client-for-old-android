package fr.speilkoun.mangareader.data;

import android.text.format.Time;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class Chapter implements Parcelable {
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

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
		public Chapter createFromParcel(Parcel in) {
			return new Chapter(in);
		}

		public Chapter[] newArray(int size) {
			return new Chapter[size];
		}
	};

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(this.id);
		out.writeInt(this.serie_id);
		out.writeInt(this.volume_id);
		out.writeInt(this.chapter_id);
		out.writeString(this.title);
		out.writeString(this.publisher);
		out.writeString(this.custom_attributes);
		out.writeString(this.release_date.format3339(true));
	}

	private Chapter(Parcel in) {
		this.id = in.readInt();
		this.serie_id = in.readInt();
		this.volume_id = in.readInt();
		this.chapter_id = in.readInt();
		this.title = in.readString();
		this.publisher = in.readString();
		this.custom_attributes = in.readString();
		this.release_date = new Time();
		this.release_date.parse3339(in.readString());
	}
}
