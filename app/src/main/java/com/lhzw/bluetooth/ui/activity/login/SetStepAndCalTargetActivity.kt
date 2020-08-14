package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set_step_and_cal_target.*
import kotlinx.android.synthetic.main.activity_set_weight.btn_next
import kotlinx.android.synthetic.main.activity_set_weight.iv_back

/**
 * Created by heCunCun on 2020/8/14
 */
class SetStepAndCalTargetActivity:BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_set_step_and_cal_target

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        hv_step.setOnItemChangedListener { index, value ->
            tv_step.text=value.toString()
        }

        hv_cal.setOnItemChangedListener { index, value ->
            tv_cal.text=value.toString()
        }
        btn_next.setOnClickListener {
            Intent(this,SetHeightActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}