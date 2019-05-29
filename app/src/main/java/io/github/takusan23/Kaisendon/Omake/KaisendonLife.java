package io.github.takusan23.Kaisendon.Omake;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import io.github.takusan23.Kaisendon.R;

public class KaisendonLife extends AppCompatActivity {

    private SharedPreferences pref_setting;
    private Switch sw;
    private EditText editText;
    private Button set_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaisendon_life);

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);
        sw = findViewById(R.id.life_switch);
        editText = findViewById(R.id.one_day_toot_count);
        set_Button = findViewById(R.id.life_settting_button);

        setSwitch();
        setTootCount();
    }

    /*スイッチ*/
    private void setSwitch() {
        sw.setChecked(pref_setting.getBoolean("life_mode", false));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = pref_setting.edit();
                editor.putBoolean("life_mode", b);
                editor.apply();
            }
        });
    }

    /*保存*/
    private void setTootCount(){
        editText.setText(pref_setting.getString("one_day_toot_challenge","0"));
        set_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref_setting.edit();
                editor.putString("one_day_toot_challenge", editText.getText().toString());
                editor.apply();
            }
        });
    }
}
