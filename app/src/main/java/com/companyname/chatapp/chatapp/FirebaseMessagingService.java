package com.companyname.chatapp.chatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.companyname.chatapp.chatapp.Activities.ProfileActivity;
import com.companyname.chatapp.chatapp.Model.Globals;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

/**
 * Created by Mohamed Ahmed on 6/23/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    NotificationManager notificationManager;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        String notification_title ;//= remoteMessage.getNotification().getTitle();
        String notification_message;// = remoteMessage.getNotification().getBody();
        int notificationId = new Random().nextInt(60000);
        String click_action;// = remoteMessage.getNotification().getClickAction();
        String from_user_id;// = remoteMessage.getData().get("from_user_id");
        from_user_id = remoteMessage.getData().get("from_user_id");
        click_action = remoteMessage.getData().get("click_action");
        notification_title = remoteMessage.getData().get("title");
        notification_message = remoteMessage.getData().get("body");
        Intent resultIntent = new Intent(this,ProfileActivity.class);
        resultIntent.putExtra("user_id",from_user_id);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Globals.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(notificationId /* ID of notification */, mBuilder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Globals.NOTIFICATION_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
