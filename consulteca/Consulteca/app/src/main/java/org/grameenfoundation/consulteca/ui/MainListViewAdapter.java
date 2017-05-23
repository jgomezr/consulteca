package org.grameenfoundation.consulteca.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.grameenfoundation.consulteca.ApplicationRegistry;
import org.grameenfoundation.consulteca.R;
import org.grameenfoundation.consulteca.model.ListObject;
import org.grameenfoundation.consulteca.model.SearchMenu;
import org.grameenfoundation.consulteca.model.SearchMenuItem;
import org.grameenfoundation.consulteca.services.MenuItemService;
import org.grameenfoundation.consulteca.synchronization.SynchronizationManager;
import org.grameenfoundation.consulteca.utils.ImageUtils;
import org.joda.time.Interval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Custom Adapter that is the backing object of the Main ListView of the application.
 */
public class MainListViewAdapter extends BaseAdapter {
    private ListObject selectedObject;
    protected MenuItemService menuItemService = new MenuItemService();
    private Object items = null;
    private Context context;
    protected LayoutInflater layoutInflater;
    private Handler handler = null;

    public MainListViewAdapter(Context context) {
        super();
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        handler = new Handler();
    }

    public static class ThumbnailViewHolder {
        public ImageView imageView;
        public int position;
    }

    public static class ThumbnailTask<T> extends AsyncTask<T, Interval, Drawable> {
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

    @Override
    public int getCount() {
        int count = 0;
        if (selectedObject != null) {
            if (selectedObject instanceof SearchMenuItem) {
                return menuItemService.countSearchMenuItems((SearchMenuItem) selectedObject);
            } else if (selectedObject instanceof SearchMenu) {
                return menuItemService.countTopLevelSearchMenuItems((SearchMenu) selectedObject);
            }

        } else {
            items = menuItemService.getAllSearchMenus();
            count = menuItemService.countSearchMenus();

            /**
             * this is here so that we can show the button synchronization view in the
             * interface.
             * It's important to set the count to 1 so that getView is called by the list view
             * control bound to this Adapter.
             */
            if (count <= 0)
                count = 1;
        }

        return count;
    }

    @Override
    public Object getItem(int position) {
        if (selectedObject != null) {
            if (selectedObject instanceof SearchMenuItem) {
                List<SearchMenuItem> searchMenuItems = (List<SearchMenuItem>) items;
                return searchMenuItems.get(position);
            } else if (selectedObject instanceof SearchMenu) {
                List<SearchMenu> searchMenus = (List<SearchMenu>) items;
                return searchMenus.get(position);
            }
        } else {
            //we assume it's the first time to access the adapter
            List<SearchMenu> menus = (List<SearchMenu>) items;
            if (menus != null) {
                return menus.get(position);
            }
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

        if (hasData()) {
            if (rowView == null || rowView.findViewById(R.id.sync_button) != null) {
                rowView = layoutInflater.inflate(R.layout.listviewobject, parent, false);
            }

            createListViewItemView(position, rowView);
        } else {
            if (rowView == null)
                rowView = layoutInflater.inflate(R.layout.listview_sync_button, parent, false);

            Button button = (Button) rowView.findViewById(R.id.sync_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SynchronizationManager.getInstance().start();
                }
            });
        }


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

        ListObject listObject = (ListObject) getItem(position);
        if (listObject != null) {
            titleView.setText(listObject.getLabel());
            if (listObject.getDescription() != null && listObject.getDescription().startsWith("No Content")) {
                descriptionView.setText("");
            } else {
                descriptionView.setText(listObject.getDescription());
            }

            descriptionView.setVisibility(TextView.VISIBLE);

            imageView.setTag(listObject);
            viewHolder.position = position;
            viewHolder.imageView = imageView;

            rowView.setTag(viewHolder);

            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (v.getTag() != null && v.getTag() instanceof ListObject) {
                            if (((ListObject) v.getTag()).isHasIcon()) {
                                Intent intent = new Intent().setClass(ApplicationRegistry.getMainActivity(),
                                        ImageViewerActivity.class);
                                intent.putExtra(ImageViewerActivity.EXTRA_LIST_OBJECT_IDENTIFIER, (ListObject) v.getTag());
                                ApplicationRegistry.getMainActivity().startActivityForResult(intent, 0);

                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

            new ThumbnailTask<ListObject>(viewHolder, position) {
                @Override
                protected Drawable doInBackground(ListObject... params) {
                    return getListObjectDrawable(params[0]);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, listObject);
        }
    }

    private boolean hasData() {
        if (menuItemService.countSearchMenus() > 0) {
            return true;
        }

        return false;
    }

    protected static Drawable getListObjectDrawable(ListObject listObject) {
        File imageCacheDir = ImageUtils.createImageCacheFolderIfNotExists(ApplicationRegistry.getApplicationContext());
        Drawable drawable = null;
        if (ImageUtils.imageExists(listObject.getId(), true)) {
            String originalImagePath = ImageUtils.getFullPath(listObject.getId(), true);
            if (originalImagePath != null) {
                String cacheImageFile = imageCacheDir + originalImagePath.substring(originalImagePath.lastIndexOf("/"));
                drawable = ImageUtils.loadBitmapDrawableIfExists(ApplicationRegistry.getApplicationContext(),
                        cacheImageFile);
                if (drawable == null) {
                    drawable = ImageUtils.scaleAndCacheImage(ApplicationRegistry.getApplicationContext(),
                            originalImagePath, cacheImageFile, 50, 50);
                } else {
                    listObject.setHasIcon(true);
                }
            }
        } else {
            int width = 50, height = 50;
            drawable = ImageUtils.drawRandomColorImageWithText(ApplicationRegistry.getApplicationContext(),
                    listObject.getLabel().substring(0, 1).toUpperCase(), width, height);
        }

        return drawable;
    }

    protected static void saveDrawableToFile(Drawable drawable, String outputPath) {
        try {
            FileOutputStream out = new FileOutputStream(outputPath);

            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
        } catch (FileNotFoundException e) {
            Log.e(MainListViewAdapter.class.getName(), "UnExpected Exception", e);
        }
    }

    public ListObject getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(ListObject selectedObject) {
        this.selectedObject = selectedObject;

        if (this.selectedObject instanceof SearchMenu) {
            items = menuItemService.getTopLevelSearchMenuItems((SearchMenu) this.selectedObject);
            notifyDataSetChanged();
        } else if (this.selectedObject instanceof SearchMenuItem) {
            items = menuItemService.getSearchMenuItems((SearchMenuItem) this.selectedObject);
            notifyDataSetChanged();
        } else {
            items = menuItemService.getAllSearchMenus();
            notifyDataSetChanged();
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

    /**
     * checks whether the given list object has any children
     *
     * @param listObject
     * @return
     */
    public boolean hasChildren(ListObject listObject) {
        return menuItemService.hasChildren(listObject);
    }

    public void refreshData() {
        notifyDataSetChanged();
    }
}
