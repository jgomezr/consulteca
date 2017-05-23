package org.grameenfoundation.consulteca.interactivecontent;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import org.grameenfoundation.consulteca.R;
import org.grameenfoundation.consulteca.model.ListObject;

import java.util.Stack;

/**
 * This is a fragment that handles the listing of the interactive content
 */
public class InteractiveContentViewFragment extends Fragment implements ActionMode.Callback {
    public static final String FRAGMENT_TAG =
            "org.grameenfoundation.consulteca.interactivecontent.InteractiveContentViewFragment";
    private static final String NAVIGATION_STACK_SAVED_STATE_KEY = "navigation_stack_state_key";
    private Stack<ListObject> listObjectNavigationStack = null;
    private ListView contentListView;
    private ActionMode actionMode;

    public InteractiveContentViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(NAVIGATION_STACK_SAVED_STATE_KEY)) {
            listObjectNavigationStack =
                    (Stack<ListObject>) savedInstanceState.get(NAVIGATION_STACK_SAVED_STATE_KEY);
        } else {
            listObjectNavigationStack = new Stack<ListObject>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.interactive_content_view_fragment, container, false);
        initContentListView(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (listObjectNavigationStack != null && !listObjectNavigationStack.isEmpty()) {
            outState.putSerializable(NAVIGATION_STACK_SAVED_STATE_KEY, listObjectNavigationStack);
        }
    }

    private void initContentListView(View container) {
        contentListView = (ListView) container.findViewById(R.id.interactive_content_list);

        final InteractiveContentListViewAdapter listViewAdapter = new InteractiveContentListViewAdapter(getActivity());
        contentListView.setAdapter(listViewAdapter);

        contentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contentItem = (String) listViewAdapter.getItem(position);
                selectListElement(contentItem);

                if (actionMode == null)
                    actionMode = getActivity().startActionMode(InteractiveContentViewFragment.this);
            }
        });
    }

    private void selectListElement(final String contentItem) {
        Intent intent = new Intent().setClass(getActivity(), ContentViewerActivity.class);
        intent.putExtra(ContentViewerActivity.EXTRA_CONTENT_IDENTIFIER, contentItem);
        this.startActivityForResult(intent, 0);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
    }
}
