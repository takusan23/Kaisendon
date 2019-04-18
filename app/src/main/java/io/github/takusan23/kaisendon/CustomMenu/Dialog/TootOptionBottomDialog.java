package io.github.takusan23.kaisendon.CustomMenu.Dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.github.takusan23.kaisendon.Activity.UserActivity;
import io.github.takusan23.kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.kaisendon.R;

public class TootOptionBottomDialog extends BottomSheetDialogFragment {

    private View view;
    private TextView account_Button;
    private TextView bookmark_Button;
    private TextView copy_TextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.toot_option_button_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        account_Button = view.findViewById(R.id.toot_option_account_button);
        bookmark_Button = view.findViewById(R.id.toot_option_bookmark_button);
        copy_TextView = view.findViewById(R.id.toot_option_copy_button);
        //クリック
        account_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(account_Button.getContext(), UserActivity.class);
                //IDを渡す
                if (CustomMenuTimeLine.isMisskeyMode()) {
                    intent.putExtra("Misskey", true);
                }
                intent.putExtra("Account_ID", getArguments().getString("user_id"));
                account_Button.getContext().startActivity(intent);

            }
        });
        //クリップボード
        copy_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(getArguments().getString("status_text"), "copy"));
                        Toast.makeText(getContext(),getString(R.string.copy) + " : " + getArguments().getString("status_text"),Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }
            }
        });
    }
}
