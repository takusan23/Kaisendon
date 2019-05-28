package io.github.takusan23.Kaisendon.CustomMenu.Dialog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.Kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstanceInfoBottomFragment extends BottomSheetDialogFragment {

    private SharedPreferences pref_setting;
    private TextView name_TextView;
    private TextView description_TextView;
    private TextView status_TextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.instance_info_bottom_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        //ダークモード対応
        DarkModeSupport darkModeSupport = new DarkModeSupport(getContext());
        darkModeSupport.setLayoutAllThemeColor((LinearLayout) view);
        name_TextView = view.findViewById(R.id.instance_info_name);
        description_TextView = view.findViewById(R.id.instance_info_description);
        status_TextView = view.findViewById(R.id.instance_info_status);
        getInstanceInfo();
    }

    /*インスタンス情報を叩く*/
    private void getInstanceInfo() {
        Request request = new Request.Builder()
                .url("https://" + pref_setting.getString("main_instance", "") + "/api/v1/instance")
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                if (!response.isSuccessful()) {
                    //失敗
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response_string);
                        JSONObject stats = jsonObject.getJSONObject("stats");
                        String name = jsonObject.getString("title");
                        String description = jsonObject.getString("description");
                        String version = jsonObject.getString("version");
                        String user_count = stats.getString("user_count");
                        String status_count = stats.getString("status_count");
                        String domain_count = stats.getString("domain_count");
                        //UIすれっど
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                name_TextView.append("\n" + name + " " + version);
                                description_TextView.append("\n" + Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
                                status_TextView.append("\n" + getString(R.string.instance_user_count) + " : " + user_count);
                                status_TextView.append("\n" + getString(R.string.instance_status_count) + " : " + status_count);
                                status_TextView.append("\n" + getString(R.string.domain) + " : " + domain_count);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
