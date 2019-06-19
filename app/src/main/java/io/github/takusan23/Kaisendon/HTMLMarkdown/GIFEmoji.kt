package io.github.takusan23.Kaisendon.HTMLMarkdown

import android.content.Context
import android.widget.TextView
import androidx.annotation.NonNull
import io.github.takusan23.Kaisendon.R
import ru.noties.markwon.AbstractMarkwonPlugin
import ru.noties.markwon.Markwon
import ru.noties.markwon.html.HtmlPlugin
import ru.noties.markwon.image.AsyncDrawableLoader
import ru.noties.markwon.image.ImagesPlugin
import ru.noties.markwon.image.gif.GifPlugin

class GIFEmoji() {
    fun setGIFEmoji(context: Context, html: String, textView: TextView) {
        var tree = textView.viewTreeObserver
        var height = 66;
        tree.addOnGlobalLayoutListener { ->
            height = textView.height
        }
        //var mark = "GIF <img src=\"https://media.best-friends.chat/accounts/avatars/000/020/498/original/bc0bd14abb0bb063.gif\" width=\"${height}\" >"

        //Markdownのライブラリ入れた
        val markwon = Markwon.builder(context)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(ImagesPlugin.create(context))
                .usePlugin(GifPlugin.create())
                .usePlugin(object : AbstractMarkwonPlugin() {   //読み込み中は別のDrawableを表示する
                    override fun configureImages(builder: AsyncDrawableLoader.Builder) {
                        builder.placeholderDrawableProvider {
                            // your custom placeholder drawable
                            context.getDrawable(R.drawable.ic_refresh_black_24dp)
                        }
                    }
                }).build()
        markwon.setMarkdown(textView, html)
    }

    fun getTextViewHeight(textView: TextView): Int {
        var height = 0
        var treeObserver = textView.viewTreeObserver
        treeObserver.addOnGlobalLayoutListener { ->
            height = textView.height
        }
        return height
    }


}
