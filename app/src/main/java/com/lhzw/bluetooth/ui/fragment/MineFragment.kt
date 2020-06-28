package com.lhzw.bluetooth.ui.fragment

import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bean.PersonalInfoBean
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/6/24
 */
class MineFragment:BaseFragment() {
    companion object {
        fun getInstance(): MineFragment = MineFragment()
    }
    override fun attachLayoutRes(): Int= R.layout.fragment_mine

    override fun initView(view: View) {

    }

    override fun lazyLoad() {
        val list = LitePal.findAll<PersonalInfoBean>()
        val bean =list[0]
    }
}