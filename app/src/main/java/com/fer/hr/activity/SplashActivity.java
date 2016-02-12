package com.fer.hr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.fer.hr.R;
import com.fer.hr.model.CubeWithMetaData;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.authentication.IAuthenticate;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.repository.IRepository;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {
    @Bind(R.id.reportImg)
    ImageView reportImg;
    @Bind(R.id.textImg)
    ImageView textImg;

    private IAuthenticate authenticationMng;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        authenticationMng = (IAuthenticate) ServiceProvider.getService(ServiceProvider.AUTHENTICATION);

        Animation comeFromBot = AnimationUtils.loadAnimation(this, R.anim.slide_out_splash);
        comeFromBot.setDuration(600);
        reportImg.setAnimation(comeFromBot);

        Animation comeFromBot2 = AnimationUtils.loadAnimation(this, R.anim.slide_out_splash);
        comeFromBot2.setDuration(900);
        comeFromBot2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                delay(1000);
                if (authenticationMng.isLogedIn())
                    startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                else
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }

            void delay(int ms){
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if(isRunning){
                        Toast.makeText(SplashActivity.this, "Internal Error!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });
        textImg.setAnimation(comeFromBot2);
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
}
