package org.grameenfoundation.consulteca.interactivecontent;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.grameenfoundation.consulteca.ApplicationRegistry;
import org.grameenfoundation.consulteca.R;
import org.grameenfoundation.consulteca.ui.ThumbnailTask;
import org.grameenfoundation.consulteca.ui.ThumbnailViewHolder;
import org.grameenfoundation.consulteca.utils.ImageUtils;

/**
 * Custom Adapter that is the backing object of the Main ListView of the application.
 */
public class InteractiveContentListViewAdapter extends BaseAdapter {
    protected LayoutInflater layoutInflater;
    private Context context;
    private Handler handler = null;
    private String[] items = null;

    public InteractiveContentListViewAdapter(Context context) {
        super();
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        handler = new Handler();
        items = ContentUtils.getContentListing();
    }

    protected static Drawable getItemDrawable(String item) {
        Drawable drawable = null;
        int width = 50, height = 50;
        drawable = ImageUtils.drawRandomColorImageWithText(ApplicationRegistry.getApplicationContext(),
                item.substring(0, 1).toUpperCase(), width, height);

        return drawable;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        if (items != null && (position >= 0 && position <= items.length)) {
            return items[position];
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null || rowView.findViewById(R.id.sync_button) != null) {
            rowView = layoutInflater.inflate(R.layout.listviewobject, parent, false);
        }

        createListViewItemView(position, rowView);
        return rowView;
    }

    private void createListViewItemView(final int position, View rowView) {
        ThumbnailViewHolder viewHolder = null;
        if (rowView.getTag() != null && rowView.getTag() instanceof ThumbnailViewHolder) {
            viewHolder = (ThumbnailViewHolder) rowView.getTag();
        } else {
            viewHolder = new ThumbnailViewHolder();
        }


        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        TextView titleView = (TextView) rowView.findViewById(R.id.title);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.description);

        String interactiveContentItem = (String) getItem(position);
        if (interactiveContentItem != null) {
            titleView.setText(interactiveContentItem);
            descriptionView.setText(interactiveContentItem);
            descriptionView.setVisibility(TextView.VISIBLE);
            imageView.setTag(interactiveContentItem);

            viewHolder.position = position;
            viewHolder.imageView = imageView;
            rowView.setTag(viewHolder);

            new ThumbnailTask<String>(viewHolder, position) {
                @Override
                protected Drawable doInBackground(String... params) {
                    return getItemDrawable(params[0]);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, interactiveContentItem);
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

}
