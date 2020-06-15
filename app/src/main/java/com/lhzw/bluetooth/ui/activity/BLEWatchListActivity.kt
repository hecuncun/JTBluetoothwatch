package com.lhzw.bluetooth.ui.activity

import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_ble_watchs.*

/**
 * Date： 2020/6/10 0010
 * Time： 16:04
 * Created by xtqb.
 */
class BLEWatchListActivity : BaseActivity() {
    override fun attachLayoutRes(): Int {
        return R.layout.activity_ble_watchs
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        im_back.setOnClickListener {
            this.finish()
        }
    }

}