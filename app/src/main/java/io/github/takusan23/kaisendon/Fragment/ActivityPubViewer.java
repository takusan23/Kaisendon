package io.github.takusan23.kaisendon.Fragment;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter;
import io.github.takusan23.kaisendon.R;

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
            path = Environment.getExternalStorageDirectory().getPath();
        } else {
            path = "/sdcard/Android/sandbox/io.github.takusan23/kaisendon";
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
        //ディレクトリ生成
        String json = "";
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/activity_pub_json");
        file.mkdir();
        //ファイルかあるか
        File json_file = new File(file.getPath() + "/outbox.json");
        if (json_file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(json_file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String lineBuffer;
                while ((lineBuffer = reader.readLine()) != null) {
                    json += lineBuffer;
                }

                fileInputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.activity_pub_message) + "\n" + path, Toast.LENGTH_LONG).show();
        }

    }

}
