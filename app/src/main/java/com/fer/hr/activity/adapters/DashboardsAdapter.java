package com.fer.hr.activity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.model.PushReport;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by igor on 24/01/16.
 */
public class DashboardsAdapter extends ArrayAdapter<PushReport> {

    public DashboardsAdapter(Context context, List<PushReport> objects) {
        super(context, -1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row_picture_header_desc, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        int drawableId = R.drawable.report_icon_2;
        if(position == 0) drawableId = R.drawable.report_icon;

        holder.dashboardImg.setImageDrawable(getContext().getResources().getDrawable(drawableId));
        holder.headerLbl.setText(getItem(position).getReportName());
        holder.descriptionLbl.setVisibility(View.GONE);

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.dashboardImg)
        ImageView dashboardImg;
        @Bind(R.id.headerLbl)
        TextView headerLbl;
        @Bind(R.id.descriptionLbl)
        TextView descriptionLbl;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
