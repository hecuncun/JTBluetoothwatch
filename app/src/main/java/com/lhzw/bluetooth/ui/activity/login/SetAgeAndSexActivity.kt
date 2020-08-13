package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set_age_and_sex.*
import kotlinx.android.synthetic.main.activity_set_age_and_sex.btn_next
import kotlinx.android.synthetic.main.activity_set_age_and_sex.iv_back
import kotlinx.android.synthetic.main.activity_set_nick_name.*

/**
 * Created by heCunCun on 2020/8/12
 */
class SetAgeAndSexActivity : BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_set_age_and_sex
    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        btn_next.setOnClickListener {
            Intent(this,SetWeightActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}