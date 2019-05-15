package io.github.takusan23.Kaisendon.CustomMenu.Dialog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import io.github.takusan23.Kaisendon.Activity.KonoAppNiTuite;
import io.github.takusan23.Kaisendon.Activity.LoginActivity;
import io.github.takusan23.Kaisendon.Activity.UserActivity;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuLoadSupport;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.Kaisendon.DesktopTL.DesktopFragment;
import io.github.takusan23.Kaisendon.FloatingTL.FloatingTL;
import io.github.takusan23.Kaisendon.Fragment.AccountListFragment;
import io.github.takusan23.Kaisendon.Fragment.ActivityPubViewer;
import io.github.takusan23.Kaisendon.Fragment.Bookmark_Frament;
import io.github.takusan23.Kaisendon.Fragment.License_Fragment;
import io.github.takusan23.Kaisendon.Fragment.SettingFragment;
import io.github.takusan23.Kaisendon.Fragment.WearFragment;
import io.github.takusan23.Kaisendon.Home;
import io.github.takusan23.Kaisendon.R;

public class TLQuickSettingsBottomFragment extends BottomSheetDialogFragment {

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
    private TextInputLayout textInputLayout;
    private DarkModeSupport darkModeSupport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tl_quick_settings_bottom_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        bottomNavigationView = view.findViewById(R.id.tl_qs_menu);
        tts_Switch = view.findViewById(R.id.tl_qs_tts_switch);
        color_EditText = view.findViewById(R.id.tl_qs_color_edittext);
        navigationView = getActivity().findViewById(R.id.nav_view);
        customMenuLoadSupport = new CustomMenuLoadSupport(getContext(), navigationView);
        switch_LinearLayout = view.findViewById(R.id.tl_qs_switch_linearlayout);
        darkModeSupport = new DarkModeSupport(getContext());
        darkModeSupport.setLayoutAllThemeColor(view.findViewById(R.id.tl_qs_main_linearlayout));
        setClickEvent();
        setDarkmodeSwitch();
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
        if (getActivity() != null) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container_container);
            if (fragment != null && fragment instanceof CustomMenuTimeLine) {
                FloatingTL floatingTL = new FloatingTL(getContext(), fragment.getArguments().getString("json"));
                floatingTL.setNotification();
            }
        }
    }

    /*デスクトップモード*/
    private void setDesktopMode() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_container, new DesktopFragment(), "desktop");
        transaction.commit();
    }

    /*ぶっくまーく*/
    private void setBookmark() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_container, new Bookmark_Frament());
        transaction.commit();
    }

    /*ポップアップメニュー*/
    @SuppressLint("RestrictedApi")
    private void setAccountPopupMenu() {
        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(getContext());
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(R.menu.tl_qs_account_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(getContext(), menuBuilder, bottomNavigationView);
        optionsMenu.setForceShowIcon(true);
        //表示
        optionsMenu.show();
        //押したときの反応
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home_menu_login:
                        Intent login = new Intent(getContext(), LoginActivity.class);
                        startActivity(login);
                        break;
                    case R.id.home_menu_account_list:
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container_container, new AccountListFragment());
                        transaction.commit();
                        break;
                    case R.id.home_menu_account:
                        Intent intent = new Intent(getContext(), UserActivity.class);
                        if (CustomMenuTimeLine.isMisskeyMode()) {
                            intent.putExtra("Account_ID", CustomMenuTimeLine.getAccount_id());
                        } else {
                            intent.putExtra("Account_ID", getArguments().getString("account_id"));
                        }
                        intent.putExtra("my", true);
                        startActivity(intent);
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
        MenuBuilder menuBuilder = new MenuBuilder(getContext());
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(R.menu.tl_qs_sonota_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(getContext(), menuBuilder, bottomNavigationView);
        optionsMenu.setForceShowIcon(true);
        //表示
        optionsMenu.show();
        //押したときの反応
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
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
                        Intent thisApp = new Intent(getContext(), KonoAppNiTuite.class);
                        startActivity(thisApp);
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
            Bitmap back_icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_arrow_back);
            String custom = CustomTabsHelper.getPackageNameToUse(getContext());
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(custom);
            customTabsIntent.launchUrl(getContext(), Uri.parse(githubUrl));
        } else {
            Uri uri = Uri.parse(githubUrl);
            Intent browser = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(browser);
        }
    }

    /*Android Pie以前ユーザー用にダークモードスイッチを用意する*/
    private void setDarkmodeSwitch() {
        if (!Build.VERSION.CODENAME.equals("Q")) {
            Switch sw = new Switch(getContext());
            darkModeSupport.setSwitchThemeColor(sw);
            sw.setText(getText(R.string.darkmode));
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
                    getContext().startActivity(new Intent(getContext(), Home.class));
                    editor.apply();
                }
            });
            switch_LinearLayout.addView(sw);
        }
    }
}
