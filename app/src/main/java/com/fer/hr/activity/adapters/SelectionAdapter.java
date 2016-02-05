package com.fer.hr.activity.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.model.SelectionEntity;
import com.fer.hr.model.SelectionGroup;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by igor on 21/01/16.
 */
public class SelectionAdapter extends BaseExpandableListAdapter {

    public static interface OnChildItemClickListener {
        public void onChildClick(SelectionEntity entity, int groupPosition, int childPosition);
    }

    private Activity parent;
    private HashMap<Integer, SelectionGroup> groups;
    private LayoutInflater inflater;
    private OnChildItemClickListener listener;

    public SelectionAdapter(Activity activity, HashMap<Integer, SelectionGroup> groups, OnChildItemClickListener listener) {
        this.parent = activity;
        this.groups = groups;
        inflater = activity.getLayoutInflater();
        this.listener = listener;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getEntities().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final SelectionEntity entity = (SelectionEntity) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_selection, parent, false);
            convertView.setTag(new ViewHolderChild(convertView));
        }
        final ViewHolderChild holder = (ViewHolderChild) convertView.getTag();

        holder.entityTitleLbl.setText(entity.getCaption());
        holder.entityDescLbl.setVisibility(View.GONE);

        holder.removeItemImg.setOnClickListener(v -> {
            SelectionEntity e = (SelectionEntity) getChild(groupPosition, childPosition);
            listener.onChildClick(e, groupPosition, childPosition);
        });

        int headerColor;
        switch(groupPosition) {
            case 0: headerColor = parent.getResources().getColor(R.color.green_accent); break;
            case 1: headerColor = parent.getResources().getColor(R.color.brown_accent); break;
            case 2: headerColor = parent.getResources().getColor(R.color.purple_accent); break;
            case 3: headerColor = parent.getResources().getColor(R.color.light_gray_accent); break;
            default: throw new IllegalArgumentException("Unexpected group position!");
        }

        Drawable background = holder.headerColor.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable)background).getPaint().setColor(headerColor);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable)background).setColor(headerColor);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).getEntities().size();
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
        SelectionGroup group = (SelectionGroup) getGroup(groupPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_header, parent, false);
            convertView.setTag(new ViewHolderGroup(convertView));
        }
        final ViewHolderGroup holder = (ViewHolderGroup) convertView.getTag();

        holder.headerLbl.setText(group.getCaption());

        int headerColor;
        switch(groupPosition) {
            case 0: headerColor = parent.getResources().getColor(R.color.green); break;
            case 1: headerColor = parent.getResources().getColor(R.color.brown); break;
            case 2: headerColor = parent.getResources().getColor(R.color.purple); break;
            case 3: headerColor = parent.getResources().getColor(R.color.light_gray); break;
            default: throw new IllegalArgumentException("Unexpected group position!");
        }

        Drawable background = holder.headerColor.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable)background).getPaint().setColor(headerColor);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable)background).setColor(headerColor);
        }

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

    static class ViewHolderGroup {
        @Bind(R.id.headerColor)
        View headerColor;
        @Bind(R.id.headerLbl)
        TextView headerLbl;

        ViewHolderGroup(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ViewHolderChild {
        @Bind(R.id.headerColor)
        View headerColor;
        @Bind(R.id.entityTitleLbl)
        TextView entityTitleLbl;
        @Bind(R.id.entityDescLbl)
        TextView entityDescLbl;
        @Bind(R.id.removeItemImg)
        ImageView removeItemImg;

        ViewHolderChild(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
