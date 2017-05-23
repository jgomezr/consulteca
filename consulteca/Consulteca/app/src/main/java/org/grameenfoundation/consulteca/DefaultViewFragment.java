package org.grameenfoundation.consulteca;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import org.grameenfoundation.consulteca.model.ListObject;
import org.grameenfoundation.consulteca.ui.*;

import java.util.Stack;

/**
 * This is a fragment that handles the display of the default view.
 */
public class DefaultViewFragment extends Fragment implements ActionMode.Callback {
    public static final String FRAGMENT_TAG = "org.grameenfoundation.consulteca.ui.DefaultViewFragment";
    private static final String NAVIGATION_STACK_SAVED_STATE_KEY = "navigation_stack_state_key";
    private Stack<ListObject> listObjectNavigationStack = null;
    private ListView mainListView;
    private MenuItem backNavigationMenuItem = null;
    private ActionMode actionMode;

    public DefaultViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(NAVIGATION_STACK_SAVED_STATE_KEY)) {
            setListObjectNavigationStack((Stack<ListObject>) savedInstanceState.get(NAVIGATION_STACK_SAVED_STATE_KEY));
        } else {
            setListObjectNavigationStack(new Stack<ListObject>());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view_fragment, container, false);
        initMainListView(view);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //save state of
        if (getListObjectNavigationStack() != null && !getListObjectNavigationStack().isEmpty()) {
            outState.putSerializable(NAVIGATION_STACK_SAVED_STATE_KEY, getListObjectNavigationStack());
        }
    }

    /**
     *
     */
    private void resetDisplayMenus() {
        if (getListObjectNavigationStack() != null) {
            getListObjectNavigationStack().clear();

            listViewBackNavigation();
        }
    }

    private void initMainListView(View container) {
        setMainListView((ListView) container.findViewById(R.id.main_list));

        final MainListViewAdapter listViewAdapter = new MainListViewAdapter(getActivity());
        getMainListView().setAdapter(listViewAdapter);

        getMainListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListObject itemToSelect = (ListObject) listViewAdapter.getItem(position);
                selectListElement(itemToSelect, listViewAdapter);

                if (actionMode == null)
                    actionMode = getActivity().startActionMode(DefaultViewFragment.this);
            }
        });

        getMainListView().setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return super.onTouch(v, event);
            }

            @Override
            public boolean onSwipeRight() {
                super.onSwipeRight();
                listViewBackNavigation();
                return false;
            }
        });

        if (!getListObjectNavigationStack().isEmpty()) {
            selectListElement(getListObjectNavigationStack().pop(), listViewAdapter);
        }

        if(backNavigationMenuItem != null){
            backNavigationMenuItem.setVisible(true);
        }
    }

    private void selectListElement(final ListObject itemToSelect, MainListViewAdapter listViewAdapter) {
        if (listViewAdapter.hasChildren(itemToSelect)) {
            listViewAdapter.setSelectedObject(itemToSelect);
            getListObjectNavigationStack().push(listViewAdapter.getSelectedObject());

            if (backNavigationMenuItem != null) {
                //backNavigationMenuItem.setVisible(true);
                getActivity().startActionMode(this);
            }
        } else {

            //if (SettingsManager.getInstance().
            //        getBooleanValue(SettingsConstants.KEY_CLIENT_IDENTIFIER_PROMPTING_ENABLED, false)) {
            //option overridden in gf-search ckw
            if(true){
                SingleInputPromptDialog dialog = new SingleInputPromptDialog(getActivity(), R.string.clientid_dialog_title,
                        R.string.clientid_dialog_message) {
                    @Override
                    protected boolean onOkClicked(String input) {
                        Intent intent = new Intent().setClass(getActivity(), SearchMenuItemActivity.class);
                        intent.putExtra(SearchMenuItemActivity.EXTRA_LIST_OBJECT_IDENTIFIER, itemToSelect);
                        intent.putExtra(SearchMenuItemActivity.CLIENT_IDENTIFIER, input);
                        intent.putExtra(SearchMenuItemActivity.BREAD_CRUMB, createBreadCrumb(itemToSelect));
                        startActivityForResult(intent, 0);

                        return true;
                    }
                };
                dialog.show();
            } else {
                Intent intent = new Intent().setClass(getActivity(), SearchMenuItemActivity.class);
                intent.putExtra(SearchMenuItemActivity.EXTRA_LIST_OBJECT_IDENTIFIER, itemToSelect);
                this.startActivityForResult(intent, 0);
            }
        }
    }

    private String createBreadCrumb(ListObject currentItem) {
        StringBuilder breadCrumb = new StringBuilder();
        boolean isCategory = true;
        for(int i=1; i< getListObjectNavigationStack().size(); i++){
            ListObject menuItem = getListObjectNavigationStack().get(i);
            if(isCategory) {
                breadCrumb.append(menuItem.getLabel() + "|");
                isCategory = false;
            } else {
                breadCrumb.append(menuItem.getLabel() + " ");
            }
        }
        return breadCrumb.toString() + currentItem.getLabel();
    }


    public boolean listViewBackNavigation() {
        MainListViewAdapter listViewAdapter = (MainListViewAdapter) getMainListView().getAdapter();

        //we pop the stack twice to get the right navigation element.
        if (!getListObjectNavigationStack().isEmpty())
            getListObjectNavigationStack().pop();

        if (!getListObjectNavigationStack().isEmpty()) {
            listViewAdapter.setSelectedObject(getListObjectNavigationStack().peek());
            return false;
        }

        if (getListObjectNavigationStack().isEmpty()) {
            listViewAdapter.setSelectedObject(null);
            if(backNavigationMenuItem != null) {
                backNavigationMenuItem.setVisible(false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.default_view_fragment, menu);

        backNavigationMenuItem = menu.findItem(R.id.action_nav_back);
        if (getListObjectNavigationStack() != null
                && !getListObjectNavigationStack().isEmpty()) {
            backNavigationMenuItem.setVisible(true);
        }
        actionMode = mode;

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_nav_back:
                listViewBackNavigation();
                if (getListObjectNavigationStack().isEmpty()) {
                    mode.finish();
                }

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
    }

    public Stack<ListObject> getListObjectNavigationStack() {
        return listObjectNavigationStack;
    }

    public void setListObjectNavigationStack(Stack<ListObject> listObjectNavigationStack) {
        this.listObjectNavigationStack = listObjectNavigationStack;
    }

    public ListView getMainListView() {
        return mainListView;
    }

    public void setMainListView(ListView mainListView) {
        this.mainListView = mainListView;
    }
}
