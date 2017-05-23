package org.grameenfoundation.consulteca;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import org.grameenfoundation.consulteca.model.ListObject;
import org.grameenfoundation.consulteca.settings.SettingsConstants;
import org.grameenfoundation.consulteca.settings.SettingsManager;
import org.grameenfoundation.consulteca.ui.FavouriteListViewAdapter;
import org.grameenfoundation.consulteca.ui.SearchMenuItemActivity;
import org.grameenfoundation.consulteca.ui.SingleInputPromptDialog;

/**
 * This fragment is responsible for viewing the favourite
 * search items.
 */
public class FavouriteViewFragment extends Fragment {
    public static final String FRAGMENT_TAG = "org.grameenfoundation.consulteca.FavouriteViewFragment";
    private ListView mainListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view_fragment, container, false);

        initMainListView(view);
        return view;
    }

    private void initMainListView(View container) {
        mainListView = (ListView) container.findViewById(R.id.main_list);
        mainListView.setAdapter(new FavouriteListViewAdapter(getActivity()));

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListObject itemToSelect = (ListObject) mainListView.getAdapter().getItem(position);
                selectListElement(itemToSelect, mainListView.getAdapter());
            }
        });

    }

    private void selectListElement(final ListObject listObject, ListAdapter adapter) {
        if (SettingsManager.getInstance().
                getBooleanValue(SettingsConstants.KEY_CLIENT_IDENTIFIER_PROMPTING_ENABLED, false)) {

            SingleInputPromptDialog dialog = new SingleInputPromptDialog(getActivity(), R.string.clientid_dialog_title,
                    R.string.clientid_dialog_message) {
                @Override
                protected boolean onOkClicked(String input) {
                    Intent intent = new Intent().setClass(getActivity(), SearchMenuItemActivity.class);
                    intent.putExtra(SearchMenuItemActivity.EXTRA_LIST_OBJECT_IDENTIFIER, listObject);
                    intent.putExtra(SearchMenuItemActivity.CLIENT_IDENTIFIER, input);
                    startActivityForResult(intent, 0);

                    return true;
                }
            };
            dialog.show();
        } else {
            Intent intent = new Intent().setClass(getActivity(), SearchMenuItemActivity.class);
            intent.putExtra(SearchMenuItemActivity.EXTRA_LIST_OBJECT_IDENTIFIER, listObject);
            this.startActivityForResult(intent, 0);
        }
    }
}
