package com.fer.hr.activity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.rest.dto.discover.SimpleCubeElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by igor on 17/01/16.
 */
public class FilterAdapter extends ArrayAdapter<SimpleCubeElement> {
    private LinkedHashMap<Integer, SimpleCubeElement> checkedMembers = new LinkedHashMap<>();

    public FilterAdapter(Context context, List<SimpleCubeElement> objects) {
        super(context, -1, objects);
    }

    public List<SimpleCubeElement> getCheckedItems() {
        ArrayList<SimpleCubeElement> result = new ArrayList<>();

        for (Map.Entry<Integer, SimpleCubeElement> entry : checkedMembers.entrySet()) {
            result.add(entry.getValue());
        }
        return result;
    }

    public void setCheckedItem(int position) {
        SimpleCubeElement member = checkedMembers.get(position);
        if(member == null) checkedMembers.put(position, getItem(position));
        else checkedMembers.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row_text_checkbox, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        final ViewHolder holder = (ViewHolder)convertView.getTag();

        holder.textLbl.setText(getItem(position).getCaption());
        SimpleCubeElement filter = checkedMembers.get(position);
        holder.filterChkBox.setChecked(filter != null);

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.rootLyt)
        LinearLayout rootLyt;
        @Bind(R.id.textLbl)
        TextView textLbl;
        @Bind(R.id.itemChkBox)
        CheckBox filterChkBox;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
