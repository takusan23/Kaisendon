package io.github.takusan23.Kaisendon.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.takusan23.Kaisendon.R

class Start_Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.activity_home_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }
}
