package com.lhzw.bluetooth.ui.activity

import android.support.v7.widget.LinearLayoutManager
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.WatchAdapter
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.ConnectWatchBean
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_ble_watchs.*

/**
 * Date： 2020/6/10 0010
 * Time： 16:04
 * Created by xtqb.
 */
class BLEWatchListActivity : BaseActivity() {
    private var watchList: MutableList<ConnectWatchBean>? = null
    private val adapter: WatchAdapter by lazy {
        WatchAdapter(this, watchList)
    }

    override fun attachLayoutRes(): Int {
        return R.layout.activity_ble_watchs
    }

    override fun initData() {
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        watchList = ArrayList()
        watchList?.add(ConnectWatchBean("JIANGTAN疆泰5X", "型号：SW2500", "应用领域：森林消防局", "规模搜救"))
        watchList?.add(ConnectWatchBean("JIANGTAN疆泰5X", "型号：SW2500", "应用领域：森林消防局", "规模搜救"))
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(BaseUtils.dip2px(16)))
    }

    override fun initView() {

    }

    override fun initListener() {
        im_back.setOnClickListener {
            this.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        watchList?.let {
            it.clear()
        }
        watchList = null
    }

}