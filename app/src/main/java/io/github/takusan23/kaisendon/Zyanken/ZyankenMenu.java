package io.github.takusan23.kaisendon.Zyanken;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import io.github.takusan23.kaisendon.R;

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

        Button host_Button = findViewById(R.id.zyanken_host);
        Button client_Button = findViewById(R.id.zyanken_join);
        LinearLayout zyanken_LinearLayout = findViewById(R.id.zyanken_menu_linearLayout);

        //枠をあける
        host_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //テキストボックスとボタンを動的に生成
                start_host_button = new Button(ZyankenMenu.this);
                start_host_button.setText("枠をあける");
                start_host_EditText = new EditText(ZyankenMenu.this);
                //Intent
                Intent intent = new Intent(ZyankenMenu.this, ZyankenSetup.class);
                intent.putExtra("mode", "host");
                startActivity(intent);
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
                //Intent
                Intent intent = new Intent(ZyankenMenu.this, Zyanken.class);
                intent.putExtra("mode", "client");
                startActivity(intent);
            }
        });

    }
}
