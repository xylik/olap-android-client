package com.fer.hr.activity.adapters;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.rest.dto.discover.SaikuCube;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by igor on 15/01/16.
 */
public class CubesAdapter extends ArrayAdapter<SaikuCube> {
    private List<SaikuCube> cubesList;
    private int selectedItemIndx = 0;

    public CubesAdapter(Context context, List<SaikuCube> cubesList) {
        super(context, -1, cubesList);
        this.cubesList = cubesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE));
            convertView = inflater.inflate(R.layout.list_row_text_radiobtn, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        final ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.cubeNameTxt.setText( getItem(position).getName() );
        holder.cubeCatalogTxt.setVisibility(View.GONE);
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
        @Bind(R.id.headerLbl)
        TextView cubeNameTxt;
        @Bind(R.id.descriptionLbl)
        TextView cubeCatalogTxt;
        @Bind(R.id.radioBtn)
        RadioButton radioBtn;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
