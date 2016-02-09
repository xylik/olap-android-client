package com.fer.hr.services.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.fer.hr.R;
import com.fer.hr.activity.LoginActivity;
import com.fer.hr.data.Profile;
import com.fer.hr.model.Report;
import com.google.gson.Gson;

/**
 * Created by igor on 04/01/16.
 */
public class GcmIntentService extends IntentService {
    private static final String SERVER_MESSAGE_KEY = "message";
    public static final String PUSH_RECEIVED = "PUSH_RECEIVED";
    private Profile appProfile;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        appProfile = new Profile(this);
        String msgJson = intent.getExtras().getString(SERVER_MESSAGE_KEY, "");
        Gson gson = new Gson();
        Report report = null;
        if(!TextUtils.isEmpty(msgJson)) {
            report = gson.fromJson(msgJson, Report.class);
            appProfile.addPushReport(report);
        }
        String reportName = (report==null ? "" : report.getReportName());

        Intent resultIntent = new Intent(this, LoginActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        1,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon_salam_notification)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.salam_icon))
                        .setContentTitle(getString(R.string.pushMsg))
                        .setContentText(reportName)
                        .setAutoCancel(true)

                        .setContentIntent(resultPendingIntent);
        Notification notification = mBuilder.build();

        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, notification);
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        this.sendBroadcast(new Intent(PUSH_RECEIVED));
        stopSelf();
    }
}
