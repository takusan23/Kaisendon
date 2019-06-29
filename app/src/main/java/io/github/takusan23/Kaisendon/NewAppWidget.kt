package io.github.takusan23.Kaisendon

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import okhttp3.*
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
    internal lateinit var pref_setting: SharedPreferences

    override fun onUpdate(ctx: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(ctx, manager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            val remoteViewsFactoryIntent = Intent(ctx, WidgetService::class.java)
            val rv = RemoteViews(ctx.packageName, R.layout.new_app_widget)
            rv.setRemoteAdapter(R.id.widget_listview, remoteViewsFactoryIntent)

            setOnButtonClickPendingIntent_Load(ctx, rv, appWidgetId)
            setOnButtonClickPendingIntent_Toot(ctx, rv, appWidgetId)
            setOnButtonClickPendingIntent_Lunch(ctx, rv, appWidgetId)

            val URLJumpIntent = Intent(ctx, NewAppWidget::class.java)
            val URLJumpPendingIntent = PendingIntent.getBroadcast(ctx, 30, URLJumpIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.widget_listview, URLJumpPendingIntent)

            manager.updateAppWidget(appWidgetId, rv)
        }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId != 0) {
            AppWidgetManager.getInstance(ctx).notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listview)
        }

        if (intent.getBooleanExtra("TootMode", false)) {
            val channel = "notification_toot"

            //通知チャンネル実装
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager != null && notificationManager.getNotificationChannel(channel) == null) {
                    val notificationChannel = NotificationChannel(channel, "Notification Toot", NotificationManager.IMPORTANCE_HIGH)
                    notificationChannel.description = ctx.getString(R.string.toot_notification_description)
                    notificationChannel.name = ctx.getString(R.string.toot_notification_name)
                    notificationManager.createNotificationChannel(notificationChannel)
                }

                val notification_localtimeline_toot = Intent(ctx, NewAppWidget::class.java)
                val notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(ctx, 1, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT)
                //トゥート
                val remoteInput = androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(ctx.getString(R.string.imananisiteru))
                        .build()

                val notification_toot_action = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send, ctx.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()

                val pattern = longArrayOf(100)

                val newMessageNotification = NotificationCompat.Builder(ctx, channel)
                        .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                        .setContentTitle(ctx.getString(R.string.toot))
                        .setContentText(ctx.getString(R.string.imananisiteru))
                        .setPriority(1)
                        .setVibrate(pattern)
                        .addAction(notification_toot_action).build()

                val notificationManager_1 = NotificationManagerCompat.from(ctx)
                notificationManager_1.notify(R.string.add_widget, newMessageNotification)

            } else {
                val notification_localtimeline_toot = Intent(ctx, NewAppWidget::class.java)
                val notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(ctx, 1, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT)
                //トゥート
                val remoteInput = androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(ctx.getString(R.string.imananisiteru))
                        .build()

                val notification_toot_action = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send, ctx.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()

                val pattern = longArrayOf(100)

                val newMessageNotification = NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                        .setContentTitle(ctx.getString(R.string.toot))
                        .setContentText(ctx.getString(R.string.imananisiteru))
                        .setPriority(1)
                        .setVibrate(pattern)
                        .addAction(notification_toot_action).build()

                val notificationManager = NotificationManagerCompat.from(ctx)
                notificationManager.notify(R.string.add_widget, newMessageNotification)
            }
        }
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            var charSequence: CharSequence? = ""
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (remoteInput != null) {
                charSequence = remoteInput.getCharSequence("Toot_Text")
                val finalCharSequence = charSequence
                val uiHandler = Handler(Looper.getMainLooper())
                val AccessToken = pref_setting.getString("main_token", "")
                val Instance = pref_setting.getString("main_instance", "")
                val url = "https://$Instance/api/v1/statuses/?access_token=$AccessToken"
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("status", finalCharSequence)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                val requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
                val request = Request.Builder()
                        .url(url)
                        .post(requestBody_json)
                        .build()
                val okHttpClient = OkHttpClient()
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        //失敗
                        uiHandler.post { Toast.makeText(ctx, ctx.getString(R.string.error), Toast.LENGTH_SHORT).show() }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        uiHandler.post {
                            if (!response.isSuccessful) {
                                Toast.makeText(ctx, ctx.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, ctx.getString(R.string.toot_ok) + " : " + finalCharSequence, Toast.LENGTH_SHORT).show()
                                notificationManager.cancel(R.string.add_widget)
                            }
                        }

                    }
                })
            }
        }

        //アプリ起動
        if (intent.getBooleanExtra("Lunch", false)) {
            val lunch_Intent = Intent(ctx, Home::class.java)
            lunch_Intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ctx.startActivity(lunch_Intent)
        }

        if (intent.getBooleanExtra("ListViewClick", false)) {
            val mediaURL = intent.getStringExtra("URL")
            pref_setting = getDefaultSharedPreferences(ctx)
            val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)
            //カスタムタグ有効
            if (chrome_custom_tabs) {
                val back_icon = BitmapFactory.decodeResource(ctx.applicationContext.resources, R.drawable.ic_action_arrow_back)
                val custom = CustomTabsHelper.getPackageNameToUse(ctx)
                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.intent.setPackage(custom)
                customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                customTabsIntent.launchUrl(ctx, Uri.parse(mediaURL))
                //無効
            } else {
                val uri = Uri.parse(mediaURL)
                val intent_url = Intent(Intent.ACTION_VIEW, uri)
                ctx.startActivity(intent_url)
            }

        }

        /*

        if (intent.getBooleanExtra("Open", false)) {
            Intent openApp = new Intent(ctx, Home.class);
            if (pref_setting.getString("WidgetTLType", "Home").contains("Home")) {
                openApp.putExtra("Home", true);
                ctx.startActivity(openApp);
            }
            if (pref_setting.getString("WidgetTLType", "Home").contains("Notification")) {
                openApp.putExtra("Notification", true);
                ctx.startActivity(openApp);
            }
            if (pref_setting.getString("WidgetTLType", "Home").contains("Local")) {
                openApp.putExtra("Local", true);
                ctx.startActivity(openApp);
            }
            if (pref_setting.getString("WidgetTLType", "Home").contains("Federated")) {
                openApp.putExtra("Federated", true);
                ctx.startActivity(openApp);
            }
            openApp.putExtra("Home", true);
            ctx.startActivity(openApp);

*/


    }

    private fun setOnButtonClickPendingIntent_Load(ctx: Context, rv: RemoteViews, appWidgetId: Int) {
        val btnClickIntent = Intent(ctx, NewAppWidget::class.java)
        btnClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        val btnClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                23,
                btnClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        rv.setOnClickPendingIntent(R.id.widget_button_load, btnClickPendingIntent)
    }

    private fun setOnButtonClickPendingIntent_Toot(ctx: Context, rv: RemoteViews, appWidgetId: Int) {
        val btnClickIntent = Intent(ctx, NewAppWidget::class.java)
        btnClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        btnClickIntent.putExtra("TootMode", true)

        val btnClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                24,
                btnClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        rv.setOnClickPendingIntent(R.id.widget_button_toot, btnClickPendingIntent)
    }

    private fun setOnButtonClickPendingIntent_Lunch(ctx: Context, rv: RemoteViews, appWidgetId: Int) {
        val btnClickIntent = Intent(ctx, NewAppWidget::class.java)
        btnClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        btnClickIntent.putExtra("Lunch", true)

        val btnClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                25,
                btnClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        rv.setOnClickPendingIntent(R.id.widget_button_lunch, btnClickPendingIntent)
    }

    companion object {

        private val ACTION_CLICK = "kaisendon.widget.Public"
    }

}