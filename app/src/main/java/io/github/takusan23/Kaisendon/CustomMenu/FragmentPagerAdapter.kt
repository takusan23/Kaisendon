package io.github.takusan23.Kaisendon.CustomMenu

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.github.takusan23.Kaisendon.Fragment.SettingFragment
import io.github.takusan23.Kaisendon.Home
import kotlinx.android.synthetic.main.activity_home.*

class FragmentPagerAdapter(fm: FragmentManager, context: Context) : FragmentPagerAdapter(fm) {

    var context: Context = context
    var customMenuLoadSupport: CustomMenuLoadSupport = CustomMenuLoadSupport(context, (context as Home).nav_view)
    var fragmentList = arrayListOf<Fragment>()

    init {
        fragmentList = customMenuLoadSupport.loadMenuViewPager()
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }

    override fun getCount(): Int {
        return fragmentList.size
    }


}