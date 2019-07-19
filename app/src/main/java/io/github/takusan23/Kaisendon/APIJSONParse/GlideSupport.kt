package io.github.takusan23.Kaisendon.APIJSONParse

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

class GlideSupport() {

    /*
    * Glideつかって角を丸くしたり、キャッシュ限定で読み込んだりするくらす？
    * */

    private val corner = 10

    //オフライン（キャッシュ）で読み込む？
    fun loadOfflineGlide(url: String, imageView: ImageView) {
        Glide.with(imageView.getContext())
                .load(url)
                .onlyRetrieveFromCache(true)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        //読み込めなかったらレイアウト消す
                        if (imageView.getParent() is  LinearLayout){
                            if (imageView.getParent() as LinearLayout != null) {
                                (imageView.getParent() as LinearLayout).removeView(imageView)
                            }
                        }
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
                .into(imageView)
    }

    //オフライン（キャッシュ）で読み込み、角を丸くする。
    fun loadOfflineRoundCornerGlide(url: String, imageView: ImageView) {
        Glide.with(imageView.getContext())
                .load(url)
                .onlyRetrieveFromCache(true)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(corner)))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        //読み込めなかったらレイアウト消す
                        if (imageView.getParent() is  LinearLayout){
                            if (imageView.getParent() as LinearLayout != null) {
                                (imageView.getParent() as LinearLayout).removeView(imageView)
                            }
                        }
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
                .into(imageView)
    }

    //Glideの角を丸くする
    fun loadRoundCornerGlide(url: String, imageView: ImageView) {
        Glide.with(imageView.getContext())
                .load(url)
                //.transform(new CenterCrop(),new RoundedCorners(10))
                .apply(RequestOptions.bitmapTransform(RoundedCorners(corner)))
                .into(imageView)
    }

    //角を丸くしない
    fun loadNormalGlide(url: String, imageView: ImageView) {
        Glide.with(imageView.getContext())
                .load(url)
                .into(imageView)
    }

    //角を丸くする設定かどうか
    fun isRoundCorner(context: Context): Boolean {
        val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        val value = pref_setting.getBoolean("pref_avatar_round_corner", true)
        return value
    }

    //キャッシュ限定で読み込む設定かどうか
    fun isCacheOnly(context: Context): Boolean {
        val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        val value = pref_setting.getBoolean("pref_offline_cache_load", true)
        return value
    }

    //設定から丸くするかどうか決める。
    //角を丸くする設定込みなので短くかけるよ！
    fun loadGlide(url: String, imageView: ImageView) {
        //角を丸くするか
        if (isRoundCorner(imageView.context)) {
            loadRoundCornerGlide(url, imageView)
        } else {
            loadNormalGlide(url,imageView)
        }
    }

    //loadGlideのキャッシュ版。
    //角を丸くする設定込みなので短くかけるよ！
    fun loadGlideReadFromCache(url: String, imageView: ImageView){
        //角を丸くするか
        if (isRoundCorner(imageView.context)) {
            loadOfflineRoundCornerGlide(url, imageView)
        } else {
            loadOfflineGlide(url,imageView)
        }
    }

}