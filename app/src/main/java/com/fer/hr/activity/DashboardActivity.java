package com.fer.hr.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fer.hr.R;
import com.fer.hr.activity.fragments.PersonalReportsFragment;
import com.fer.hr.activity.fragments.PushReportsFragment;
import com.fer.hr.data.Profile;
import com.fer.hr.model.CubeWithMetaData;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.authentication.IAuthenticate;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DashboardActivity extends AppCompatActivity {
    @Bind(R.id.navBar)
    Toolbar navBar;
    @Bind(R.id.tabBar)
    TabLayout tabBar;
    @Bind(R.id.viewPager)
    ViewPager viewPager;
    ProgressDialog progressDialog;

    private IAuthenticate authenticationMng;
    private IRepository repositoryMng;
    private boolean isRunning = false;
    private Profile appProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboards);
        ButterKnife.bind(this);
        appProfile = new Profile(this);
        isRunning = true;

        authenticationMng = (IAuthenticate) ServiceProvider.getService(ServiceProvider.AUTHENTICATION);
        repositoryMng = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);

        progressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                finish();
                dismiss();
            }
        };
        progressDialog.setMessage("Syncing Reports");
        progressDialog.setCanceledOnTouchOutside(false);

        if (repositoryMng.isEmpty()) {
            repositoryMng.getFreshCubesMeta(cubesMetaCallback);
            progressDialog.show();
        }

        navBar.setNavigationIcon(R.drawable.icon_navbar_back);
        navBar.setTitle("REPORTS");
        navBar.setTitleTextColor(getResources().getColor(R.color.white));
        navBar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_more_vert_white_24dp));
        setSupportActionBar(navBar);

        setupViewPager(viewPager);
        tabBar = (TabLayout) findViewById(R.id.tabBar);
        tabBar.setupWithViewPager(viewPager);
        tabBar.getTabAt(0).setIcon(R.drawable.user_white_xxhdpi);
        tabBar.setTabTextColors(getResources().getColor(R.color.white), getResources().getColor(R.color.white));
//        tabBar.getTabAt(1).setIcon(R.drawable.www_white_large);

        initView();
        setActions();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PersonalReportsFragment(), "");
        adapter.addFragment(new PushReportsFragment(), "WWW");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logoutMItem) {
            appProfile.setAuthenticationToken(null);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
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
    }

    private void setActions() {
        navBar.setNavigationOnClickListener(v -> onBackPressed());
    }
    private final Callback<List<CubeWithMetaData>> cubesMetaCallback = new Callback<List<CubeWithMetaData>>() {
        @Override
        public void success(List<CubeWithMetaData> result) {
            if (isRunning) {
                progressDialog.dismiss();
            }
        }

        @Override
        public void failure(Exception e) {
            progressDialog.dismiss();
            showErrorMsg();
        }
    };

    private void showErrorMsg() {
        if (isRunning) {
            String tstMsg = "Server problem please try later!";
            Toast toast = Toast.makeText(this, tstMsg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }


}
