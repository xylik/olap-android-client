package com.fer.hr.activity.adapters;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fer.hr.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by igor on 15/01/16.
 */
public class CubesListAdapter<T> extends ArrayAdapter<T> {
    private List<T> cubesList;
    private int selectedItemIndx = 0;

    public CubesListAdapter(Context context, int layoutResId, List<T> cubesList) {
        super(context, layoutResId, cubesList);
        this.cubesList = cubesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = ((LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE));
            convertView = inflater.inflate(R.layout.list_row_text_radiobtn, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.cubeNameTxt.setText((String) getItem(position));
        holder.radioBtn.setChecked(selectedItemIndx == position);
        return convertView;
    }

    public int getSelectedItemIndx() {
        return selectedItemIndx;
    }

    public void setSelectedItemIndx(int selectedItemIndx) {
        this.selectedItemIndx = selectedItemIndx;
    }


    static class ViewHolder {
        @Bind(R.id.cubeNameTxt)
        TextView cubeNameTxt;
        @Bind(R.id.cubeCatalogTxt)
        TextView cubeCatalogTxt;
        @Bind(R.id.txtContainerLyt)
        LinearLayout txtContainerLyt;
        @Bind(R.id.radioBtn)
        RadioButton radioBtn;
        @Bind(R.id.rootLyt)
        RelativeLayout rootLyt;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}