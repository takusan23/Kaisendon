package com.takusan_23.kaisendon;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);

        //ブロードキャストを送信
        //ブロードキャスト先を指定（明示的ブロードキャスト）
        //マニフェストにも記入しないと動かないので注意



/*
        //ローカルトースト
        Intent toot_Broadcast = new Intent(context, BroadcastReciver_Widget_ToastTimeline.class);
        //アカウントペーじ
        Intent account_Intent = new Intent(context, AccountActivity.class);
        //トゥート
        Intent toot_Intent = new Intent(context, TootActivity.class);
        //ボタン
        views.setOnClickPendingIntent(R.id.widegt_1, PendingIntent.getActivity(context, 0, account_Intent, PendingIntent.FLAG_UPDATE_CURRENT));
        views.setOnClickPendingIntent(R.id.widegt_2, PendingIntent.getActivity(context, 0, toot_Intent, PendingIntent.FLAG_UPDATE_CURRENT));
        views.setOnClickPendingIntent(R.id.widegt_3, PendingIntent.getBroadcast(context, 0, toot_Broadcast, PendingIntent.FLAG_UPDATE_CURRENT));
*/

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

            Intent remoteViewsFactoryIntent = new Intent(context,WidgetService.class);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

