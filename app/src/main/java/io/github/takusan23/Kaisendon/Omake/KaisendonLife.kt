package io.github.takusan23.Kaisendon.Omake

import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.activity_kaisendon_life.*

class KaisendonLife : AppCompatActivity() {

    private var pref_setting: SharedPreferences? = null
    private var sw: Switch? = null
    private var editText: EditText? = null
    private var set_Button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)
        setContentView(R.layout.activity_kaisendon_life)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)
        sw = findViewById(R.id.life_switch)
        editText = findViewById(R.id.one_day_toot_count)
        set_Button = findViewById(R.id.life_settting_button)

        darkModeSupport.setTextViewThemeColor(findViewById(R.id.kaisendon_life_title))
        darkModeSupport.setDrawableStartTextViewColor(sw!!.parent as LinearLayout)
        darkModeSupport.setDrawableStartTextViewColor(set_Button!!.parent as LinearLayout)
        darkModeSupport.setDrawableStartTextViewColor(one_day_day_count_setup_button!!.parent as LinearLayout)

        setSwitch()
        setTootCount()
        setDatCountSetUP()
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

    /*値を引き継ぐ。がばってるけどべつにずるしても何もないのでセーフ*/
    private fun setDatCountSetUP() {
        //開発者向けオプションが有効になっている必要がある。
        val devOption: Int = Settings.Secure.getInt(this.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        one_day_day_count_setup_button.setOnClickListener {
            if (devOption == 1) {
                val editor = pref_setting!!.edit()
                editor.putString("lunch_count", one_day_day_count_setup_textinput!!.text.toString())
                editor.apply()
            } else {
                Toast.makeText(this,"開発者限定機能です",Toast.LENGTH_SHORT).show()
            }
        }
    }

}
