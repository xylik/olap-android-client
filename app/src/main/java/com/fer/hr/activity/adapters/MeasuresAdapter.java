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
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by igor on 17/01/16.
 */
public class MeasuresAdapter extends ArrayAdapter<SaikuMeasure> {
    private LinkedHashMap<Integer, SaikuMeasure> checkedMembers = new LinkedHashMap<>();

    public MeasuresAdapter(Context context, List<SaikuMeasure> objects) {
        super(context, -1, objects);
    }

    public List<SaikuMeasure> getCheckedItems() {
        ArrayList<SaikuMeasure> result = new ArrayList<>();

        for (Map.Entry<Integer, SaikuMeasure> entry : checkedMembers.entrySet()) {
            result.add(entry.getValue());
        }
        return result;
    }

    public void setCheckedItem(int position) {
        SaikuMeasure member = checkedMembers.get(position);
        if(member == null) checkedMembers.put(position, getItem(position));
        else checkedMembers.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row_text_checkbox, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textLbl.setText(getItem(position).getName());
        SaikuMeasure measure = checkedMembers.get(position);
        holder.measureChkBox.setChecked(measure != null);

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.rootLyt)
        LinearLayout rootLyt;
        @Bind(R.id.textLbl)
        TextView textLbl;
        @Bind(R.id.measureChkBox)
        CheckBox measureChkBox;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
