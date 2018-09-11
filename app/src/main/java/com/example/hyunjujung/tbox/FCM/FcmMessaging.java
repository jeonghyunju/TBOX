package com.example.hyunjujung.tbox.FCM;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.login_join_activity.ToysLogin;
import com.google.firebase.messaging.RemoteMessage;

/**
 *  [ FCM 에서 메세지를 받으면 기기에 알림을 띄운다 ]
 *
 */

public class FcmMessaging extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    Intent intent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendNotification(remoteMessage.getData().get("message"));
    }

    private void sendNotification(String messageBody) {

        /* 알림을 누르면 로그인 화면으로 이동한다 */
        intent = new Intent(this, ToysLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_ringing)
                .setContentTitle("알림")
                .setAutoCancel(true)
                .setSound(defaultUri)
                .setContentText(messageBody)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
