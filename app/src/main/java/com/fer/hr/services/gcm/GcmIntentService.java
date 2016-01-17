package com.fer.hr.services.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.fer.hr.R;
import com.fer.hr.data.Profile;

/**
 * Created by igor on 04/01/16.
 */
public class GcmIntentService extends IntentService {
    private Profile appProfile;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        appProfile = new Profile(this);
        Bundle extras = intent.getExtras();
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.

        //Intent resultIntent = new Intent(this, SplashActivity.class);
        //resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        1,
                        new Intent(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon_salam_notification)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.salam_icon))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(extras.getString("message"))
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent);
        Notification notification = mBuilder.build();

        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, notification);
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        this.sendBroadcast(new Intent("push_received"));
        stopSelf();
    }
}
