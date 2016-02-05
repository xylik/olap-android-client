package com.fer.hr.activity.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.activity.test.CustExpListview;
import com.fer.hr.activity.test.SecondLevelAdapter;
import com.fer.hr.model.Dimension;
import com.fer.hr.model.Hierarchy;
import com.fer.hr.model.Level;
import com.fer.hr.rest.dto.discover.SaikuHierarchy;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DimensionsAdapter extends BaseExpandableListAdapter {
    public static interface OnChildItemClickListener {
        void onChildClick(View childView, Level level, Level.State newState);
    }

    private HashMap<Integer, Dimension> groups;
    public LayoutInflater inflater;
    public Context ctx;
    private OnChildItemClickListener listener;

    public DimensionsAdapter(Context ctx, HashMap<Integer, Dimension> dimensions) {
        this.ctx = ctx;
        this.groups = dimensions;
        inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnChildClickListener(OnChildItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getHierarchies().get(childPosition); //return hierarchy
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Hierarchy h = (Hierarchy)getChild(groupPosition, childPosition);
        CustExpListview hierarchiesExpList = new CustExpListview(ctx);
        HierarchiesAdapter hAdapter = new HierarchiesAdapter(ctx, h.getData().getCaption(), h.getLevels());
        hAdapter.setOnChildClickListener(listener);
        hierarchiesExpList.setAdapter(hAdapter);
//        hierarchiesExpList.setGroupIndicator(null);
        return hierarchiesExpList;
//        holder.filterImg.setOnClickListener(v -> {
//            Level.State newState = level.getState() != Level.State.FILTER?  Level.State.FILTER : Level.State.NEUTRAL;
//            listener.onChildClick(holder.filterImg, groupPosition,childPosition, newState);
//        });
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).getHierarchies().size();
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_txt_collapse, parent, false);
            convertView.setTag(new ViewHolderHeader(convertView));
        }
        ViewHolderHeader holder = (ViewHolderHeader)convertView.getTag();
        final Dimension dimension = (Dimension) getGroup(groupPosition);

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
}