package io.github.takusan23.Kaisendon.Fragment;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter;
import io.github.takusan23.Kaisendon.R;
import io.github.takusan23.Kaisendon.SnackberProgress;

/**
 * A simple {@link Fragment} subclass.
 */
public class ActivityPubViewer extends Fragment {
    private SharedPreferences pref_setting;

    //RecyclerView
    private ArrayList<ArrayList> recyclerViewList;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private RecyclerView recyclerView;
    private CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter;

    private String path = "";
    private String json = "";


    public ActivityPubViewer() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_pub_viewer, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        recyclerView = view.findViewById(R.id.activity_pub_viwer_recyclerView);

        recyclerViewList = new ArrayList<>();
        //ここから下三行必須
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
        recyclerView.setAdapter(customMenuRecyclerViewAdapter);
        recyclerViewLayoutManager = recyclerView.getLayoutManager();

        getActivity().setTitle(getString(R.string.activity_pub_viewer));

        //ぱす（Android Qから変わった
        if (!Build.VERSION.CODENAME.contains("Q")) {
            path = Environment.getExternalStorageDirectory().getPath() + "/Kaisendon/activity_pub_json/outbox.json";
        } else {
            path = "/sdcard/Android/sandbox/io.github.takusan23/kaisendon/activity_pub_json/outbox.json";
        }

        Toast.makeText(getContext(), getString(R.string.activity_pub_message) + "\n" + path, Toast.LENGTH_LONG).show();

        //ストレージ読み込み、書き込み権限チェック
        int read = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED) {
            //許可済み
            loadOutBoxJSON();
        } else {
            //許可を求める
            //配列なんだねこれ
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4545);
        }

    }

    /**
     * outbox.jsonを読み込む？
     */
    private void loadOutBoxJSON() {
        //くるくる
        SnackberProgress.showProgressSnackber(recyclerView, getContext(), getString(R.string.loading));
        //ぱす
        String kaisendon_path = Environment.getExternalStorageDirectory().getPath() + "/Kaisendon";
        File kaisendon_file = new File(kaisendon_path);
        kaisendon_file.mkdir();
        //ディレクトリ生成
        File file = new File(kaisendon_path + "/activity_pub_json");
        file.mkdir();
        //ファイルかあるか
        File json_file = new File(file.getPath() + "/outbox.json");
        //Nexus 7 2013でクソ重かったので非同期処理
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (json_file.exists()) {
                    try {
                        //JSONデータ取り出し
                        FileInputStream fileInputStream = new FileInputStream(json_file);
                        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        String lineBuffer;
                        while ((lineBuffer = reader.readLine()) != null) {
                            json += lineBuffer;
                        }
                        //RecyclerViewセット
                        setRecyclerView();
                        fileInputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.activity_pub_message) + "\n" + path, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * RecyclerViewセット
     */
    private void setRecyclerView() {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("orderedItems");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject toot_JsonObject = jsonArray.getJSONObject(i);
                if (getActivity() != null && isAdded()) {
                    //配列を作成
                    ArrayList<String> Item = new ArrayList<>();
                    //メモとか通知とかに
                    Item.add("ActivityPub");
                    //内容
                    Item.add("");
                    //ユーザー名
                    Item.add("");
                    //JSONObject
                    Item.add(toot_JsonObject.toString());
                    //ぶーすとした？
                    Item.add("false");
                    //ふぁぼした？
                    Item.add("false");
                    //Mastodon / Misskey
                    Item.add("ActivityPub");
                    //Insatnce/AccessToken
                    Item.add("");
                    Item.add("");
                    Item.add("");
                    //画像表示、こんてんとわーにんぐ
                    Item.add("false");
                    Item.add("false");

                    recyclerViewList.add(Item);
                }
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customMenuRecyclerViewAdapter.notifyDataSetChanged();
                    //くるくる終了
                    SnackberProgress.closeProgressSnackber();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
