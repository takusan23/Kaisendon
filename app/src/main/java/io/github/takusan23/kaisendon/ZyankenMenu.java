package io.github.takusan23.kaisendon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ZyankenMenu extends AppCompatActivity {

    //Button
    Button host_Button;
    Button client_Button;
    //LienarLayout
    LinearLayout zyanken_LinearLayout;

    //枠をあける
    Button start_host_button;
    EditText start_host_EditText;
    //参加する
    Button start_client_button;
    EditText start_client_EditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zyanken_menu);

        host_Button.findViewById(R.id.zyanken_host);
        client_Button.findViewById(R.id.zyanken_join);
        zyanken_LinearLayout.findViewById(R.id.zyanken_menu_linearLayout);

        //枠をあける
        host_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //テキストボックスとボタンを動的に生成
                start_host_button = new Button(ZyankenMenu.this);
                start_host_button.setText("枠をあける");
                start_host_EditText = new EditText(ZyankenMenu.this);
                //レイアウトに追加
                zyanken_LinearLayout.addView(start_host_button);
                zyanken_LinearLayout.addView(start_host_EditText);
                //他のレイアウトがあれば消す
                if (start_client_button != null && start_client_EditText != null) {
                    zyanken_LinearLayout.removeView(start_client_button);
                    zyanken_LinearLayout.removeView(start_client_EditText);
                }
            }
        });

        //参加用
        client_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //テキストボックスとボタンを動的に生成
                start_client_button = new Button(ZyankenMenu.this);
                start_client_button.setText("枠をあける");
                start_client_EditText = new EditText(ZyankenMenu.this);
                //レイアウトに追加
                zyanken_LinearLayout.addView(start_client_button);
                zyanken_LinearLayout.addView(start_client_EditText);
                //他のレイアウトがあれば消す
                if (start_host_button != null && start_host_EditText != null) {
                    zyanken_LinearLayout.removeView(start_host_button);
                    zyanken_LinearLayout.removeView(start_host_EditText);
                }
            }
        });

    }
}
