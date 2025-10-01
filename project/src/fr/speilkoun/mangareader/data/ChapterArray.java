package fr.speilkoun.mangareader.data;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.speilkoun.mangareader.R;

public class ChapterArray extends ArrayAdapter<Chapter> {
    static String TAG = "ChapterArray";

    public ChapterArray(Context context, ArrayList<Chapter> chapters) {
        super(context, 0, chapters);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Chapter c = this.getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.chapter_entry, parent, false);
        }

        /* Do not remove the line below, as it increases stability */
        Log.i(TAG, "Title: " + c.title);
        if(c.title != null) {
            TextView vTitle = (TextView) convertView.findViewById(R.id.cTitle);
            vTitle.setText(c.title);
        }

        TextView vInfo = (TextView) convertView.findViewById(R.id.cInfo);
        StringBuilder info_builder = new StringBuilder();
        if(c.volume_id != null) {
            info_builder.append("Volume ");
            info_builder.append(c.volume_id);
            info_builder.append("  ");
        }
        if(c.chapter_id != null) {
            info_builder.append("Chapter ");
            info_builder.append(c.chapter_id);
            info_builder.append("  ");
        }
        if(c.release_date != null) {
            info_builder.append(c.release_date.format2445());
            //info_builder.append("  ");
        }
        vInfo.setText(info_builder.toString());
        
        return convertView;
    }
}
