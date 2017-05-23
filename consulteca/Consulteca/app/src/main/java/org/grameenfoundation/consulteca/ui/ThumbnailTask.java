package org.grameenfoundation.consulteca.ui;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import org.joda.time.Interval;

/**
 * used to update an image view in the thumbnail holder in a background without
 * working on the ui thread.
 */
public class ThumbnailTask<T> extends AsyncTask<T, Interval, Drawable> {
    protected ThumbnailViewHolder viewHolder = null;
    protected int position;

    public ThumbnailTask(ThumbnailViewHolder viewHolder, int position) {
        this.viewHolder = viewHolder;
        this.position = position;
    }

    @Override
    protected Drawable doInBackground(T... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (viewHolder.position == position) {
            viewHolder.imageView.setImageDrawable(drawable);
        }
    }
}
