package io.github.takusan23.Kaisendon.CustomTabURL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import androidx.browser.customtabs.CustomTabsIntent;
import android.text.style.URLSpan;
import android.view.View;

import io.github.takusan23.Kaisendon.R;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

public class CustomTabURLSpan extends URLSpan {

    String test = null;

    public CustomTabURLSpan(String url) {
        super(url);
    }

    public CustomTabURLSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        //CustomTab起動！！！
        //contextはwidgetにあるよ
        //戻るアイコン
        Bitmap back_icon = BitmapFactory.decodeResource(widget.getContext().getResources(), R.drawable.ic_action_arrow_back);
        String custom = CustomTabsHelper.getPackageNameToUse(widget.getContext());
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage(custom);
        customTabsIntent.launchUrl(widget.getContext(), Uri.parse(url));
    }
}