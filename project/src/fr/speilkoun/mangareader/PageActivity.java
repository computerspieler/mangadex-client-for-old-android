package fr.speilkoun.mangareader;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Page;

public class PageActivity extends Activity {
    static final String TAG = "PageActivity";

    ImageView page_view;

    ArrayList<Page> pages;

    int current_page_idx = 0;
    Bitmap prev_image = null;
    Bitmap current_image = null;
    Bitmap next_image = null;

    // TODO: Set this flag based on memory usage
    static final boolean LOAD_AND_KEEP_NEIGHBORS = false;

    Bitmap load_page(int page) {
        if(page < 0 || page >= pages.size())
            return null;
        
        
        return BitmapFactory.decodeFile(
            Database.getInstance()
                .getFilePath(pages.get(page).file_id)
        );
    }

    @SuppressWarnings("unused")
    void set_current_page(int new_page_idx) {
        if(new_page_idx < 0 || new_page_idx >= pages.size())
            //TODO: Add a toast or change chapter ?
            return;
        
        if(new_page_idx == current_page_idx+1 && LOAD_AND_KEEP_NEIGHBORS) {
            prev_image = current_image;
            current_image = next_image;
            next_image = null;
        } else if(new_page_idx == current_page_idx-1 && LOAD_AND_KEEP_NEIGHBORS) {
            next_image = current_image;
            current_image = prev_image;
            prev_image = null;
        } else {
            next_image = null;
            current_image = null;
            prev_image = null;
        }

        System.gc();

        current_page_idx = new_page_idx;

        if(current_image == null)
            current_image = load_page(current_page_idx);

        if(LOAD_AND_KEEP_NEIGHBORS) {
            if(prev_image == null)
                prev_image = load_page(current_page_idx - 1);
            if(next_image == null)
                next_image = load_page(current_page_idx + 1);
        }

        if(current_image != null) {
            page_view.setImageBitmap(current_image);
            page_view.refreshDrawableState();
            Log.i(TAG, current_page_idx + " " + pages.get(current_page_idx).file_id + " " + current_image.toString());
        }
            
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        int chapter_id = getIntent().getExtras().getInt("chapter_id");
        this.pages = Database.getInstance().getPages(chapter_id);
        if(this.pages.size() == 0) {
            Toast.makeText(
                this.getApplicationContext(),
                R.string.empty_chapter_toast,
                Toast.LENGTH_SHORT
            ).show();
            this.finish();
            return;
        }

        page_view = new ImageView(this);
        this.setContentView(page_view);

        Log.i(TAG, "We survived ! " + pages.size());
        this.set_current_page(0);

        this.page_view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                if(ev.getAction() != MotionEvent.ACTION_UP)
                    return true;
                
                float portionX = ev.getX() / v.getWidth();
                if(portionX >= .66)
                    PageActivity.this.set_current_page(PageActivity.this.current_page_idx + 1);
                if(portionX <= .33)
                    PageActivity.this.set_current_page(PageActivity.this.current_page_idx - 1);
                Log.i(TAG, ""+portionX + ": " + (portionX >= .66) + "; " + (portionX <= .33));
                return true;
            }
        });
    }		
}
