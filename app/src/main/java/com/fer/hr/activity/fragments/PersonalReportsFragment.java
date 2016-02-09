package com.fer.hr.activity.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.activity.OlapNavigator;
import com.fer.hr.activity.TableResultActivity;
import com.fer.hr.activity.adapters.ReportsAdapter;
import com.fer.hr.data.Profile;
import com.fer.hr.model.Report;
import com.fer.hr.services.gcm.GcmIntentService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

public class PersonalReportsFragment extends Fragment {

    @Bind(R.id.personalReportLst)
    ListView personalReportLst;

    private List<Report> reports;
    private ReportsAdapter reportsAdapter;
    private Profile appProfile;

    public PersonalReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appProfile = new Profile(getContext());
        reports = appProfile.getAllPersonalReports();
        reportsAdapter = new ReportsAdapter(getContext(), reports, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_personal_reports, container, false);
        ButterKnife.bind(this, v);
        personalReportLst.setAdapter(reportsAdapter);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnItemClick(R.id.personalReportLst)
    void reportClick(int position) {
        if (position == 0) startActivity(new Intent(getContext(), OlapNavigator.class));
        else {
            Report r = reports.get(position);
            Intent i = new Intent(getContext(), TableResultActivity.class);
            i.putExtra(TableResultActivity.TITLE_KEY, r.getReportName());
            i.putExtra(TableResultActivity.MDX_KEY, r.getMdx());
            i.putExtra(TableResultActivity.CUBE_KEY, r.getCube());
            startActivity(i);
        }
    }

    @OnItemLongClick(R.id.personalReportLst)
    boolean reportLongClick(int position) {
        if (position <= 0) return true;

        final Dialog d = new Dialog(getContext());
        d.setTitle("Choose action");
        d.setContentView(R.layout.dialog_delete_report);
        TextView delete = (TextView) d.findViewById(R.id.deleteLbl);
        TextView cancel = (TextView) d.findViewById(R.id.cancelLbl);

        Report p = reports.get(position);
        delete.setOnClickListener(v -> {
            reports.remove(position);
            appProfile.removePersonalReport(p);
            refreshReportsData();
            d.dismiss();
        });

        cancel.setOnClickListener(v -> {
            d.dismiss();
        });
        d.show();
        return true;
    }

    private void refreshReportsData() {
        reports.clear();
        reports.addAll(appProfile.getAllPushReports());
        reportsAdapter.notifyDataSetChanged();
    }

}
