package com.fer.hr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.data.Profile;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

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
    private List<String> listData;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboards);
        ButterKnife.bind(this);
        appProfile = new Profile(this);
        isRunning = true;

        listData = Arrays.asList(new String("AD HOC") );
        listAdapter = new ArrayAdapter<String>(this, R.layout.list_row_picture_header_desc, R.id.headerLbl, listData);
        initView();
        setActions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void initView() {
        title.setText(getString(R.string.dshboardActTitle));
        dashboardsList.setAdapter(listAdapter);
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
    void onListClick(int position) {
        if(position == 0) startActivity(new Intent(this, OlapNavigator.class));
    }
}
