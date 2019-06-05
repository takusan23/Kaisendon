package io.github.takusan23.Kaisendon

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.*

class Kaisendon_NowPlaying_Service : NotificationListenerService() {
    private val TAG = "Notification"

    //internal var pref_setting = PreferenceManager.getDefaultSharedPreferences(conte)

    internal var app_name = ""

    internal lateinit var broadcastReceiver: BroadcastReceiver
    override fun onCreate() {
        super.onCreate()
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                stopSelf()
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction("Stop_Now_Playing")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }


    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notifications = activeNotifications
        for (`object` in notifications) {

            val notificationList = ArrayList<String>()

            val not = `object`.notification
            val bundle = not.extras

            //パッケージ名からアプリ名を取得
            val app = `object`.packageName
            val packageManager = applicationContext.packageManager
            var applicationInfo: ApplicationInfo?
            try {
                applicationInfo = packageManager.getApplicationInfo(app, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                applicationInfo = null
            }

            val applicationName = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "(unknown)") as String

            println("あいうえお : " + bundle.getString(Notification.EXTRA_TITLE)!!)

/*
            if (applicationName.contains(pref_setting.getString("Now_Playing_AppName", "NicoBox")!!)) {

                //タイトル
                val text = bundle.getString(Notification.EXTRA_TITLE)

                //ブロードキャストを利用してTootActivityへ送信する
                val intent = Intent()
                intent.action = "Now_Playing"
                intent.putExtra("title", text)
                baseContext.sendBroadcast(intent)

            }
*/
        }

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

}