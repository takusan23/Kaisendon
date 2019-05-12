package io.github.takusan23.kaisendon.CustomMenu.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import java.util.ArrayList;

import io.github.takusan23.kaisendon.Activity.KonoAppNiTuite;
import io.github.takusan23.kaisendon.Activity.LoginActivity;
import io.github.takusan23.kaisendon.Activity.UserActivity;
import io.github.takusan23.kaisendon.CustomMenu.CustomMenuLoadSupport;
import io.github.takusan23.kaisendon.CustomMenu.CustomMenuSQLiteHelper;
import io.github.takusan23.kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.kaisendon.DesktopTL.DesktopFragment;
import io.github.takusan23.kaisendon.FloatingTL.FloatingTL;
import io.github.takusan23.kaisendon.Fragment.AccountListFragment;
import io.github.takusan23.kaisendon.Fragment.ActivityPubViewer;
import io.github.takusan23.kaisendon.Fragment.Bookmark_Frament;
import io.github.takusan23.kaisendon.Fragment.License_Fragment;
import io.github.takusan23.kaisendon.Fragment.SettingFragment;
import io.github.takusan23.kaisendon.Fragment.WearFragment;
import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.R;

public class TLQuickSettingSnackber {
    private Activity context;
    private View view;
    private Snackbar snackbar;

    private BottomNavigationView bottomNavigationView;
    private Switch tts_Switch;
    private EditText color_EditText;
    //private FragmentTransaction transaction;
    private SharedPreferences pref_setting;
    private NavigationView navigationView;
    private CustomMenuSQLiteHelper helper;
    private SQLiteDatabase db;
    private CustomMenuLoadSupport customMenuLoadSupport;
    private LinearLayout switch_LinearLayout;
    private ArrayList<String> list;
    private DarkModeSupport darkModeSupport;
    private ImageView icon;
    private LinearLayout tl_qs_color_edittext_linearlayout;

    public TLQuickSettingSnackber(Activity context, View view) {
        this.context = context;
        this.snackbar = setSnackBer();
    }

    private Snackbar setSnackBer() {
        View view = context.findViewById(R.id.container_public);
        Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //TextViewを非表示にする
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setVisibility(View.INVISIBLE);
        //Linearlayout
        LinearLayout main_LinearLayout = new LinearLayout(context);
        main_LinearLayout.setOrientation(LinearLayout.VERTICAL);
        main_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(context).inflate(R.layout.tl_quick_settings_bottom_fragment, main_LinearLayout);
        snackBer_viewGrop.addView(main_LinearLayout, 0);

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        bottomNavigationView = main_LinearLayout.findViewById(R.id.tl_qs_menu);
        tts_Switch = main_LinearLayout.findViewById(R.id.tl_qs_tts_switch);
        color_EditText = main_LinearLayout.findViewById(R.id.tl_qs_color_edittext);
        icon = main_LinearLayout.findViewById(R.id.imageView2);
        navigationView = context.findViewById(R.id.nav_view);
        customMenuLoadSupport = new CustomMenuLoadSupport(context, navigationView);
        switch_LinearLayout = main_LinearLayout.findViewById(R.id.tl_qs_switch_linearlayout);

        darkModeSupport = new DarkModeSupport(context);
        darkModeSupport.setBottomNavigationBerThemeColor(bottomNavigationView);

        setClickEvent();
        setDarkmodeSwitch();

        return snackbar;
    }

