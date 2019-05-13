package io.github.takusan23.kaisendon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import okhttp3.OkHttpClient;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    private static final String ACTION_CLICK = "kaisendon.widget.Public";
    SharedPreferences pref_setting;

    @Override
    public void onUpdate(Context ctx, AppWidgetManager manager, int[] appWidgetIds) {
        super.onUpdate(ctx, manager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            Intent remoteViewsFactoryIntent = new Intent(ctx, WidgetService.class);
            RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.new_app_widget);
            rv.setRemoteAdapter(R.id.widget_listview, remoteViewsFactoryIntent);

            setOnButtonClickPendingIntent_Load(ctx, rv, appWidgetId);
            setOnButtonClickPendingIntent_Toot(ctx, rv, appWidgetId);
            setOnButtonClickPendingIntent_Lunch(ctx, rv, appWidgetId);

            Intent URLJumpIntent = new Intent(ctx, NewAppWidget.class);
            PendingIntent URLJumpPendingIntent = PendingIntent.getBroadcast(ctx, 30, URLJumpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_listview, URLJumpPendingIntent);

            manager.updateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);

        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId != 0) {
            AppWidgetManager.getInstance(ctx).notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listview);

        }

        if (intent.getBooleanExtra("TootMode", false)) {
            String channel = "Widget_Notification";

            //通知チャンネル実装
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel = new NotificationChannel(channel, "LocalTimeline Toot", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(ctx.getString(R.string.widget_notification_channel));
                notificationChannel.setName(ctx.getString(R.string.widget_notification_channel));

                notificationManager.createNotificationChannel(notificationChannel);

                Intent notification_localtimeline_toot = new Intent(ctx, NewAppWidget.class);
                PendingIntent notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(ctx, 1, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT);
                //トゥート
                androidx.core.app.RemoteInput remoteInput = new androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(ctx.getString(R.string.imananisiteru))
                        .build();

                NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                        , ctx.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

                long[] pattern = {100};

                Notification newMessageNotification =
                        new NotificationCompat.Builder(ctx, channel)
                                .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                                .setContentTitle(ctx.getString(R.string.toot))
                                .setContentText(ctx.getString(R.string.imananisiteru))
                                .setPriority(1)
                                .setVibrate(pattern)
                                .addAction(notification_toot_action).build();

                NotificationManagerCompat notificationManager_1 = NotificationManagerCompat.from(ctx);
                notificationManager_1.notify(R.string.add_widget, newMessageNotification);

            } else {
                Intent notification_localtimeline_toot = new Intent(ctx, NewAppWidget.class);
                PendingIntent notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(ctx, 1, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT);
                //トゥート
                androidx.core.app.RemoteInput remoteInput = new androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(ctx.getString(R.string.imananisiteru))
                        .build();

                NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                        , ctx.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

                long[] pattern = {100};

                Notification newMessageNotification =
                        new NotificationCompat.Builder(ctx)
                                .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                                .setContentTitle(ctx.getString(R.string.toot))
                                .setContentText(ctx.getString(R.string.imananisiteru))
                                .setPriority(1)
                                .setVibrate(pattern)
                                .addAction(notification_toot_action).build();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
                notificationManager.notify(R.string.add_widget, newMessageNotification);
            }
        }
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence charSequence = "";
            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            if (remoteInput != null) {
                charSequence = remoteInput.getCharSequence("Toot_Text");
                System.out.println("Reply : " + (String) charSequence);

                CharSequence finalCharSequence = charSequence;
                new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... string) {
                        try {
                            pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

                            String AccessToken, Instance;

                            boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
                            if (accessToken_boomelan) {
                                AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
                                Instance = pref_setting.getString("pref_mastodon_instance", "");
                            } else {
                                AccessToken = pref_setting.getString("main_token", "");
                                Instance = pref_setting.getString("main_instance", "");
                            }
                            MastodonClient client = new MastodonClient.Builder(Instance, new OkHttpClient.Builder(), new Gson()).accessToken(AccessToken).build();
                            com.sys1yagi.mastodon4j.api.entity.Status statuses = new Statuses(client).postStatus((String) finalCharSequence, null, null, false, null).execute();
                        } catch (Mastodon4jRequestException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                Toast.makeText(ctx, ctx.getString(R.string.toot_ok) + " : " + charSequence, Toast.LENGTH_SHORT).show();
                notificationManager.cancel(R.string.add_widget);
            }
        }

        //アプリ起動
        if (intent.getBooleanExtra("Lunch", false)) {
            Intent lunch_Intent = new Intent(ctx, Home.class);
            lunch_Intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(lunch_Intent);
        }

        if (intent.getBooleanExtra("ListViewClick", false)) {
            String mediaURL = intent.getStringExtra("URL");
            pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
            boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
            //カスタムタグ有効
            if (chrome_custom_tabs) {
                Bitmap back_icon = BitmapFactory.decodeResource(ctx.getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);
                String custom = CustomTabsHelper.getPackageNameToUse(ctx);
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setPackage(custom);
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                customTabsIntent.launchUrl(ctx, Uri.parse(mediaURL));
                //無効
            } else {
                Uri uri = Uri.parse(mediaURL);
                Intent intent_url = new Intent(Intent.ACTION_VIEW, uri);
                ctx.startActivity(intent_url);
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

    private void setOnButtonClickPendingIntent_Load(Context ctx, RemoteViews rv, int appWidgetId) {
        Intent btnClickIntent = new Intent(ctx, NewAppWidget.class);
        btnClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent btnClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                23,
                btnClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        rv.setOnClickPendingIntent(R.id.widget_button_load, btnClickPendingIntent);
    }

    private void setOnButtonClickPendingIntent_Toot(Context ctx, RemoteViews rv, int appWidgetId) {
        Intent btnClickIntent = new Intent(ctx, NewAppWidget.class);
        btnClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        btnClickIntent.putExtra("TootMode", true);

        PendingIntent btnClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                24,
                btnClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        rv.setOnClickPendingIntent(R.id.widget_button_toot, btnClickPendingIntent);
    }

    private void setOnButtonClickPendingIntent_Lunch(Context ctx, RemoteViews rv, int appWidgetId) {
        Intent btnClickIntent = new Intent(ctx, NewAppWidget.class);
        btnClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        btnClickIntent.putExtra("Lunch", true);

        PendingIntent btnClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                25,
                btnClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        rv.setOnClickPendingIntent(R.id.widget_button_lunch, btnClickPendingIntent);
    }

}