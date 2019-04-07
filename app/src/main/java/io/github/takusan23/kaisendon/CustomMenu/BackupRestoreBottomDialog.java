package io.github.takusan23.kaisendon.CustomMenu;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.R;

public class BackupRestoreBottomDialog extends BottomSheetDialogFragment {

    private Button backup_Button;
    private Button restore_Button;
    private TextView path_TextView;
    private String path;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.backup_restore_bottomdialogfragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        backup_Button = view.findViewById(R.id.backup_restore_backup_Button);
        restore_Button = view.findViewById(R.id.backup_restore_restore_Button);
        path_TextView = view.findViewById(R.id.backup_restore_path_textView);

        //パスをTextViewに入れる
        path_TextView.append("\n" + Environment.getExternalStorageDirectory().getPath() + "/kaisendon");

        backup_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBackupDB();
                //終了
                dismiss();
            }
        });
        restore_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRestore();
                //終了
                dismiss();
                //再読み込み
                getContext().startActivity(new Intent(getContext(), Home.class));
            }
        });
        //ぱす（Android Qから変わった
        if (!Build.VERSION.CODENAME.contains("Q")){
            path = Environment.getExternalStorageDirectory().getPath();
        }else {
            path = "/sdcard/Android/sandbox/io.github.takusan23/kaisendon";
        }
    }

    /*バックアップ、リストアはちゃんとUI作って書き直す予定（）*/

    /**
     * DataBaseバックアップ？
     * <p>
     * https://stackoverflow.com/questions/18635412/restoring-sqlite-db-file
     */
    private void startBackupDB() {
        //Android Pie（9.0）だと/sdcardに作られるけど、
        //Android Q（不明）はScoped Storageの関係上/sdcard/Android/sandbox/io.github.takusan23/kaisendonに作成されます
        backup("CustomMenu.db");
        Toast.makeText(getContext(), getString(R.string.backup_successful) + "\n" + path + "/kaisendon_backup", Toast.LENGTH_SHORT).show();
    }

    /**
     * リストア
     */
    private void startRestore() {
        restore("CustomMenu.db");
        Toast.makeText(getContext(), getString(R.string.restore_successful), Toast.LENGTH_SHORT).show();
    }


    /**
     * Backup
     */
    private void backup(String fileName) {
        try {
            File sd = new File(Environment.getExternalStorageDirectory().getPath() + "/kaisendon_backup");
            // kaisendonディレクトリを作成する
            sd.mkdir();
            //ユーザーが扱えない領域？
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/" + fileName;
                String backupfilename = fileName;
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.fillInStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * リストア
     */
    private void restore(String fileName) {

        //Toast.makeText(getContext(), "リストア実行", Toast.LENGTH_SHORT).show();
        File sd = new File(Environment.getExternalStorageDirectory().getPath() + "/kaisendon_backup");
        //ユーザーが扱えない領域？
        File data = Environment.getDataDirectory();
        try {
            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/" + fileName;
                String backupfilename = fileName;
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    //Toast.makeText(getContext(), "リストアが完了しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}