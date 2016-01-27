package com.fer.hr.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.activity.adapters.DashboardsAdapter;
import com.fer.hr.data.Profile;
import com.fer.hr.model.PushReport;
import com.fer.hr.services.gcm.GcmIntentService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;

public class DashboardActivity extends AppCompatActivity {
    @Bind(R.id.btnBack)
    ImageButton btnBack;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.actionBtn)
    ImageButton actionBtn;
    @Bind(R.id.dashboardsList)
    ListView dashboardsList;

    private boolean isRunning;
    private Profile appProfile;
    private List<PushReport> reports;
    private DashboardsAdapter reportsAdapter;
    private BroadcastReceiver pushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshReportsData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboards);
        ButterKnife.bind(this);
        appProfile = new Profile(this);
        isRunning = true;
        registerReceiver(pushReceiver, new IntentFilter(GcmIntentService.PUSH_RECEIVED));

        reports = appProfile.getAllPushReports();
        reportsAdapter = new DashboardsAdapter(this, reports);

        initView();
        setActions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        unregisterReceiver(pushReceiver);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void initView() {
        title.setText(getString(R.string.dshboardActTitle));
        dashboardsList.setAdapter(reportsAdapter);
    }

    private void setActions() {
        btnBack.setOnClickListener(v -> onBackPressed());
        actionBtn.setOnClickListener(v ->
        {
            appProfile.setAuthenticationToken(null);
            finish();
        });
    }

    @OnItemClick(R.id.dashboardsList)
    void reportClick(int position) {
        if (position == 0) startActivity(new Intent(this, OlapNavigator.class));
        else {
            PushReport r = reports.get(position);
            Intent i = new Intent(this, TableResultActivity.class);
            i.putExtra(TableResultActivity.TITLE_KEY, r.getReportName());
            i.putExtra(TableResultActivity.MDX_KEY, r.getMdx());
            i.putExtra(TableResultActivity.CUBE_KEY, r.getCube());
            startActivity(i);
        }
    }

    @OnItemLongClick(R.id.dashboardsList)
    boolean reportLongClick(int position) {
        if(position <= 0)  return true;

        final Dialog d = new Dialog(this);
        d.setTitle("Choose action");
        d.setContentView(R.layout.dialog_delete_report);
        TextView delete = (TextView)d.findViewById(R.id.deleteLbl);
        TextView cancel = (TextView)d.findViewById(R.id.cancelLbl);

        PushReport p = reports.get(position);
        delete.setOnClickListener(v -> {
            reports.remove(position);
            appProfile.removePushReport(p);
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
