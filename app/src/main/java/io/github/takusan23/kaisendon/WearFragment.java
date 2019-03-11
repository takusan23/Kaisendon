package io.github.takusan23.kaisendon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.takusan23.kaisendon.Activity.WearTootShortcutListActivity;


public class WearFragment extends Fragment {
    View view;

    private Button accountTransportButton;
    private SharedPreferences pref_setting;
    private Button toot_shortcut_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_wear, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //設定のプリファレンス
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        accountTransportButton = view.findViewById(R.id.account_transport_button);
        toot_shortcut_button = view.findViewById(R.id.toot_shortcut_setting);
        //アクセストークンを変更してる場合のコード
        //アクセストークン
        String AccessToken = null;
        //インスタンス
        String Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");
        }

        //タイトル
        getActivity().setTitle(R.string.kaisendon_wear);

        //アカウント転送ボタン
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;
        accountTransportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWearDeviceText("/instance", finalInstance);
                sendWearDeviceText("/token", finalAccessToken);
                sendWearDeviceText("/finish", "finish");
            }
        });

        toot_shortcut_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WearTootShortcutListActivity.class);
                startActivity(intent);
            }
        });

    }

    private void sendWearDeviceText(String name, String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Node(接続先？)検索
                Task<List<Node>> nodeListTask =
                        Wearable.getNodeClient(getContext()).getConnectedNodes();
                try {
                    List<Node> nodes = Tasks.await(nodeListTask);
                    for (Node node : nodes) {
                        //sendMessage var1 は名前
                        //sendMessage var2 はメッセージ
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(getContext()).sendMessage(node.getId(), name, message.getBytes());

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
}