    private void setClickEvent() {
        //trueでアニメーションされる？
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tl_qs_account:
                        setAccountPopupMenu();
                        break;
                    case R.id.tl_qs_bookmark:
                        setBookmark();
                        break;
                    case R.id.tl_qs_desktop_mode:
                        setDesktopMode();
                        break;
                    case R.id.tl_qs_floating_tl:
                        showFloatingTL();
                        break;
                    case R.id.tl_qs_sonota:
                        setSonotaMenu();
                        break;
                }
                return true;
            }
        });
    }


    /*読み上げするかを返す*/
    public boolean getTimelineTTS() {
        boolean is = false;
        if (tts_Switch != null && tts_Switch.isChecked()) {
            is = true;
        }
        return is;
    }

    /*ハイライトする文字を返す*/
    public String getHighlightText() {
        String text = "";
        if (color_EditText != null) {
            text = color_EditText.getText().toString();
        }
        return text;
    }

    /*Floating TL*/
    private void showFloatingTL() {
        if (context != null) {
            Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.container_container);
            if (fragment instanceof CustomMenuTimeLine) {
                FloatingTL floatingTL = new FloatingTL(context, fragment.getArguments().getString("json"));
                floatingTL.setNotification();
            } else {
                Toast.makeText(context,context.getString(R.string.floating_tl_error_custom_tl),Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*デスクトップモード*/
    private void setDesktopMode() {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_container, new DesktopFragment(), "desktop");
        transaction.commit();
    }

    /*ぶっくまーく*/
    private void setBookmark() {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_container, new Bookmark_Frament());
        transaction.commit();
    }

    /*ポップアップメニュー*/
    @SuppressLint("RestrictedApi")
    private void setAccountPopupMenu() {
        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(context);
        MenuInflater inflater = new MenuInflater(context);
        inflater.inflate(R.menu.tl_qs_account_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(context, menuBuilder, bottomNavigationView);
        optionsMenu.setForceShowIcon(true);
        //表示
        optionsMenu.show();
        //押したときの反応
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home_menu_login:
                        Intent login = new Intent(context, LoginActivity.class);
                        context.startActivity(login);
                        break;
                    case R.id.home_menu_account_list:
                        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container_container, new AccountListFragment());
                        transaction.commit();
                        break;
                    case R.id.home_menu_account:
                        Intent intent = new Intent(context, UserActivity.class);
                        if (list!=null){
                            if (CustomMenuTimeLine.isMisskeyMode()) {
                                intent.putExtra("Account_ID", CustomMenuTimeLine.getAccount_id());
                            } else {
                                intent.putExtra("Account_ID", list.get(0));
                            }
                            intent.putExtra("my", true);
                            context.startActivity(intent);
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menuBuilder) {

            }
        });
    }


    /*その他のメニュー*/
    @SuppressLint("RestrictedApi")
    private void setSonotaMenu() {
        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(context);
        MenuInflater inflater = new MenuInflater(context);
        inflater.inflate(R.menu.tl_qs_sonota_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(context, menuBuilder, bottomNavigationView);
        optionsMenu.setForceShowIcon(true);
        //表示
        optionsMenu.show();
        //押したときの反応
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.home_menu_activity_pub_viewer:
                        transaction.replace(R.id.container_container, new ActivityPubViewer());
                        transaction.commit();
                        break;
                    case R.id.home_menu_reload_menu:
                        //再読み込み
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.custom_menu);
                        customMenuLoadSupport.loadCustomMenu(null);
                        break;
                    case R.id.home_menu_setting:
                        transaction.replace(R.id.container_container, new SettingFragment());
                        transaction.commit();
                        break;
                    case R.id.home_menu_license:
                        transaction.replace(R.id.container_container, new License_Fragment());
                        transaction.commit();
                        break;
                    case R.id.home_menu_thisapp:
                        Intent thisApp = new Intent(context, KonoAppNiTuite.class);
                        context.startActivity(thisApp);
                        break;
                    case R.id.home_menu_privacy_policy:
                        showPrivacyPolicy();
                        break;
                    case R.id.home_menu_wear:
                        transaction.replace(R.id.container_container, new WearFragment());
                        transaction.commit();
                        break;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menuBuilder) {

            }
        });
    }

    /**
     * プライバシーポリシー
     */
    private void showPrivacyPolicy() {
        String githubUrl = "https://github.com/takusan23/Kaisendon/blob/master/kaisendon-privacy-policy.md";
        if (pref_setting.getBoolean("pref_chrome_custom_tabs", true)) {
            Bitmap back_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_arrow_back);
            String custom = CustomTabsHelper.getPackageNameToUse(context);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(custom);
            customTabsIntent.launchUrl(context, Uri.parse(githubUrl));
        } else {
            Uri uri = Uri.parse(githubUrl);
            Intent browser = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(browser);
        }
    }

    /*Android Pie以前ユーザー用にダークモードスイッチを用意する*/
    private void setDarkmodeSwitch() {
        if (!Build.VERSION.CODENAME.equals("Q")) {
            Switch sw = new Switch(context);
            sw.setText(context.getText(R.string.darkmode));
            sw.setTextColor(context.getColor(R.color.white));
            sw.setChecked(pref_setting.getBoolean("darkmode", false));
            SharedPreferences.Editor editor = pref_setting.edit();
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        editor.putBoolean("darkmode", true);
                    } else {
                        editor.putBoolean("darkmode", false);
                    }
                    context.startActivity(new Intent(context, Home.class));
                    editor.apply();
                }
            });
            switch_LinearLayout.addView(sw);
        }
    }

    public void showSnackBer() {
        //表示
        snackbar.show();
    }

    public void dismissSnackBer() {
        //表示
        snackbar.dismiss();
    }

    /*SnackBer*/
    public Snackbar getSnackbar() {
        //表示
        return snackbar;
    }

    /*配列を設定*/
    public void setList(ArrayList<String> list){
        this.list = list;
    }

}
