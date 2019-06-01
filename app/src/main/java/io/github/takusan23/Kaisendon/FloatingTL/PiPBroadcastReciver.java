package io.github.takusan23.Kaisendon.FloatingTL;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.github.takusan23.Kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PiPBroadcastReciver extends BroadcastReceiver {
    private SharedPreferences pref_setting;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notification_localtimeline_toot = new Intent(context, PiPBroadcastReciver.class);
        notification_localtimeline_toot.putExtra("toot_text", true);
        PendingIntent notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(context, 1, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        //投稿するとき
        if (intent.getBooleanExtra("toot_text", false)) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                CharSequence charSequence = "";
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (remoteInput != null) {
                    charSequence = remoteInput.getCharSequence("Toot_Text");
                    CharSequence finalCharSequence = charSequence;
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    String AccessToken = pref_setting.getString("main_token", "");
                    String Instance = pref_setting.getString("main_instance", "");
                    String url = "https://" + Instance + "/api/v1/statuses/?access_token=" + AccessToken;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("status", finalCharSequence);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RequestBody requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody_json)
                            .build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            //失敗
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!response.isSuccessful()) {
                                        Toast.makeText(context, context.getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.toot_ok) + " : " + finalCharSequence, Toast.LENGTH_SHORT).show();
                                        notificationManager.cancel(R.string.add_widget);
                                    }
                                }
                            });

                        }
                    });
                }
            }
        } else {
            //トゥート通知
            //通知チャンネル実装
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String channel = "Widget_Notification";
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel = new NotificationChannel(channel, "LocalTimeline Toot", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(context.getString(R.string.widget_notification_channel));
                notificationChannel.setName(context.getString(R.string.widget_notification_channel));

                notificationManager.createNotificationChannel(notificationChannel);

                //トゥート
                androidx.core.app.RemoteInput remoteInput = new androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(context.getString(R.string.imananisiteru))
                        .build();

                NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                        , context.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

                long[] pattern = {100};

                Notification newMessageNotification =
                        new NotificationCompat.Builder(context, channel)
                                .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                                .setContentTitle(context.getString(R.string.toot))
                                .setContentText(context.getString(R.string.imananisiteru))
                                .setPriority(1)
                                .setVibrate(pattern)
                                .addAction(notification_toot_action).build();

                NotificationManagerCompat notificationManager_1 = NotificationManagerCompat.from(context);
                notificationManager_1.notify(R.string.add_widget, newMessageNotification);

            } else {
                //トゥート
                androidx.core.app.RemoteInput remoteInput = new androidx.core.app.RemoteInput.Builder("Toot_Text")
                        .setLabel(context.getString(R.string.imananisiteru))
                        .build();

                NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                        , context.getString(R.string.imananisiteru), notification_localtimeline_pendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

                long[] pattern = {100};

                Notification newMessageNotification =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_create_black_24dp_black)
                                .setContentTitle(context.getString(R.string.toot))
                                .setContentText(context.getString(R.string.imananisiteru))
                                .setPriority(1)
                                .setVibrate(pattern)
                                .addAction(notification_toot_action).build();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(R.string.add_widget, newMessageNotification);
            }
        }
    }
}
