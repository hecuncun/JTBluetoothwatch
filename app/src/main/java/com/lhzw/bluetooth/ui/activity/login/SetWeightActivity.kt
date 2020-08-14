package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set_weight.*

/**
 * Created by heCunCun on 2020/8/12
 */
class SetWeightActivity : BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_set_weight

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        hv_weight.setOnItemChangedListener { index, value ->
            tv_weight.text = value.toString()
        }

        hv_hope.setOnItemChangedListener { index, value ->
            tv_hope.text = value.toString()
        }
        btn_next.setOnClickListener {
            Intent(this, SetStepAndCalTargetActivity::class.java).apply {
                startActivity(this)
            }
        }

    }
}