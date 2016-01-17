package com.fer.hr.activity.adapters;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.activity.OlapNavigator;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final SparseArray<OlapNavigator.Group> groups;
    public LayoutInflater inflater;
    public Activity activity;

    public MyExpandableListAdapter(Activity act, SparseArray<OlapNavigator.Group> groups) {
        activity = act;
        this.groups = groups;
        inflater = act.getLayoutInflater();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).children.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String childData = (String) getChild(groupPosition, childPosition);
        ViewHolderRow holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_dimension_selecter, parent, false);
            holder = new ViewHolderRow(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderRow) convertView.getTag();
        }

        holder.levelLbl.setText(childData);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        OlapNavigator.Group group = (OlapNavigator.Group) getGroup(groupPosition);
        ViewHolderHeader holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_header, parent, false);
            holder = new ViewHolderHeader(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderHeader) convertView.getTag();
        }
        holder.headerLbl.setText(group.string);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'list_row_header.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */


    static class ViewHolderHeader {
        @Bind(R.id.headerLbl)
        TextView headerLbl;

        ViewHolderHeader(View view) {
            ButterKnife.bind(this, view);
        }
    }


    static class ViewHolderRow {
        @Bind(R.id.levelLbl)
        TextView levelLbl;
        @Bind(R.id.rowsImg)
        FrameLayout rowsImg;
        @Bind(R.id.collsImg)
        FrameLayout collsImg;

        ViewHolderRow(View view) {
            ButterKnife.bind(this, view);
        }
    }
}