package com.fer.hr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fer.hr.R;
import com.fer.hr.data.Profile;
import com.fer.hr.model.CubeWithMetaData;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.authentication.IAuthenticate;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.repository.IRepository;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @Bind(R.id.btnBack)
    ImageButton btnBack;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.actionBtn)
    ImageButton actionBtn;
    @Bind(R.id.userEmail)
    EditText userEmail;
    @Bind(R.id.userPassword)
    EditText userPassword;
    @Bind(R.id.login)
    Button login;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.registerChk)
    CheckBox registerChk;
    @Bind(R.id.contentRoot)
    RelativeLayout contentRoot;

    private boolean isLogin = true;
    private Profile appProfile;
    private static boolean isRunning = false;
    //TODO deploy: remove from production
    private static final String TEST_EMAIL = "igor@gmail.com";
    private static final String TEST_PASSWORD = "lozinka";
    private IAuthenticate authenticationMng;
    private IRepository repositoryMng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        isRunning = true;
        appProfile = new Profile(this);
        authenticationMng = (IAuthenticate) ServiceProvider.getService(ServiceProvider.AUTHENTICATION);
        repositoryMng = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);

        if (authenticationMng.isLogedIn()) {
            contentRoot.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            repositoryMng.getFreshCubesMeta(cubesMetaCallback);
        } else {
            initView();
            setActions();
        }
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
        title.setText("Login/Register");
        actionBtn.setVisibility(View.GONE);
        userEmail.setText(TEST_EMAIL);
        userPassword.setText(TEST_PASSWORD);
    }

    private void setActions() {
        btnBack.setOnClickListener(v -> onBackPressed());

        registerChk.setOnCheckedChangeListener((buttonView, isChecked) -> isLogin = !isLogin);

        login.setOnClickListener(v -> {
            clearFormErrors();
            if (checkFormErrors()) return;
            progressBar.setVisibility(View.VISIBLE);

            String email = userEmail.getText().toString().trim();
            String password = userPassword.getText().toString().trim();
            if (isLogin) authenticationMng.login(email, password, loginRegisterCallback);
            else authenticationMng.register(email, password, loginRegisterCallback);
        });
    }

    private final Callback<String> loginRegisterCallback = new Callback<String>() {
        @Override
        public void success(String saikuToken) {
            if (isRunning) {
                repositoryMng.getFreshCubesMeta(cubesMetaCallback);
            }
        }

        @Override
        public void failure(Exception e) {
            e.printStackTrace();
            showErrorMsg(false);
        }
    };

    private final Callback<List<CubeWithMetaData>> cubesMetaCallback = new Callback<List<CubeWithMetaData>>() {
        @Override
        public void success(List<CubeWithMetaData> result) {
            if (isRunning) {
                progressBar.setVisibility(View.GONE);
                startDashboardActivity();
            }
        }

        @Override
        public void failure(Exception e) {
            showErrorMsg(true);
        }
    };

    private void showErrorMsg(boolean isMeta) {
        if (isRunning) {
            String tstMsg;
            if (isLogin) {
                userPassword.setText("");
                tstMsg = "Invalid email or password!";
            } else if (isMeta) tstMsg = "Server problem please try later!";
            else {
                tstMsg = "User with provided email allready exists!";
                clearFormData();
            }

            progressBar.setVisibility(View.GONE);
            Toast toast = Toast.makeText(LoginActivity.this, tstMsg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private boolean checkFormErrors() {
        boolean hasErrors = false;
        if (TextUtils.isEmpty(userEmail.getText())) {
            hasErrors = true;
            userEmail.setError("Can't be empty!");
        }
        if (TextUtils.isEmpty(userPassword.getText())) {
            hasErrors = true;
            userPassword.setError("Can't be empty!");
        }
        return hasErrors;
    }

    private void clearFormErrors() {
        userEmail.setError(null);
        userPassword.setError(null);
    }

    private void clearFormData() {
        userEmail.setText("");
        userPassword.setText("");
    }

    private void startDashboardActivity() {
        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
        finish();
    }

}