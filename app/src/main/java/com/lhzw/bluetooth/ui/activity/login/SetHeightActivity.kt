package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.ui.activity.MainActivity
import kotlinx.android.synthetic.main.activity_set_height.*

/**
 * Created by heCunCun on 2020/8/14
 */
class SetHeightActivity:BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_set_height

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        hv_height.setOnItemChangedListener { index, value ->
            tv_height.text=value.toString()
        }

        iv_back.setOnClickListener {
            finish()
        }

        btn_next.setOnClickListener {
            Intent(this,MainActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}