package io.github.takusan23.Kaisendon.CustomTabURL

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.text.style.URLSpan
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import io.github.takusan23.Kaisendon.R
import org.chromium.customtabsclient.shared.CustomTabsHelper

class CustomTabURLSpan : URLSpan {

    internal var test: String? = null

    constructor(url: String) : super(url) {}

    constructor(src: Parcel) : super(src) {}

    override fun onClick(widget: View) {
        val url = url
        //CustomTab起動！！！
        //contextはwidgetにあるよ
        //戻るアイコン
        val back_icon = BitmapFactory.decodeResource(widget.context.resources, R.drawable.ic_action_arrow_back)
        val custom = CustomTabsHelper.getPackageNameToUse(widget.context)
        val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
        val customTabsIntent = builder.build()
        customTabsIntent.intent.setPackage(custom)
        customTabsIntent.launchUrl(widget.context, Uri.parse(url))
    }
}