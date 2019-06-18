package io.github.takusan23.Kaisendon.HTMLMarkdown

import android.content.Context
import android.widget.TextView
import ru.noties.markwon.Markwon
import ru.noties.markwon.html.HtmlPlugin
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
                .build()
        markwon.setMarkdown(textView, html)
    }
}
