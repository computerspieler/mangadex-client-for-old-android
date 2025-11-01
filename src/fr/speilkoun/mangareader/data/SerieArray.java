package fr.speilkoun.mangareader.data;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.speilkoun.mangareader.R;

public class SerieArray extends ArrayAdapter<Serie> {
    static String TAG = "SerieArray";

    public SerieArray(Context context, ArrayList<Serie> series) {
        super(context, 0, series);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Serie s = this.getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.serie_entry, parent, false);
        }

        /* Do not remove the line below, as it increases stability */
        Log.i(TAG, "Title: " + s.title);
        if(s.title != null) {
            TextView vTitle = (TextView) convertView.findViewById(R.id.sTitle);
            vTitle.setText(s.title);
        }
        
        if(s.cover_image_id != null) {
            String cover_path = Database.getInstance().getFilePath(s.cover_image_id);
            ImageView v = (ImageView) convertView.findViewById(R.id.sImage);
            Bitmap image = BitmapFactory.decodeFile(cover_path);

            if(image != null && v != null)
                v.setImageBitmap(image);
            else if(image != null)
                Log.e(TAG, "Unable to load the view");
            else
                Log.e(TAG, "Unable to load the image");
        }

        return convertView;
    }
}
