package io.github.takusan23.Kaisendon

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.activity_toot.view.*
import kotlinx.android.synthetic.main.carview_toot_layout.view.*
import java.util.*

class TootCardView(val context: Context,val isMisskey:Boolean){

    val cardView = CardView(context)
    val linearLayout = LinearLayout(context)
    
    lateinit var tootEditText:TextView
    
    init{
        //初期化
        val laytoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        laytoutInflater.inflate(R.layout.carview_toot_layout,linearLayout)
        tootEditText = linearLayout.toot_card_textinput
    }

    fun setClickEvent(){
        linearLayout.toot_card_attach_image.setOnClickListener {
            setAttachImage()
        }
        linearLayout.device_info_button.setOnClickListener {
            showDeviceInfo()
        }
        linearLayout.toot_card_visibility_button.setOnClickListener {
            showVisibility()
        }
        linearLayout.toot_card_vote_button.setOnClickListener {
            showVote()
        }
        linearLayout.toot_card_scheduled_time_button.setOnClickListener {
            showScheduled()
        }
        linearLayout.toot_card_paint_post_button.setOnClickListener {
            showPaint()
        }
    }

    fun setAttachImage(){
        
    }
    
    fun showVisibility(){
        
    }
    
    fun showDeviceInfo(){
        val view = linearLayout.toot_card_device_button
        //ポップアップメニュー作成
        val device_menuBuilder = MenuBuilder(context)
        val device_inflater = MenuInflater(context)
        device_inflater.inflate(R.menu.device_info_menu, device_menuBuilder)
        val device_optionsMenu = MenuPopupHelper(context, device_menuBuilder, view)
        device_optionsMenu.setForceShowIcon(true)
        //コードネーム変換（手動
        var codeName = ""
        when(Build.VERSION.SDK_INT){
            Build.VERSION_CODES.N->{
                codeName = "Nougat"
            }
            Build.VERSION_CODES.O->{
                codeName = "Oreo"
            }
            Build.VERSION_CODES.P->{
                codeName = "Pie"
            }
            Build.VERSION_CODES.Q->{
                codeName = "10"
            }
        }
        view.setOnClickListener {
            device_optionsMenu.show()
        }
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        device_menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                //名前
                if (menuItem.title.toString().contains(context.getString(R.string.device_name))) {
                    tootEditText.append(Build.MODEL)
                    tootEditText.append("\r\n")
                }
                //Androidバージョン
                if (menuItem.title.toString().contains(context.getString(R.string.android_version))) {
                    tootEditText.append(Build.VERSION.RELEASE)
                    tootEditText.append("\r\n")
                }
                //めーかー
                if (menuItem.title.toString().contains(context.getString(R.string.maker))) {
                    tootEditText.append(Build.BRAND)
                    tootEditText.append("\r\n")
                }
                //SDKバージョン
                if (menuItem.title.toString().contains(context.getString(R.string.sdk_version))) {
                    tootEditText.append(Build.VERSION.SDK_INT.toString())
                    tootEditText.append("\r\n")
                }
                //コードネーム
                if (menuItem.title.toString().contains(context.getString(R.string.codename))) {
                    tootEditText.append(codeName)
                    tootEditText.append("\r\n")
                }
                //バッテリーレベル
                if (menuItem.title.toString().contains(context.getString(R.string.battery_level))) {
                    tootEditText.append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString() + "%")
                    tootEditText.append("\r\n")
                }
                return false
            }
            override fun onMenuModeChange(menuBuilder: MenuBuilder) {

            }
        })
    }
    
    fun showScheduled(){
        if(linearLayout.toot_card_scheduled_linearlayout.visibility== View.GONE){
            linearLayout.toot_card_scheduled_linearlayout.visibility == View.VISIBLE
        }else{
            linearLayout.toot_card_scheduled_linearlayout.visibility == View.GONE
        }
        linearLayout.toot_card_scheduled_time_button.setOnClickListener {
            //時間ピッカー
            showTimePicker(linearLayout.toot_card_scheduled_time_textview)
        }
        linearLayout.toot_card_scheduled_date_button.setOnClickListener {
            //日付ピッカー
            showDatePicker(linearLayout.toot_card_scheduled_date_textview)
        }

    }
    
    fun showVote(){
        if(linearLayout.toot_card_vote_linearlayout.visibility== View.GONE){
            linearLayout.toot_card_vote_linearlayout.visibility == View.VISIBLE
        }else{
            linearLayout.toot_card_vote_linearlayout.visibility == View.GONE
        }
    }
    
    fun showPaint(){
        
    }
    
    fun postStatus(){
        
    }


    /**
     * DatePicker
     */
    private fun showDatePicker(textView: TextView) {
        val date = arrayOf("")

        val calendar = Calendar.getInstance()
        val dateBuilder = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            var month = month
            var month_string = ""
            var day_string = ""
            //1-9月は前に0を入れる
            if (month++ <= 9) {
                month_string = "0" + month++.toString()
            } else {
                month_string = month++.toString()
            }
            //1-9日も前に0を入れる
            if (dayOfMonth <= 9) {
                day_string = "0$dayOfMonth"
            } else {
                day_string = dayOfMonth.toString()
            }
            textView.text = year.toString() + month_string + day_string + "T"
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        dateBuilder.show()
    }

    /**
     * TimePicker
     */
    private fun showTimePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            var hourOfDay = hourOfDay
            var hour_string = ""
            var minute_string = ""
            //1-9月は前に0を入れる
            if (hourOfDay <= 9) {
                hour_string = "0" + hourOfDay++.toString()
            } else {
                hour_string = hourOfDay++.toString()
            }
            //1-9日も前に0を入れる
            if (minute <= 9) {
                minute_string = "0$minute"
            } else {
                minute_string = minute.toString()
            }
            textView.text = hour_string + minute_string + "00" + "+0900"
        }, hour, minute, true)
        dialog.show()
    }



}