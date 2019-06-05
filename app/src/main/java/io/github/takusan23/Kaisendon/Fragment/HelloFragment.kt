package io.github.takusan23.Kaisendon.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.takusan23.Kaisendon.R

class HelloFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.hello_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}
