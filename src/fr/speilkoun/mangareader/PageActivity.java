package fr.speilkoun.mangareader;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import fr.speilkoun.mangareader.data.Database;
import fr.speilkoun.mangareader.data.Page;

public class PageActivity extends Activity {
    class OnImageTouchListener implements OnTouchListener {
        final String TAG = "OnImageTouchListener";

        PageActivity parent;

        float drag_x, drag_y;
        boolean on_drag = false;

        public OnImageTouchListener(PageActivity parent) {
            this.parent = parent;
        }

        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            switch(ev.getAction()) {
            case MotionEvent.ACTION_UP:
                // Ignore page switching when zoomed in
                // to avoid issues        
                if(parent.zoom == DEFAULT_ZOOM) {
                    float portionX = ev.getX() / v.getWidth();
                    if(portionX >= .66)
                        parent.set_current_page(parent.current_page_idx + 1);
                    if(portionX <= .33)
                        parent.set_current_page(parent.current_page_idx - 1);
                    Log.i(TAG, ""+portionX + ": " + (portionX >= .66) + "; " + (portionX <= .33));
                } else if(on_drag)
                    on_drag = false;
                break;

            case MotionEvent.ACTION_DOWN:
                if(parent.zoom == DEFAULT_ZOOM)
                    break;
                
                on_drag = true;
                drag_x = ev.getX();
                drag_y = ev.getY();
                break;
            
            case MotionEvent.ACTION_MOVE:
                parent.image_matrix.postTranslate(
                    ev.getX() - drag_x,
                    ev.getY() - drag_y
                );
                parent.updateImageMatrix();

                drag_x = ev.getX();
                drag_y = ev.getY();
                break;
            
            default:
                break;
            }
            return true;
        }
    }

    static final String TAG = "PageActivity";

    ImageView page_view;

    ArrayList<Page> pages;

    final int ZOOM_INCREMENT = 20;
    final int DEFAULT_ZOOM = 100;

    int zoom = DEFAULT_ZOOM;
    int current_page_idx = 0;
    Matrix image_matrix = new Matrix();
    Bitmap current_image;

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
        
        current_image = null;
        page_view.setImageBitmap(null);
        page_view.refreshDrawableState();
        System.gc();

        current_page_idx = new_page_idx;

        current_image = load_page(current_page_idx);
        page_view.setImageBitmap(current_image);
        page_view.refreshDrawableState();

        resetZoom();
    }

    void resetZoom() {
        if(current_image == null)
            return;
        zoom = DEFAULT_ZOOM;

        float image_width_scale = (float) page_view.getWidth() / (float) current_image.getWidth();
        float image_height_scale = (float) page_view.getHeight() / (float) current_image.getHeight();
        float image_scale = Math.min(image_width_scale, image_height_scale);

        image_matrix.reset();
        image_matrix.postScale(image_scale, image_scale);
        this.updateImageMatrix();
    }

    void updateImageMatrix() {
        page_view.setImageMatrix(image_matrix);
    }

    void addZoom(int zoom_increment) {
        int new_zoom = Math.max(zoom + zoom_increment, DEFAULT_ZOOM);

        if(new_zoom == DEFAULT_ZOOM) {
            resetZoom();
            return;
        }

        float scale = (float) new_zoom / (float) zoom;
        zoom = new_zoom;
        image_matrix.postScale(scale, scale);
        this.updateImageMatrix();
    }

    void initZoomButtons() {
        Button zoomIn = (Button) findViewById(R.id.currentPageZoomIn);
        zoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PageActivity.this.addZoom(ZOOM_INCREMENT);
            }
        });

        Button zoomOut = (Button) findViewById(R.id.currentPageZoomOut);
        zoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PageActivity.this.addZoom(-ZOOM_INCREMENT);
            }
        });
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        this.setContentView(R.layout.page_activity);

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

        page_view = (ImageView) this.findViewById(R.id.currentPageImage);
        page_view.setScaleType(ImageView.ScaleType.MATRIX);
        // We do this to wait until the imageview size is defined to reset
        // the matrix
        page_view.post(new Runnable() {
            @Override
            public void run() {
                PageActivity.this.resetZoom();
            }
        });

        this.set_current_page(0);

        this.page_view.setOnTouchListener(new OnImageTouchListener(this));

        this.page_view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent ev) {
                if(ev.getAction() != KeyEvent.ACTION_UP)
                    return true;
                
                switch(keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_SOFT_LEFT:
                    PageActivity.this.set_current_page(PageActivity.this.current_page_idx - 1);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_SOFT_RIGHT:
                    PageActivity.this.set_current_page(PageActivity.this.current_page_idx + 1);
                    break;
                case KeyEvent.KEYCODE_BACK:
                    PageActivity.this.finish();
                    break;
                default: break;
                }
                return true;
            }
        });

        this.page_view.setFocusableInTouchMode(true);

        initZoomButtons();
    }		
}
