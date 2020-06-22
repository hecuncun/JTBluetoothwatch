package com.lhzw.bluetooth.ui.activity

import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by heCunCun on 2020/6/22
 * 年月周统计表
 */
class StatisticsActivity:BaseActivity() {
    override fun attachLayoutRes(): Int= R.layout.activity_statistics

    override fun initData() {
    }

    override fun initView() {
        toolbar_title.text="日常统计"
        toolbar_left_img.setImageResource(R.drawable.bg_back_selector)
        toolbar_left_img.visibility= View.VISIBLE
    }

    override fun initListener() {
    }
}