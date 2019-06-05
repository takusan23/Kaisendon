package io.github.takusan23.Kaisendon.FloatingTL

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class PiPBroadcastReciver : BroadcastReceiver() {
    private var pref_setting: SharedPreferences? = null
    private val notificationManager: NotificationManager? = null

    override fun onReceive(context: Context, intent: Intent) {
        val notification_localtimeline_toot = Intent(context, PiPBroadcastReciver::class.java)
        notification_localtimeline_toot.putExtra("toot_text", true)
        val notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(context, 1, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT)
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        //投稿するとき
        if (intent.getBooleanExtra("toot_text", false)) {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput != null) {
                var charSequence: CharSequence? = ""
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (remoteInput != null) {
                    charSequence = remoteInput.getCharSequence("Toot_Text")
                    postMastodon(context, charSequence!!.toString())
                }
            }
        } else {
            //トゥート通知
            //通知チャンネル実装
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = "notification_toot"
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager != null && notificationManager.getNotificationChannel(channel) == null) {
                    val notificationChannel = NotificationChannel(channel, "Notification Toot", NotificationManager.IMPORTANCE_HIGH)
                    notificationChannel.description = context.getString(R.string.toot_notification_description)
                    notificationChannel.name = context.getString(R.string.toot_notification_name)
                    notificationManager.createNotificationChannel(notificationChannel)
                }

                //トゥート
                val remoteInput = androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(context.getString(R.string.imananisiteru))
                        .build()

                val notification_toot_action = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send, context.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()

                val pattern = longArrayOf(100)

                val newMessageNotification = NotificationCompat.Builder(context, channel)
                        .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                        .setContentTitle(context.getString(R.string.toot_text))
                        .setContentText(context.getString(R.string.imananisiteru))
                        .setPriority(1)
                        .setVibrate(pattern)
                        .addAction(notification_toot_action).build()

                val notificationManager_1 = NotificationManagerCompat.from(context)
                notificationManager_1.notify(R.string.add_widget, newMessageNotification)

            } else {
                //トゥート
                val remoteInput = androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(context.getString(R.string.imananisiteru))
                        .build()

                val notification_toot_action = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send, context.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()

                val pattern = longArrayOf(100)

                val newMessageNotification = NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                        .setContentTitle(context.getString(R.string.toot_text))
                        .setContentText(context.getString(R.string.imananisiteru))
                        .setPriority(1)
                        .setVibrate(pattern)
                        .addAction(notification_toot_action).build()

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(R.string.add_widget, newMessageNotification)
            }
        }
    }

    /*Mastodon投稿*/
    private fun postMastodon(context: Context, status: String) {
        val AccessToken = pref_setting!!.getString("main_token", "")
        val Instance = pref_setting!!.getString("main_instance", "")
        val url = "https://$Instance/api/v1/statuses/?access_token=$AccessToken"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("status", status)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        val request = Request.Builder()
                .url(url)
                .post(requestBody_json)
                .build()
        postAPI(request, context, status)
    }

    /*Misskeyに投稿*/
    private fun postMisskey(context: Context, status: String) {
        val instance = pref_setting!!.getString("misskey_main_instance", "")
        val token = pref_setting!!.getString("misskey_main_token", "")
        val url = "https://$instance/api/notes/create"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("text", status)
            jsonObject.put("viaMobile", true)//スマホからなので一応
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        //作成
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        postAPI(request, context, status)
    }

    /*共通部分*/
    private fun postAPI(request: Request, context: Context, status: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uiHandler = Handler(Looper.getMainLooper())
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗
                uiHandler.post { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                uiHandler.post {
                    if (!response.isSuccessful) {
                        Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.toot_ok) + " : " + status, Toast.LENGTH_SHORT).show()
                        notificationManager.cancel(R.string.add_widget)
                    }
                }
            }
        })
    }
}
