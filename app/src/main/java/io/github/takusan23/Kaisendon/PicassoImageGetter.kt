package io.github.takusan23.Kaisendon

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.widget.TextView

import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class PicassoImageGetter(target: TextView) : Html.ImageGetter {

    private var textView: TextView? = null

    init {
        textView = target
    }

    override fun getDrawable(source: String): Drawable {
        val drawable = BitmapDrawablePlaceHolder()
        val uiHandler = Handler(Looper.getMainLooper())
        uiHandler.post {
            Picasso.get()
                    .load(source)
                    .placeholder(R.drawable.ic_sync_black_24dp)
                    .into(drawable)
        }

        return drawable
    }

    private inner class BitmapDrawablePlaceHolder : BitmapDrawable(), Target {

        protected var drawablea: Drawable? = null

        override fun draw(canvas: Canvas) {
            if (drawablea != null) {
                drawablea!!.draw(canvas)
            }
        }

        fun setDrawable(drawable: Drawable) {
            this.drawablea = drawable
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            drawable.setBounds(0, 0, 40, 40)
            setBounds(0, 0, 40, 40)
            if (textView != null) {
                textView!!.setText(textView!!.getText())
            }
        }

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            setDrawable(BitmapDrawable(textView!!.resources, bitmap))
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {

        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable) {

        }

    }
}