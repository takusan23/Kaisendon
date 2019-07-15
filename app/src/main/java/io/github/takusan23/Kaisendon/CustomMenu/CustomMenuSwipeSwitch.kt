package io.github.takusan23.Kaisendon.CustomMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.fragment_custom_menu_swipe_switch.*

class CustomMenuSwipeSwitch : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_menu_swipe_switch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Adapter
        val fragmentPagerAdapter = FragmentPagerAdapter(activity?.supportFragmentManager!!, context!!)
        swipe_switch_view_pager.adapter = fragmentPagerAdapter
    }
}