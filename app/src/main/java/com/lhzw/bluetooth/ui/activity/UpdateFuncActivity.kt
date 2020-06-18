package com.lhzw.bluetooth.ui.activity

import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_update_func_list.*

/**
 * Date： 2020/6/18 0018
 * Time： 10:42
 * Created by xtqb.
 */

class UpdateFuncActivity : BaseActivity() {
    override fun attachLayoutRes() = R.layout.activity_update_func_list

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        startProgress()

        startProgress1()

        im_back.setOnClickListener {
            this.finish()
        }

    }

    fun startProgress() {
        progesss_app.max = 100
        progesss_app.progress = 0
        var counter = 0
        Thread {
            while (counter < 101) {
                counter++
                progesss_app.progress = counter
                Thread.sleep(50)
            }
        }.start()
    }


    fun startProgress1() {
        progesss_watch.max = 100
        progesss_watch.progress = 0
        var counter = 0
        Thread {
            while (counter < 101) {
                counter++
                progesss_watch.progress = counter
                Thread.sleep(50)
            }
        }.start()
    }
}