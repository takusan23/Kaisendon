package io.github.takusan23.Kaisendon

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.carview_toot_layout.view.*

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
        
    }
    
    fun showVote(){
        
    }
    
    fun showPaint(){
        
    }
    
    fun postStatus(){
        
    }
    


}