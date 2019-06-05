package io.github.takusan23.Kaisendon.Zyanken

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import io.github.takusan23.Kaisendon.R

class ZyankenMenu : AppCompatActivity() {

    //Button
    internal var host_Button: Button? = null
    internal var client_Button: Button? = null
    //LienarLayout
    internal var zyanken_LinearLayout: LinearLayout? = null

    //枠をあける
    internal lateinit var start_host_button: Button
    internal lateinit var start_host_EditText: EditText
    //参加する
    internal lateinit var start_client_button: Button
    internal lateinit var start_client_EditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zyanken_menu)

        val host_Button = findViewById<Button>(R.id.zyanken_host)
        val client_Button = findViewById<Button>(R.id.zyanken_join)
        val zyanken_LinearLayout = findViewById<LinearLayout>(R.id.zyanken_menu_linearLayout)

        supportActionBar!!.title = "じゃんけんメニュー"

        //枠をあける
        host_Button.setOnClickListener {
            //テキストボックスとボタンを動的に生成
            start_host_button = Button(this@ZyankenMenu)
            start_host_button.text = "枠をあける"
            start_host_EditText = EditText(this@ZyankenMenu)
            //Intent
            val intent = Intent(this@ZyankenMenu, ZyankenSetup::class.java)
            intent.putExtra("mode", "host")
            startActivity(intent)
        }

        //参加用
        client_Button.setOnClickListener {
            //テキストボックスとボタンを動的に生成
            start_client_button = Button(this@ZyankenMenu)
            start_client_button.text = "枠をあける"
            start_client_EditText = EditText(this@ZyankenMenu)
            //Intent
            val intent = Intent(this@ZyankenMenu, Zyanken::class.java)
            intent.putExtra("mode", "client")
            startActivity(intent)
        }
    }


}
