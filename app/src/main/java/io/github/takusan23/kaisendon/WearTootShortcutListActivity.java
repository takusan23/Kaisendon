package io.github.takusan23.kaisendon;


import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class WearTootShortcutListActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener, MenuBuilder.Callback {

    private SharedPreferences pref_setting;
    private ListView listView;
    private Button add_button;
    private Button send_button;
    private Button area_button;
    private EditText editText;

    private String toot_area = "public";

    private SimpleAdapter adapter;

    private ArrayList<String> toot_list = new ArrayList<>();
    private ArrayList<String> icon_list = new ArrayList<>();

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_wear_toot_shortcut_list);
        setTitle(getString(R.string.toot_shortcut_setting));

        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        listView = findViewById(R.id.toot_shortcut_listview);
        add_button = findViewById(R.id.toot_shortcut_add_toot);
        area_button = findViewById(R.id.toot_shortcut_toot_area);
        send_button = findViewById(R.id.toot_shortcut_send);
        editText = findViewById(R.id.toot_shortcut_edittext);

        //ListView
        //メインアカウント
        ArrayList<ListItem> toot = new ArrayList<>();
        adapter = new SimpleAdapter(WearTootShortcutListActivity.this, R.layout.timeline_item, toot);

        //送信
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWearDeviceText("/clear", "clear");
                if (toot_list.size() != 0) {
                    //for
                    for (int i = 0; i < toot_list.size(); i++) {
                        sendWearDeviceText("/toot_text", toot_list.get(i));
                        sendWearDeviceText("/toot_icon", icon_list.get(i));
                    }
                    sendWearDeviceText("/finish", "finish");
                }
            }
        });

        //公開範囲
        final MenuBuilder menuBuilder = new MenuBuilder(WearTootShortcutListActivity.this);
        MenuInflater inflater = new MenuInflater(WearTootShortcutListActivity.this);
        inflater.inflate(R.menu.toot_area_menu, menuBuilder);
        final MenuPopupHelper optionsMenu = new MenuPopupHelper(WearTootShortcutListActivity.this, menuBuilder, area_button);
        optionsMenu.setForceShowIcon(true);
        area_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsMenu.show();
                menuBuilder.setCallback(WearTootShortcutListActivity.this);
            }
        });

        //リストに追加
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EditText
                String edittext_text = editText.getText().toString();
                //List追加
                toot_list.add(edittext_text);
                icon_list.add(toot_area);
                //ListView更新
                adapter.clear();
                for (int i = 0; i < icon_list.size(); i++) {
                    setListView(i);
                }
            }
        });

        //リストから消す
        //長押しで対応
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //削除できるように
                //Snackberで警告
                Snackbar.make(view, getString(R.string.toot_shortcut_delete), Snackbar.LENGTH_SHORT).setAction(getString(R.string.delete_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //削除選択時
                        //リストから消す
                        toot_list.remove(position);
                        icon_list.remove(position);
                        //ListView再読込
                        if (toot_list.size() == icon_list.size()) {
                            adapter.clear();
                            for (int i = 0; i < toot_list.size(); i++) {
                                setListView(i);
                            }
                        }

                    }
                }).show();
                return false;
            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    //WearOS端末からリストを受け取る
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        //空にする
        adapter.clear();
        //Text
        if (messageEvent.getPath().contains("/toot_text")) {
            toot_list.add(new String(messageEvent.getData()));
        }
        //Icon
        if (messageEvent.getPath().contains("/toot_icon")) {
            icon_list.add(new String(messageEvent.getData()));
        }
        //終わり
        if (messageEvent.getPath().contains("/finish")) {
            //ListViewに入れる
            //なんか数が合わないときがよくある
            if (icon_list.size() == toot_list.size()) {
                for (int i = 0; i < icon_list.size(); i++) {
                    setListView(i);
                }
            } else {
                Toast.makeText(WearTootShortcutListActivity.this, getString(R.string.size_error), Toast.LENGTH_SHORT).show();
                toot_list.clear();
                icon_list.clear();
            }

        }
    }

    //ListViewに入れる
    private void setListView(int count) {
        //配列を作成
        ArrayList<String> Item = new ArrayList<>();
        //メモとか通知とかに
        Item.add("toot_shortcut");
        //内容
        Item.add(toot_list.get(count));
        //ユーザー名
        Item.add("");
        //時間、クライアント名等
        Item.add(null);
        //Toot ID 文字列版
        Item.add(null);
        //アバターURL
        Item.add("toot_shortcut " + icon_list.get(count));
        //アカウントID
        Item.add("0");
        //ユーザーネーム
        Item.add("");
        //メディア
        Item.add(null);
        Item.add(null);
        Item.add(null);
        Item.add(null);
        //カード
        Item.add(null);
        Item.add(null);
        Item.add(null);
        Item.add(null);
        ListItem listItem = new ListItem(Item);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(listItem);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }
        });
    }

    private void sendWearDeviceText(String name, String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Node(接続先？)検索
                Task<List<Node>> nodeListTask =
                        Wearable.getNodeClient(WearTootShortcutListActivity.this).getConnectedNodes();
                try {
                    List<Node> nodes = Tasks.await(nodeListTask);
                    for (Node node : nodes) {
                        //sendMessage var1 は名前
                        //sendMessage var2 はメッセージ
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(WearTootShortcutListActivity.this).sendMessage(node.getId(), name, message.getBytes());

                        Integer result = Tasks.await(sendMessageTask);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    //公開範囲
    @Override
    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.toot_area_public:
                toot_area = "public";
                area_button.setText(getString(R.string.visibility_public));
                area_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);
                break;
            case R.id.toot_area_unlisted:
                toot_area = "unlisted";
                area_button.setText(getString(R.string.visibility_unlisted));
                area_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_black_24dp, 0, 0, 0);
                break;
            case R.id.toot_area_local:
                toot_area = "private";
                area_button.setText(getString(R.string.visibility_private));
                area_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_black_24dp, 0, 0, 0);
                break;
            case R.id.toot_area_direct:
                toot_area = "direct";
                area_button.setText(getString(R.string.visibility_direct));
                area_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0);
                break;
        }
        return false;
    }

    @Override
    public void onMenuModeChange(MenuBuilder menuBuilder) {

    }
}
