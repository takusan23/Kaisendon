package io.github.takusan23.Kaisendon.Omake

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.takusan23.Kaisendon.R

class KaisendonLife : AppCompatActivity() {

    private var pref_setting: SharedPreferences? = null
    private var sw: Switch? = null
    private var editText: EditText? = null
    private var set_Button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kaisendon_life)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)
        sw = findViewById(R.id.life_switch)
        editText = findViewById(R.id.one_day_toot_count)
        set_Button = findViewById(R.id.life_settting_button)

        setSwitch()
        setTootCount()
    }

    /*スイッチ*/
    private fun setSwitch() {
        sw!!.isChecked = pref_setting!!.getBoolean("life_mode", false)
        sw!!.setOnCheckedChangeListener { compoundButton, b ->
            val editor = pref_setting!!.edit()
            editor.putBoolean("life_mode", b)
            editor.apply()
        }
    }

    /*保存*/
    private fun setTootCount() {
        editText!!.setText(pref_setting!!.getString("one_day_toot_challenge", "0"))
        set_Button!!.setOnClickListener {
            val editor = pref_setting!!.edit()
            editor.putString("one_day_toot_challenge", editText!!.text.toString())
            editor.apply()
        }
    }
}
