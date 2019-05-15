package io.github.takusan23.Kaisendon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BroadcastReciver_Widget_ToastTimeline extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        if (pref_setting.getInt("timeline_toast_check", 0) == 1) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putInt("timeline_toast_check", 0);
            editor.commit();

            Toast.makeText(context,R.string.timeline_toast_disable, Toast.LENGTH_SHORT).show();

        }else {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putInt("timeline_toast_check", 1);
            editor.commit();

            Toast.makeText(context,R.string.timeline_notification_public, Toast.LENGTH_SHORT).show();

        }
    }
}
