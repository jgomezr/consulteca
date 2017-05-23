package org.grameenfoundation.consulteca.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.grameenfoundation.consulteca.R;
import org.grameenfoundation.consulteca.model.ListObject;
import org.grameenfoundation.consulteca.model.SearchLog;
import org.grameenfoundation.consulteca.model.SearchMenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * extends the MainListViewAdapter to support the display of
 * favourite lists.
 */
public class RecentSearchesListViewAdapter extends MainListViewAdapter {
    private static final int SELECTED_IMAGE_VIEW_BACKGROUND_COLOR = 0xff5E5C5C;
    private List<SearchLog> searchLogs = null;
    private List<SearchLog> selectedItems = new ArrayList<SearchLog>();
    private Context context = null;

    public RecentSearchesListViewAdapter(Context context) {
        super(context);
        this.context = context;
    }


    @Override
    public int getCount() {
        searchLogs = menuItemService.getAllSearchLogs();
        return menuItemService.countSearchLogs();
    }

    @Override
    public Object getItem(int position) {
        SearchLog searchLog = null;
        if (position >= 0 && position < searchLogs.size()) {
            searchLog = searchLogs.get(position);
        }

        return searchLog;
    }

    /**
     * gets the associated menu item for the given search log.
     *
     * @param searchLog search log for which a search menu item is required.
     * @return SearchMenuItem associated with the given search log.
     */
    private SearchMenuItem getAssociatedItem(SearchLog searchLog) {
        return menuItemService.getSearchMenuItem(searchLog.getMenuItemId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            rowView = layoutInflater.inflate(R.layout.listviewobject, parent, false);
        }

        ThumbnailViewHolder viewHolder;
        if (rowView.getTag() != null && rowView.getTag() instanceof ThumbnailViewHolder) {
            viewHolder = (ThumbnailViewHolder) rowView.getTag();
        } else {
            viewHolder = new ThumbnailViewHolder();
        }


        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        TextView titleView = (TextView) rowView.findViewById(R.id.title);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.description);

        SearchLog searchLog = (SearchLog) getItem(position);
        if (searchLog != null) {
            ListObject listObject = getAssociatedItem(searchLog);
            if (listObject != null) {
                titleView.setText(listObject.getLabel());
                descriptionView.setText(searchLog.getDescription());
                descriptionView.setVisibility(TextView.VISIBLE);

                imageView.setTag(searchLog);
                viewHolder.position = position;
                viewHolder.imageView = imageView;
                rowView.setTag(viewHolder);

                new ThumbnailTask<SearchLog>(viewHolder, position) {
                    @Override
                    protected Drawable doInBackground(SearchLog... params) {
                        return getSearchLogImageViewDrawable(params[0]);
                    }

                    @Override
                    protected void onPostExecute(Drawable drawable) {
                        super.onPostExecute(drawable);
                        viewHolder.imageView.setBackgroundColor(SELECTED_IMAGE_VIEW_BACKGROUND_COLOR);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchLog);
            }
        }

        return rowView;
    }

    /**
     * gets the drawable that can be used as an image for the view
     * representing the given search log.
     *
     * @param param the search log for which a drawable is required
     * @return Drawable or null if a drawable for the search log cannot be determined.
     */
    private Drawable getSearchLogImageViewDrawable(SearchLog param) {
        SearchMenuItem searchMenuItem = getAssociatedItem(param);
        if (searchMenuItem != null) {
            if (isSearchLogSelected(param)) {
                return context.getResources().getDrawable(R.drawable.ic_action_accept);
            }
            return getListObjectDrawable(searchMenuItem);
        }

        return null;
    }

    /**
     * mark the given view element as selected. The thumbnail for the element
     * will be marked with a tick.
     *
     * @param view     the view of the search to update.
     * @param position the position of the search log to mark as selected.
     */
    public void markSelected(View view, int position) {
        SearchLog searchLog = (SearchLog) getItem(position);
        if (searchLog != null) {
            if (!isSearchLogSelected(searchLog)) {
                selectedItems.add(searchLog);

                ImageView imageView = (ImageView) view.findViewById(R.id.img);
                if (imageView != null) {
                    imageView.setBackgroundColor(SELECTED_IMAGE_VIEW_BACKGROUND_COLOR);
                    imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_accept));
                }
            }
        }
    }

    /**
     * deselects the search log represented with the given position.
     *
     * @param view     view of the search to updated.
     * @param position the position of the search log to update.
     */
    public void unMarkSelected(View view, int position) {
        SearchLog searchLog = (SearchLog) getItem(position);
        if (searchLog != null) {
            removeSelectedSearchLog(searchLog);
            ImageView imageView = (ImageView) view.findViewById(R.id.img);
            if (imageView != null) {
                imageView.setImageDrawable(getSearchLogImageViewDrawable(searchLog));
            }
        }
    }


    /**
     * removes the given search log from the list of selected items.
     *
     * @param searchLog the search log to remove from the selected list.
     */
    private void removeSelectedSearchLog(SearchLog searchLog) {
        SearchLog existing = null;
        for (SearchLog log : selectedItems) {
            if (log.getId().equals(searchLog.getId())) {
                existing = log;
                break;
            }
        }

        if (existing != null)
            selectedItems.remove(existing);
    }


    /**
     * checks whether the given search log is already selected.
     *
     * @param searchLog the search to check for.
     * @return true if the search log is already selected otherwise false.
     */
    public boolean isSearchLogSelected(SearchLog searchLog) {
        for (SearchLog log : selectedItems) {
            if (log.getId().equals(searchLog.getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * deletes the selected items from the data store
     */
    public void deletedSelectedItems() {
        menuItemService.deleteSearchLogs(selectedItems);
        selectedItems.clear();

        refreshData();

    }

    /**
     * clears the selected items
     */
    public void clearSelectedItems() {
        this.selectedItems.clear();
        refreshData();
    }
}
