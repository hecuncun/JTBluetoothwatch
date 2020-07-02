package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ClipDrawable
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.WatchAdapter
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.ConnectWatchBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.*
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.service.BleConnectService
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.view.SpaceItemDecoration
import com.lhzw.bluetooth.widget.LoadingView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_ble_watchs.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Date： 2020/6/10 0010
 * Time： 16:04
 * Created by xtqb.
 */
class BLEWatchListActivity : BaseActivity() {

    private var connectedDeviceName: String by Preference(Constants.CONNECT_DEVICE_NAME, "")//缓存设备名称
    private var lastDeviceMacAddress: String by Preference(Constants.LAST_DEVICE_ADDRESS, "")//缓存mac
    private var autoConnect: Boolean by Preference(Constants.AUTO_CONNECT, false)//是否自动连接
    private val REQUEST_CODE = 0x333
    private val PERMISS_REQUEST_CODE = 0x356
    private val REQUEST_ENABLE_BLE = 101
    private var bleManager: BluetoothManager? = null

    private var watchList: MutableList<ConnectWatchBean>? = null
    private val TAG = BLEWatchListActivity::class.java.simpleName

    override fun useEventBus() = true
    private val adapter: WatchAdapter by lazy {
        WatchAdapter(this, watchList)
    }

    override fun attachLayoutRes(): Int {
        return R.layout.activity_ble_watchs
    }

    override fun initData() {

    }
    private lateinit var drawable:ClipDrawable
    val animator = ValueAnimator.ofInt(10000)
    private fun initProgress() {
        animator.duration=5000
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            drawable.level=value
            if (value==10000){
                drawable.level=0
            }
        }
    }

    private fun initRecyclerView() {
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        watchList = ArrayList()
        watchList?.add(ConnectWatchBean("JIANGTAI疆泰05X", "型号：SW2500", "应用领域：森林消防局", "规模搜救"))
        watchList?.add(ConnectWatchBean("JIANGTAI疆泰05X", "型号：SW2501", "应用领域：森林消防局", "规模搜救"))
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(BaseUtils.dip2px(16)))
    }

    override fun initView() {
        tv_device_name.typeface = Constants.font_normal
        initRecyclerView()
        initConnectState()//初始化连接状态
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        loadingView = LoadingView(this)
        drawable = progress_iv.background as ClipDrawable
        //initProgress()
    }

    private fun initConnectState() {
        //判断蓝牙手表连接成功状态
        if (connectState) {
            showConnectedContainer(true)
        } else {
            showConnectedContainer(false)
        }
    }

    override fun initListener() {
        fl_sync.setOnClickListener {
          //  animator.start()
            startSyncData()
        }
        im_back.setOnClickListener {
            this.finish()
        }
        iv_ok.setOnClickListener {
            //断开连接
            connectState = false
            //关闭自动连接
            autoConnect = false
            RxBus.getInstance().post("disconnect", "")
            dialog_container.visibility = View.GONE
        }
        iv_canel.setOnClickListener {
            dialog_container.visibility = View.GONE
            iv_disconnect.visibility = View.VISIBLE
        }
        //断开连接
        iv_disconnect.setOnClickListener {
            //点击断开蓝牙,直接前端显示断开,不重新连接
            //todo:弹窗提示
            iv_disconnect.visibility = View.GONE
            dialog_container.visibility = View.VISIBLE
        }

        adapter.setOnItemClickListener(object : WatchAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                //点击进入扫描二维码页面
                if (bleManager?.adapter!!.isEnabled) {//蓝牙打开
                    //请求权限
                    jumpToScannerActivity()

                } else {
                    //打开蓝牙
                    val intentOpen = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intentOpen, REQUEST_ENABLE_BLE)
                }
            }
        })


    }

    private fun startSyncData() {
        //点击同步
        if (BleConnectService.isConnecting) {
            Toast.makeText(this, "正在进行同步中，请稍后同步", Toast.LENGTH_LONG).show()
        } else {
            BleConnectService.isConnecting = true
            Toast.makeText(this, "开始同步", Toast.LENGTH_LONG).show()
            RxBus.getInstance().post("sync", SyncDataEvent("sync"))
            loadingView?.setLoadingTitle("同步数据...")
            loadingView?.show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setSyncDataProgress(event: ProgressEvent){
        Logger.e("进度==${event.progress},state=${event.state}")
      //  0 运动数据同步  1 运动数据解析  2 同步解析结束
        when(event.state){
            0->{
                drawable.level=(10000*(event.progress*0.5)).toInt()
            }
            1->{
                drawable.level=(10000*(event.progress*0.5+0.5)).toInt()
            }
            2->{
                drawable.level=0
                showToast("同步完成")
            }
            3->{//3 进程杀死/断开连接销毁进度
                drawable.level=0
                showToast("已断开连接")
            }
        }

    }

    private fun jumpToScannerActivity() {// Manifest.permission.VIBRATE允许访问振动设备
        if (checkPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE))) {
            val intent = Intent(this, ScanQRCodeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        } else {
            requestPermission(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE),
                    PERMISS_REQUEST_CODE
            )
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISS_REQUEST_CODE) {
            val intent = Intent(this, ScanQRCodeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private var loadingView: LoadingView? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                val result = data.getStringExtra("result")
                //showToast("扫描结果为$result")
                Logger.e("result=$result")
                //此处进行蓝牙连接 SW2500,SW2500_D371,E3:0B:AA:DE:D3:71,00010000,6811E7ED,00010000,00000001,00010000,00010000
                if (result!!.split(",")[0] == "SW2500") {//如果为手表设备,扫码成功就保存设备
                    lastDeviceMacAddress = result.split(",")[2]
                    connectedDeviceName = result.split(",")[1]
                    autoConnect = false //扫码成功就不自动连接,等连接成功后再设置为自动成功
                    //下面为连接流程
                    loadingView?.setLoadingTitle("连接中...")
                    loadingView?.show()
                    EventBus.getDefault().post(ScanBleEvent())
                    Logger.e("发送开始扫描的EventBus")
                } else {
                    showToast("请扫描SW2500腕表")
                }

            } else {
                showToast("取消扫描")
            }
        }
    }


    /**
     * 蓝牙连接状态的回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchConnectChanged(event: ConnectEvent) {
        if (event.isConnected) {//已连接
            showConnectedContainer(true)
        } else {//已断开显示UI布局
            showConnectedContainer(false)
            Logger.e("ConnectFragment  收到断开回调")
        }
    }

    /**
     * 切换连接未连接显示状态
     */
    private fun showConnectedContainer(show: Boolean) {
        if (show) {
            ll_watch_connected_container.visibility = View.VISIBLE
            ll_watch_list_container.visibility = View.GONE
            iv_disconnect.visibility = View.VISIBLE
            tv_device_name.text = "$connectedDeviceName"
            //todo 可以点击同步数据
        } else {
            ll_watch_connected_container.visibility = View.GONE
            ll_watch_list_container.visibility = View.VISIBLE
            iv_disconnect.visibility = View.GONE
            //todo 不可点击同步数据
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        watchList?.let {
            it.clear()
        }
        watchList = null

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideDialog(event: HideDialogEvent) {
        loadingView?.dismiss()
    }

}

