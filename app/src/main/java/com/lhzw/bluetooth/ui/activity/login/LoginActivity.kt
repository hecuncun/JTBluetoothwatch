package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login_2.*

/**
 * Created by heCunCun on 2020/8/7
 *  新的登录页UI
 */
class LoginActivity:BaseActivity() {
    override fun attachLayoutRes(): Int= R.layout.activity_login_2

    override fun initData() {

    }

    override fun initView() {
        tv_register.setOnClickListener {
            Intent(this,RegisterActivity::class.java).apply {
                startActivity(this)
            }
        }

        btn_login.setOnClickListener {
            Intent(this,SetNickNameActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    override fun initListener() {

    }
}