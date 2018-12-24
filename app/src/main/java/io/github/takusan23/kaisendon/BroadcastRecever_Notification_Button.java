package io.github.takusan23.kaisendon;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class BroadcastRecever_Notification_Button extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //受信した時
        //今回は設定を変更する
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        if (intent.getBooleanExtra("TTS",false)){
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("pref_speech", false);
            editor.apply();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(R.string.speech_timeline);
        }else{
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putInt("timeline_toast_check", 0);
            editor.apply();

            //通知を消す
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //IDを指定（notifyのときのID）
            notificationManager.cancel(R.string.app_name);

        }

/*
        pref_setting.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //見つける
                if (key.equals("timeline_toast_check")){
                    System.out.println("よんだ？");
                }
            }
        });
*/




/*
        Toast.makeText(context, "寝たい、学校めんｄ", Toast.LENGTH_SHORT).show();
        System.out.println("ブロードキャスト！！！！！！！！！！！！！");
*/
    }
}
