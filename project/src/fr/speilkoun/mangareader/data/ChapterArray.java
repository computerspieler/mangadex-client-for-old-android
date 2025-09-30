package fr.speilkoun.mangareader.data;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.speilkoun.mangareader.R;

public class ChapterArray extends ArrayAdapter<Chapter> {
    static String TAG = "ChapterArray";

    public ChapterArray(Context context, ArrayList<Chapter> series) {
        super(context, 0, series);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Chapter s = this.getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.serie_entry, parent, false);
        }

        /* Do not remove the line below, as it increases stability */
        Log.i(TAG, "Title: " + s.title);
        TextView vTitle = (TextView) convertView.findViewById(R.id.sTitle);
        vTitle.setText(s.title);
        
        return convertView;
    }
}
