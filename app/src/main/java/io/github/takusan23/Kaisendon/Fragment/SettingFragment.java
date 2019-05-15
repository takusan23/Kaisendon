package io.github.takusan23.Kaisendon.Fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import io.github.takusan23.Kaisendon.R;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.setting));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preference);
    }

}
