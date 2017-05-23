package org.grameenfoundation.consulteca.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.grameenfoundation.consulteca.R;
import org.grameenfoundation.consulteca.model.Farmer;
import org.grameenfoundation.consulteca.services.MenuItemService;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2014 AppLab, Grameen Foundation
 * Created by: David                                       4
 *
 */
public class SearchFarmerAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<Farmer> resultList = new ArrayList<Farmer>();

    public SearchFarmerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Farmer getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.farmer_search_dropdown, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position).getFirstName() + " " + getItem(position).getLastName());
        ((TextView) convertView.findViewById(R.id.text2)).setText(getItem(position).getSubcounty() + " - " + getItem(position).getVillage());
        convertView.setTag(getItem(position).getId());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Farmer> farmers = findFarmer(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = farmers;
                    filterResults.count = farmers.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<Farmer>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    /**
     * Returns a search result for the given farmer name(s) in order
     * first_name[space]last_name.
     */
    private List<Farmer> findFarmer(String name) {
        MenuItemService menuItemService = new MenuItemService();
        return menuItemService.getFarmersByName(name);
    }
}
