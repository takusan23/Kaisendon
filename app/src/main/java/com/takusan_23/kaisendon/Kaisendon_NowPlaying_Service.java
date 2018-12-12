package com.takusan_23.kaisendon;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Kaisendon_NowPlaying_Service extends NotificationListenerService {
    private String TAG = "Notification";

    SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

    String app_name = "";

    BroadcastReceiver broadcastReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Stop_Now_Playing");
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        StatusBarNotification[] notifications = getActiveNotifications();
        for (StatusBarNotification object : notifications) {

            ArrayList<String> notificationList = new ArrayList<String>();

            Notification not = object.getNotification();
            Bundle bundle = not.extras;

            //パッケージ名からアプリ名を取得
            String app = object.getPackageName();
            PackageManager packageManager = getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo;
            try {
                applicationInfo = packageManager.getApplicationInfo(app, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                applicationInfo = null;
            }
            String applicationName = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");

            System.out.println("あいうえお : " + bundle.getString(Notification.EXTRA_TITLE));

            if (applicationName.contains(pref_setting.getString("Now_Playing_AppName", "NicoBox"))) {

                //タイトル
                String text = bundle.getString(Notification.EXTRA_TITLE);

                //ブロードキャストを利用してTootActivityへ送信する
                Intent intent = new Intent();
                intent.setAction("Now_Playing");
                intent.putExtra("title", text);
                getBaseContext().sendBroadcast(intent);

            }
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

}