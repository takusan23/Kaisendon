package io.github.takusan23.kaisendon.FloatingTL;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.R;

public class FloatingTL {
    private Context context;
    private NotificationManager notificationManager;
    private Notification builder;
   private String jsonObject;

    public FloatingTL(Context context,String jsonObject) {
        this.jsonObject = jsonObject;
        this.context = context;
    }

    /*通知作成*/
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setNotification() {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, FloatingTLActivity.class);
        intent.putExtra("json",jsonObject);
        PendingIntent bubbleIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //通知作成
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.CODENAME.contains("Q")) {
            String notificationID = "floating_tl";
            notificationChannel = new NotificationChannel(notificationID, "Floating TL", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Overlay Timeline");
            notificationChannel.setName("Floating TL");
            notificationManager.createNotificationChannel(notificationChannel);
            //たかさ
            Notification.BubbleMetadata bubbleMetadata = new Notification.BubbleMetadata.Builder()
                    .setDesiredHeight(600)
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_flip_to_front_black_24dp))
                    .setIntent(bubbleIntent)
                    .build();
            //通知作成
            Person person = new Person.Builder()
                    .setBot(true)
                    .setName("Floating TL")
                    .setImportant(true)
                    .build();

            builder = new Notification.Builder(context, notificationID)
                    .setContentIntent(bubbleIntent)
                    .setSmallIcon(Icon.createWithResource(context, R.drawable.ic_flip_to_front_black_24dp))
                    .setBubbleMetadata(bubbleMetadata)
                    .setContentTitle("Floating TL")
                    .setContentText("Overlay Timeline")
                    .addPerson(person)
                    .build();

            notificationManager.notify(23, builder);
        }
    }


}
