package io.github.takusan23.Kaisendon;

import android.app.Application;
import android.content.Context;

public class Preference_ApplicationContext extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
