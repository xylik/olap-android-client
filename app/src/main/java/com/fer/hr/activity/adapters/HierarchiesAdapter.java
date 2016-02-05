package com.fer.hr.activity.adapters;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.model.Level;
import com.fer.hr.utils.PixelUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HierarchiesAdapter extends BaseExpandableListAdapter {
    private Context ctx;
    private List<Level> levels;
    private String hierarchyName;
    private DimensionsAdapter.OnChildItemClickListener listener;

    public HierarchiesAdapter(Context ctx, String hierarchyName, List<Level> levels) {
        this.ctx = ctx;
        this.levels = levels;
        this.hierarchyName = hierarchyName;
    }

    public void setOnChildClickListener(DimensionsAdapter.OnChildItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return levels.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row_dimension_selecter, parent, false);
            convertView.setTag(new ChildViewHolder(convertView));
        }
        ChildViewHolder holder = (ChildViewHolder)convertView.getTag();
        final Level level = (Level) getChild(groupPosition, childPosition);

        holder.levelLbl.setText(level.getData().getName());

        holder.collsImg.setImageResource(R.drawable.column_xxhdpi);
        holder.rowsImg.setImageResource(R.drawable.row_xxhdpi);
        holder.filterImg.setImageResource(R.drawable.filter_xxhdpi);
        if(level.getState() == Level.State.COLLUMNS) holder.collsImg.setImageResource(R.drawable.column_active_xxhdpi);
        else if(level.getState() == Level.State.ROWS) holder.rowsImg.setImageResource(R.drawable.row_active_xxhdpi);
        else if(level.getState() == Level.State.FILTER) holder.filterImg.setImageResource(R.drawable.filter_active_xxhdpi);

//        Level.State currentState = level.getState();
        holder.collsImg.setOnClickListener( v -> {
            if(listener == null) return;
            Level.State newState = level.getState() != Level.State.COLLUMNS?  Level.State.COLLUMNS : Level.State.NEUTRAL;
            listener.onChildClick(holder.collsImg, level, newState);
        });
        holder.rowsImg.setOnClickListener( v -> {
            if(listener == null) return;
            Level.State newState = level.getState() != Level.State.ROWS?  Level.State.ROWS : Level.State.NEUTRAL;
            listener.onChildClick(holder.rowsImg, level, newState);
        });
        holder.filterImg.setOnClickListener(v -> {
            if(listener == null) return;
            Level.State newState = level.getState() != Level.State.FILTER?  Level.State.FILTER : Level.State.NEUTRAL;
            listener.onChildClick(holder.filterImg, level, newState);
        });

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return levels.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return levels;
    }

    @Override
    public int getGroupCount() {
        return 1;
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row_txt_collapse, parent, false);
            convertView.setTag(new GroupViewHolder(convertView));
        }
        GroupViewHolder holder = (GroupViewHolder)convertView.getTag();
        holder.headerLbl.setText(hierarchyName);
        holder.headerLbl.setCompoundDrawables(null, null, null, null);
        holder.headerLbl.setPadding(PixelUtil.dpToPx(28, ctx), 0, 0, 0);
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

    static class GroupViewHolder {
        @Bind(R.id.headerLbl)
        TextView headerLbl;

        GroupViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ChildViewHolder {
        @Bind(R.id.rootLyt)
        RelativeLayout rootLyt;
        @Bind(R.id.levelLbl)
        TextView levelLbl;
        @Bind(R.id.collsImg)
        ImageView collsImg;
        @Bind(R.id.rowsImg)
        ImageView rowsImg;
        @Bind(R.id.filterImg)
        ImageView filterImg;

        ChildViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}