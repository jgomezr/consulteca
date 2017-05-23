package org.grameenfoundation.consulteca;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import org.grameenfoundation.consulteca.model.SearchLog;
import org.grameenfoundation.consulteca.model.SearchMenuItem;
import org.grameenfoundation.consulteca.services.MenuItemService;
import org.grameenfoundation.consulteca.ui.RecentSearchesListViewAdapter;
import org.grameenfoundation.consulteca.ui.SearchMenuItemActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment class is responsible for viewing recent searches.
 */
public class RecentSearchesViewFragment extends Fragment implements ActionMode.Callback {
    public static final String FRAGMENT_TAG = "org.grameenfoundation.consulteca.RecentSearchesViewFragment";

    private ListView mainListView;
    private List<SearchLog> selectedItems = new ArrayList<SearchLog>();
    private ActionMode actionMode = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view_fragment, container, false);

        initMainListView(view);

        return view;
    }

    private void initMainListView(View container) {
        mainListView = (ListView) container.findViewById(R.id.main_list);
        mainListView.setAdapter(new RecentSearchesListViewAdapter(getActivity()));

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SearchLog searchLog = (SearchLog) mainListView.getAdapter().getItem(position);
                if (searchLog != null) {
                    selectListElement(searchLog);
                }
            }
        });

        mainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                RecentSearchesListViewAdapter listViewAdapter = (RecentSearchesListViewAdapter) mainListView.getAdapter();
                SearchLog searchLog = (SearchLog) listViewAdapter.getItem(position);
                if (searchLog != null && !listViewAdapter.isSearchLogSelected(searchLog)) {
                    listViewAdapter.markSelected(view, position);

                    if (actionMode == null)
                        actionMode = getActivity().startActionMode(RecentSearchesViewFragment.this);
                } else if (searchLog != null && listViewAdapter.isSearchLogSelected(searchLog)) {
                    listViewAdapter.unMarkSelected(view, position);
                }

                return true;
            }
        });

    }

    private void selectListElement(final SearchLog searchLog) {
        SearchMenuItem searchMenuItem = new MenuItemService().getSearchMenuItem(searchLog.getMenuItemId());
        if (searchMenuItem != null) {
            Intent intent = new Intent().setClass(getActivity(), SearchMenuItemActivity.class);
            intent.putExtra(SearchMenuItemActivity.EXTRA_LIST_OBJECT_IDENTIFIER, searchMenuItem);
            this.startActivityForResult(intent, 0);
        }

    }

    /**
     * deletes the selected items from the data store
     */
    private void deleteSelectedItems() {
        RecentSearchesListViewAdapter listViewAdapter = (RecentSearchesListViewAdapter) mainListView.getAdapter();
        listViewAdapter.deletedSelectedItems();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.recent_searches_context_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                deleteSelectedItems();
                actionMode.finish();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;

        RecentSearchesListViewAdapter listViewAdapter = (RecentSearchesListViewAdapter) mainListView.getAdapter();
        listViewAdapter.clearSelectedItems();
    }
}
