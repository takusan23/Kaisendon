package io.github.takusan23.Kaisendon.FloatingTL

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.github.takusan23.Kaisendon.R

class FloatingTL(private val context: Context, private val jsonObject: String) {
    private var notificationManager: NotificationManager? = null
    private var builder: Notification? = null

    /*通知作成*/
    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun setNotification(lunchPiP: Boolean) {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, FloatingTLActivity::class.java)
        intent.putExtra("json", jsonObject)
        intent.putExtra("pip", lunchPiP)
        val bubbleIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        //通知作成
        var notificationChannel: NotificationChannel? = null
        //PiPモードが無効になってる場合
        if (!lunchPiP) {
            val notificationID = "floating_tl"
            //通知ちゃんねるが作成されてなければ作成する
            if (notificationManager!!.getNotificationChannel(notificationID) == null) {
                notificationChannel = NotificationChannel(notificationID, context.getString(R.string.floating_tl), NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = context.getString(R.string.floating_tl_description)
                notificationChannel.name = context.getString(R.string.floating_tl)
                notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
                notificationManager!!.createNotificationChannel(notificationChannel)
            }
            //たかさ
            val bubbleMetadata = Notification.BubbleMetadata.Builder()
                    .setDesiredHeight(600)
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_flip_to_front_black_24dp))
                    .setIntent(bubbleIntent)
                    .build()
            //通知作成
            val person = Person.Builder()
                    .setBot(true)
                    .setName(context.getString(R.string.floating_tl))
                    .setImportant(true)
                    .build()

            builder = Notification.Builder(context, notificationID)
                    .setContentIntent(bubbleIntent)
                    .setSmallIcon(Icon.createWithResource(context, R.drawable.ic_flip_to_front_black_24dp))
                    .setBubbleMetadata(bubbleMetadata)
                    .setContentTitle(context.getString(R.string.floating_tl))
                    .setContentText(context.getString(R.string.floating_tl_description))
                    .addPerson(person)
                    .build()

            notificationManager!!.notify(23, builder)
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //Oreo以降はPictureInPictureで起動
            context.startActivity(intent)
        } else {
            //エラー文
            Toast.makeText(context, context.getString(R.string.floating_tl_error_os), Toast.LENGTH_SHORT).show()
        }
    }


}
