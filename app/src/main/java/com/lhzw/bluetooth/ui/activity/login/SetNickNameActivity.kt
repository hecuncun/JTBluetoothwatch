package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set_nick_name.*

/**
 * Created by heCunCun on 2020/8/12
 */
class SetNickNameActivity:BaseActivity() {
    override fun attachLayoutRes(): Int= R.layout.activity_set_nick_name
    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        btn_next.setOnClickListener {
           Intent(this,SetAgeAndSexActivity::class.java).apply {
               startActivity(this)
           }
        }
    }
}