package io.github.takusan23.Kaisendon

import android.app.Application
import android.content.Context

class Preference_ApplicationContext : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        var context: Context? = null
            private set
    }
}