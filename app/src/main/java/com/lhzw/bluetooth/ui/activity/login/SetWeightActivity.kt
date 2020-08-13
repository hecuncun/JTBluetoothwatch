package com.lhzw.bluetooth.ui.activity.login

import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set_nick_name.*
import kotlinx.android.synthetic.main.activity_set_weight.*
import kotlinx.android.synthetic.main.activity_set_weight.iv_back

/**
 * Created by heCunCun on 2020/8/12
 */
class SetWeightActivity:BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_set_weight

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        hv_current.setOnItemChangedListener { index, value ->
            tv_current.text=value.toString()
        }

        hv_hope.setOnItemChangedListener { index, value ->
            tv_hope.text=value.toString()
        }


    }
}