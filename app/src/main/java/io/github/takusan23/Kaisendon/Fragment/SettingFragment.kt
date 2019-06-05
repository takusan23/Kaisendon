package io.github.takusan23.Kaisendon.Fragment

import android.os.Bundle

import androidx.preference.PreferenceFragmentCompat

import io.github.takusan23.Kaisendon.R

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.title = getString(R.string.setting)
    }

    override fun onCreatePreferences(bundle: Bundle, s: String) {
        addPreferencesFromResource(R.xml.preference)
    }

}
