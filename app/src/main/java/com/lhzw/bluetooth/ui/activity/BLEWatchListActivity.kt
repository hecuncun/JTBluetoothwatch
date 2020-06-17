package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.WatchAdapter
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.ConnectWatchBean
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.ToastUtils
import com.lhzw.bluetooth.view.SpaceItemDecoration
import com.lhzw.bluetooth.widget.LoadingView
import kotlinx.android.synthetic.main.activity_ble_watchs.*


/**
 * Date： 2020/6/10 0010
 * Time： 16:04
 * Created by xtqb.
 */
class BLEWatchListActivity : BaseActivity() {
    private val SCAN_DURATION: Long = 20000//扫描持续时长10s
    private var watchList: MutableList<ConnectWatchBean>? = null
    private var bleManager: BluetoothManager? = null
    private val PERMISS_REQUEST_BLE_CODE = 0X357
    private var loadingView: LoadingView? = null
    private val TAG = BLEWatchListActivity::class.java.simpleName
    private var toothMap: MutableMap<String, BluetoothDevice>? = null
    private val adapter: WatchAdapter by lazy {
        WatchAdapter(this, watchList)
    }

    override fun attachLayoutRes(): Int {
        return R.layout.activity_ble_watchs
    }

    override fun initData() {
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var state = false
        bleManager?.apply {
            state = adapter.isEnabled//拿到蓝牙状态
        }
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        watchList = ArrayList()
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(BaseUtils.dip2px(16)))

        if (!state) {
            ToastUtils.toastError("蓝牙未开启, 请开启蓝牙")
        } else {
            loadingView = LoadingView(this)
            loadingView?.setLoadingTitle("搜索中...")
            if (checkPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                startScan()
            } else {
                requestPermission(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISS_REQUEST_BLE_CODE)
            }
        }
        toothMap = HashMap()
    }

    override fun initView() {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISS_REQUEST_BLE_CODE) {
            startScan()
        }
    }

    private fun startScan() {
        // 判断是否在搜索，如果在搜索，就取消
        bleManager?.apply {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            registerBrodcast()
            //开始搜索
            adapter.startDiscovery()
            loadingView?.show()
            Handler().postDelayed({
                bleManager?.apply {
                    if (adapter.isDiscovering) {
                        adapter.cancelDiscovery()
                    }
                }
            }, SCAN_DURATION)
        }
    }

    private fun registerBrodcast() {
        // 找到设备的广播
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        // 注册广播
        registerReceiver(receiver, filter)
        // 搜索完成的广播
        val filter1 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        // 注册广播
        registerReceiver(receiver, filter1)
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 收到的广播类型
            val action = intent!!.action
            // 发现设备的广播
            if (BluetoothDevice.ACTION_FOUND == action) {
                // 从intent中获取设备
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                // 没否配对
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        if (!TextUtils.isEmpty(device.name) && device.name.contains("SW2500")) {
                            toothMap?.apply {
                                if (get(device.name) == null) {
                                    put(device.name, device)
                                    watchList?.add(ConnectWatchBean(device.name, "型号：SW2500", "应用领域：森林消防局", "规模搜救"))
                                    adapter.notifyDataSetChanged()
                                }
                            }

                        }
                    }
                }
                // 搜索完成
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                // 关闭进度条
                loadingView?.dismiss()
                // ArrayList<String> printNameArray = new ArrayList<String>(booth.keySet());
                Log.e(TAG, "onReceive: 搜索完成")
            }
        }

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
        bleManager?.apply {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
        }
        unregisterReceiver(receiver)
        bleManager = null
        loadingView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        loadingView = null
    }

}