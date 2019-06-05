package io.github.takusan23.Kaisendon

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

class MarqueeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {

    private var mIsMarqueeStarted: Boolean = false

    init {

        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        isFocusable = true
        isFocusableInTouchMode = true
        isSingleLine = true
    }

    override fun isSelected(): Boolean {
        // Always return true. because marquee animation stop when return false.
        return true
    }

    fun startMarquee() {
        if (mIsMarqueeStarted) {
            return
        }
        mIsMarqueeStarted = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        invalidate()
    }

    fun stopMarquee() {
        if (!mIsMarqueeStarted) {
            return
        }
        mIsMarqueeStarted = false
        ellipsize = TextUtils.TruncateAt.END
        invalidate()
    }
}