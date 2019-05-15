package io.github.takusan23.Kaisendon;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import okhttp3.OkHttpClient;

public class BroadcastReceiver_Notification_Timeline extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String broadcast_type = intent.getExtras().getString("Type","");
        //渡してくれたIDを取得する
        long toot_id = intent.getExtras().getLong("ID");

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        //Toast.makeText(context,"タイプ : " + broadcast_type, Toast.LENGTH_SHORT).show();
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        //設定を取得
        //アクセストークンを変更してる場合のコード
        //アクセストークン
        String AccessToken = null;

        //インスタンス
        String Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        MastodonClient client = new MastodonClient.Builder(Instance, new OkHttpClient.Builder(), new Gson()).accessToken(AccessToken).build();

        //お気に入り
        if (broadcast_type.equals("Favourite") && remoteInput == null){
            Toast.makeText(context,"お気に入り : " + String.valueOf(toot_id), Toast.LENGTH_SHORT).show();
            //お気に入り
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {

                    try {
                        com.sys1yagi.mastodon4j.api.entity.Status statuses = new Statuses(client).postFavourite(toot_id).execute();
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            Toast.makeText(context,"お気に入り : " + String.valueOf(toot_id), Toast.LENGTH_SHORT).show();
        }

        //トゥート
        else if (remoteInput != null){
            CharSequence charSequence = "";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (remoteInput != null) {
                charSequence = remoteInput.getCharSequence("Toot_Text");
                //System.out.println("Reply : " + (String) charSequence);

                CharSequence finalCharSequence = charSequence;
                new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... string) {
                        try {
                            com.sys1yagi.mastodon4j.api.entity.Status statuses = new Statuses(client).postStatus((String) finalCharSequence, null, null,false,null).execute();
                        } catch (Mastodon4jRequestException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                Toast.makeText(context,"トゥートしました : " + charSequence, Toast.LENGTH_SHORT).show();
                notificationManager.cancel(R.string.notification_LocalTimeline_Notification);
            }

        }

        else if (broadcast_type.equals("Timeline_Show")){
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putInt("timeline_toast_check", 0);
            editor.commit();
        }

        else if (broadcast_type.equals("TTS")){
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("pref_speech", false);
            editor.apply();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(R.string.speech_timeline);
        }


    }
}
