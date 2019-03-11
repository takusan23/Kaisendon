package io.github.takusan23.kaisendon.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.takusan23.kaisendon.R;

public class Start_Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.activity_home_timeline, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


    }
}
