package com.fer.hr.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fer.hr.App;
import com.fer.hr.R;
import com.fer.hr.data.Profile;
import com.fer.hr.gcm.GCMService;
import com.fer.hr.rest.api.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

    private boolean isLogin = true;
    private Profile appProfile;
    private static boolean isRunning = false;
    //TODO deploy: remove from production
    private static final String TEST_EMAIL = "igor@gmail.com";
    private static final String TEST_PASSWORD = "lozinka";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        appProfile = new Profile(this);

        initActivity();
        setActions();
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

    private void initActivity() {
        title.setText("Login/Register");
        actionBtn.setVisibility(View.GONE);
        userEmail.setText(TEST_EMAIL);
        userPassword.setText(TEST_PASSWORD);
    }

    private void setActions() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        registerChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isLogin = !isLogin;
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearErrors();
                if (checkForErrors()) return;

                progressBar.setVisibility(View.VISIBLE);
                String credentials = userEmail.getText().toString().trim() + ":" + userPassword.getText().toString().trim();
                String encodedCredentials = "Basic " + AuthenticationUtil.encodeBase64(credentials);

                if (isLogin) login(encodedCredentials);
                else register(encodedCredentials);
            }
        });
    }

    private boolean checkForErrors() {
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

    private void clearErrors() {
        userEmail.setError(null);
        userPassword.setError(null);
    }

    private void clearForm() {
        userEmail.setText("");
        userPassword.setText("");
    }

    private void login(String encodedCredentials) {
        App.api.login(encodedCredentials, new Callback<String>() {
            @Override
            public void success(String authenticationToken, Response response) {
                if(isRunning) {
                    clearForm();
                    progressBar.setVisibility(View.GONE);
                    appProfile.setAuthenticationToken(authenticationToken);
                    startActivity(new Intent(LoginActivity.this, MdxActivity.class));
                    finish();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                if(isRunning) {
                    userPassword.setText("");
                    progressBar.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(LoginActivity.this, "Invalid email or password!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void register(final String encodedCredentials) {
        String gcmId = appProfile.getGcmId();
        if(gcmId == null) {
            GCMService.instance(this, Constants.PROJECT_ID).registerWithGCMServer(new GCMService.Callback() {
                @Override
                public void success(String gcmRegToken) {
                    if(isRunning) executeRegisterApiCall(encodedCredentials, gcmRegToken);
                }

                @Override
                public void failure(String errorMsg) {
                    if(isRunning) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Server connection problem, please try later!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else executeRegisterApiCall(encodedCredentials, gcmId);
    }

    private void executeRegisterApiCall(String encodedCredentials, String gcmId) {
        App.api.registerAccount(encodedCredentials, gcmId, new Callback<String>() {
            @Override
            public void success(String token, Response response) {
                if (isRunning) {
                    clearForm();
                    progressBar.setVisibility(View.GONE);
                    appProfile.setAuthenticationToken(token);
                    startActivity(new Intent(LoginActivity.this, MdxActivity.class));
                    finish();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                if (isRunning) {
                    progressBar.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(LoginActivity.this, "User with provided email allready exists!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

}