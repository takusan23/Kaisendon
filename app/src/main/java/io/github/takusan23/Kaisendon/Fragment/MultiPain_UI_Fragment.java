package io.github.takusan23.Kaisendon.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import io.github.takusan23.Kaisendon.Preference_ApplicationContext;
import io.github.takusan23.Kaisendon.R;

public class MultiPain_UI_Fragment extends Fragment {

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        //設定読み込み
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        boolean pannel_4 = pref_setting.getBoolean("pref_multipain_pannel_4", false);

        if (pannel_4) {
            //4枚
            view = inflater.inflate(R.layout.activity_multi_pain_4, container, false);
         }else {
            //2枚
            view = inflater.inflate(R.layout.activity_multi_pain, container, false);
        }

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        String start_fragment = pref_setting.getString("pref_multipain_upper_right","Local");
        if (start_fragment.equals("HomeCard")){
            FragmentChange(new HomeCrad_Fragment());
        }
        if (start_fragment.equals("Home")){
            FragmentChange(new Home_Fragment());
        }
        if (start_fragment.equals("Notification")){
            FragmentChange(new Notification_Fragment());
        }
        if (start_fragment.equals("Local")){
            FragmentChange(new Public_TimeLine_Fragment());
        }
        if (start_fragment.equals("Federated")){
            FragmentChange(new Federated_TimeLine_Fragment());
        }
        if (start_fragment.equals("Start")){
            FragmentChange(new Start_Fragment());
        }
        if (start_fragment.equals("Streaming")){
            FragmentChange(new CustomStreamingFragment());
        }

        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)){
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    public void FragmentChange(Fragment fragment){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment2, fragment);
        transaction.commit();
    }


    @Override
    public void onDetach(){
        super.onDetach();
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        SharedPreferences.Editor editor = pref_setting.edit();
        editor.putBoolean("app_multipain_ui", false);
        editor.apply();
    }

}
