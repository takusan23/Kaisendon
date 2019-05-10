package io.github.takusan23.kaisendon.CustomMenu.Dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import io.github.takusan23.kaisendon.R;

public class TLQuickSettingsBottomFragment extends BottomSheetDialogFragment {
    private LinearLayout floating_tl_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tl_quick_settings_bottom_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        floating_tl_button = view.findViewById(R.id.tl_qs_flotingtl);;
    }

}
