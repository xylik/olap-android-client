package com.fer.hr.activity.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.model.Dimension;
import com.fer.hr.model.Level;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DimensionsAdapter extends BaseExpandableListAdapter {
    public static interface OnChildItemClickListener {
        void onChildClick(View childView, int groupPosition, int childPosition, Level.State newState);
    }

    private HashMap<Integer, Dimension> groups;
    public LayoutInflater inflater;
    public Activity activity;
    private OnChildItemClickListener listener;

    public DimensionsAdapter(Activity activity, HashMap<Integer, Dimension> dimensions) {
        activity = activity;
        this.groups = dimensions;
        inflater = activity.getLayoutInflater();
    }

    public void setOnChildClickListener(OnChildItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getLevels().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Level level = (Level) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_dimension_selecter, parent, false);
            convertView.setTag(new ViewHolderRow(convertView));
        }
        final ViewHolderRow holder = (ViewHolderRow) convertView.getTag();

        holder.levelLbl.setText(level.getData().getName());
//        holder.hierarchyLbl.setText(level.getData().getHierarchyUniqueName());
        holder.hierarchyLbl.setVisibility(View.GONE);


        holder.collsImg.setImageResource(R.drawable.column_icon);
        holder.rowsImg.setImageResource(R.drawable.row_icon);
        holder.filterImg.setImageResource(R.drawable.filter_icon);
        if(level.getState() == Level.State.COLLUMNS)
            holder.collsImg.setImageResource(R.drawable.delete_icon);
        else if(level.getState() == Level.State.ROWS)
            holder.rowsImg.setImageResource(R.drawable.delete_icon);
        else if(level.getState() == Level.State.FILTER)
            holder.filterImg.setImageResource(R.drawable.delete_icon);

//        Level.State currentState = level.getState();
        holder.collsImg.setOnClickListener( v -> {
            Level.State newState = level.getState() != Level.State.COLLUMNS?  Level.State.COLLUMNS : Level.State.NEUTRAL;
            listener.onChildClick(holder.collsImg, groupPosition,childPosition, newState);
        });
        holder.rowsImg.setOnClickListener( v -> {
            Level.State newState = level.getState() != Level.State.ROWS?  Level.State.ROWS : Level.State.NEUTRAL;
            listener.onChildClick(holder.rowsImg, groupPosition, childPosition, newState);
        });
//        holder.filterImg.setOnClickListener(v -> {
//            Level.State newState = level.getState() != Level.State.FILTER?  Level.State.FILTER : Level.State.NEUTRAL;
//            listener.onChildClick(holder.filterImg, groupPosition,childPosition, newState);
//        });

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).getLevels().size();
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
        Dimension dimension = (Dimension) getGroup(groupPosition);
        ViewHolderHeader holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_header, parent, false);
            holder = new ViewHolderHeader(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderHeader) convertView.getTag();
        }
        holder.headerLbl.setText(dimension.getData().getCaption());
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

    static class ViewHolderHeader {
        @Bind(R.id.headerLbl)
        TextView headerLbl;

        ViewHolderHeader(View view) {
            ButterKnife.bind(this, view);
        }
    }


    static class ViewHolderRow {
        @Bind(R.id.rootLyt)
        RelativeLayout rootLyt;
        @Bind(R.id.levelLbl)
        TextView levelLbl;
        @Bind(R.id.hierarchyLbl)
        TextView hierarchyLbl;
        @Bind(R.id.collsImg)
        ImageView collsImg;
        @Bind(R.id.rowsImg)
        ImageView rowsImg;
        @Bind(R.id.filterImg)
        ImageView filterImg;

        ViewHolderRow(View view) {
            ButterKnife.bind(this, view);
        }
    }
}