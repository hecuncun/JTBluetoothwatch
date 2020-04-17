package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import android.support.design.bottomnavigation.LabelVisibilityMode
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.FragmentTransaction
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.*
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.ui.fragment.ConnectFragment
import com.lhzw.bluetooth.ui.fragment.HomeFragment
import com.lhzw.bluetooth.ui.fragment.SettingFragment
import com.lhzw.bluetooth.ui.fragment.SportsFragment
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.find


class MainActivity : BaseActivity() {

    private val FRAGMENT_HOME = 0x01
    private val FRAGMENT_SPORTS = 0X02
    private val FRAGMENT_SETTING = 0X03
    private val FRAGMENT_CONNECT = 0X04

    private var mIndex = FRAGMENT_HOME

    private var mHomeFragment: HomeFragment? = null
    private var mSportsFragment: SportsFragment? = null
    private var mSettingFragment: SettingFragment? = null
    private var mConnectFragment: ConnectFragment? = null
    private val PERMISS_REQUEST_CODE = 0x100
    private val PERMISS_REQUEST_CODE_PHONE = 0x101

    private var bleStateChangeReceiver: BleStateChangeReceiver? = null

    private var autoConnect: Boolean by Preference(Constants.AUTO_CONNECT, false)

    override fun useEventBus() = true

    override fun attachLayoutRes(): Int = com.lhzw.bluetooth.R.layout.activity_main

    @SuppressLint("InvalidWakeLockTag")
    override fun initData() {
        if (checkPermissions(arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS))) {
            Logger.e("已获取监听电话短信权限")
        } else {
            requestPermission(arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), PERMISS_REQUEST_CODE_PHONE)
        }
        if (checkPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))) {
            Logger.e("已获取存储权限")
            //未初始化就 先初始化一个用户对象
            LitePal.getDatabase()
            val bean = LitePal.find<PersonalInfoBean>(1)
            if (bean == null) {
                val personalInfoBean = PersonalInfoBean("9", 1, 25, 172, 65, 70, 10000, 1500, 5, 194)
                personalInfoBean.save()
            }
        } else {
            Logger.e("请求存储权限")
            requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISS_REQUEST_CODE)
        }


        //注册蓝牙广播
        bleStateChangeReceiver = BleStateChangeReceiver()
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bleStateChangeReceiver, filter)

        //打开监听通知栏功能
        toggleNotificationListenerService()
        openSetting()
        if (autoConnect && bleManager!!.adapter.isEnabled && !connectState) {
            EventBus.getDefault().post(ScanBleEvent())
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        val pm =  getSystemService(Context.POWER_SERVICE) as PowerManager;
//         wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"")
//         wakeLock?.acquire()
    }

    private var wakeLock: PowerManager.WakeLock? = null
    //==================================================扫描操作START==========
//    private var isScanning = false  //是否正在扫描
//    private val SCAN_DURATION: Long = 30000//扫描时长10s

//    private fun startScan() {
//        Logger.e("开始搜索")
//        val scanner = BluetoothLeScannerCompat.getScanner()
//        val settings = ScanSettings.Builder()
//                .setLegacy(false)
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .setReportDelay(1000)
//                .setUseHardwareBatchingIfSupported(false)
//                .build()
//
//        val filters = mutableListOf<ScanFilter>()//过滤器
//
//
//        // filters.add(ScanFilter.Builder().setServiceUuid(mUUid).build())
//        try {
//            scanner.startScan(filters, settings, scanCallback)
//            isScanning = true
//
//            Handler().postDelayed({
//                if (isScanning) {
//                    stopScan()
//                }
//            }, SCAN_DURATION)
//        } catch (e: Exception) {
//            Log.e("BLE_Error", "scanner already started with given callback ...")
//        }
//    }

//    private fun stopScan() {
//        val scanner = BluetoothLeScannerCompat.getScanner()
//        try {
//            scanner.stopScan(scanCallback)
//            isScanning = false
//            if (autoConnect && !connectState) {//重连还未连接成功
//                Logger.e("未找到蓝牙,继续搜索...")
//                Handler().postDelayed(Runnable {
//                    startAutoScanAndConnect()
//                }, scannerDelayTime)
//                Logger.e("延时==$scannerDelayTime")
//            } else {
//                if (!connectState) {
//                    showToast("腕表蓝牙未开启或处于连接状态")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("BLE_Error", "BT Adapter is not turned ON ...")
//        }
//    }


//    private var loadingView: LoadingView? = null
//    private var lastDeviceMacAddress: String by Preference(Constants.LAST_DEVICE_ADDRESS, "")
//    private val mListValues = mutableListOf<ExtendedBluetoothDevice>()
//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            Logger.e("onScanResult")
//        }
//
//        override fun onBatchScanResults(results: MutableList<ScanResult>) {
//            // Log.e("Tag", "result == null ${results.size}")
//            Logger.e("搜索设备中...")
//            for (result in results) {
//                if (result.device.name != null && result.device.name.contains("SW2500")) {
//                    if (mListValues.size == 0) {
//                        mListValues.add(ExtendedBluetoothDevice(result))
//                    } else {
//                        var hasExist = false
//                        for (device in mListValues) {
//                            if (result.device.address == device.device.address) {
//                                hasExist = true
//                                break
//                            }
//                        }
//                        if (!hasExist) {
//                            mListValues.add(ExtendedBluetoothDevice(result))
//                        }
//                    }
//
//                }
//                Log.e("TAG", "已找到周围腕表设备数量==${mListValues.size}")
//            }
//
//            val lastList = mListValues.filter {
//
//                it.device.address == lastDeviceMacAddress
//            }
//
//            if (lastList.isNotEmpty()) {
//                Logger.e("已找到蓝牙设备")
//                if (loadingView == null) {
//                    loadingView = LoadingView(this@MainActivity)
//                    loadingView?.setLoadingTitle("连接中...")
//                    loadingView?.show()
//                }
//
//                if (!connectState) {
//                    Logger.e("RxBus发送连接请求...")
//                    RxBus.getInstance().post("connect", BlutoothEvent(lastList[0].device, this@MainActivity))
//                }
//            } else {
//                if (!connectState) {//未找到设备  还未连接成功 就发断开指令
//                    Logger.e("未发现上次连接设备,断开重试")
//                    RxBus.getInstance().post("disconnect", "")
//                }
//
//
//            }
//
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            Logger.e("onScanFailed")
//
//        }
//    }

    //   private var connectedDeviceName: String by Preference(Constants.CONNECT_DEVICE_NAME, "")

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchConnectChanged(event: ConnectEvent) {
        if (event.isConnected) {//已连接
            Logger.e("收到同步数据的EVENT")
            //  loadingView?.setLoadingTitle("同步数据中...")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideDialog(event: HideDialogEvent) {
        if (event.success) {
            Logger.e("MainActivity  同步数据成功")
        }

    }

//    //自动扫描并且连接
//    private var autoScanner: BluetoothLeScannerCompat? = null
//    private var scannerDelayTime = 1000L//默认一秒
//    private fun startAutoScanAndConnect() {
//        scannerDelayTime *= 2
//        if (scannerDelayTime > 8000L) {
//            scannerDelayTime = 8000L
//        }
//        if (autoScanner == null) {
//            autoScanner = BluetoothLeScannerCompat.getScanner()
//        }
//        val settings = ScanSettings.Builder()
//                .setLegacy(false)
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .setReportDelay(500)
//                .setUseHardwareBatchingIfSupported(false)
//                .build()
//
//        val filters = mutableListOf<ScanFilter>()//过滤器
//
//        autoScanner?.startScan(filters, settings, scanCallback)
//
//        isScanning = true
//        Handler().postDelayed({
//            try {
//                if (isScanning) {
//                    stopScan()
//                }
//            } catch (e: Exception) {
//                Log.e("Bluetooth", "Bluetooth sync fail ...")
//            }
//        }, SCAN_DURATION)
//    }
    //==================================================扫描操作START==========


    //======================通知相关start=====================================================
    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"

    private fun openSetting() {
        if (!isEnabled()) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        } else {
            Toast.makeText(this, "已开启服务权限", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleNotificationListenerService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(
                ComponentName(this@MainActivity, com.lhzw.bluetooth.service.NotifyService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        pm.setComponentEnabledSetting(
                ComponentName(this@MainActivity, com.lhzw.bluetooth.service.NotifyService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    private fun isEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver,
                ENABLED_NOTIFICATION_LISTENERS)
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    //======================通知相关end=====================================================

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISS_REQUEST_CODE == requestCode) {
            //未初始化就 先初始化一个用户对象
            LitePal.getDatabase()
            val bean = LitePal.find<PersonalInfoBean>(1)
            if (bean == null) {
                val personalInfoBean = PersonalInfoBean("9", 1, 25, 172, 65, 70, 10000, 1500, 5, 194)
                personalInfoBean.save()
            }


        }
    }

    private var state = false
    private var bleManager: BluetoothManager? = null

    //    private var myBleManager: BleManager? = null
    override fun initView() {
//        myBleManager = BleManager(this)
//        myBleManager!!.setGattCallbacks(this)

        bottom_navigation.run {
            // 以前使用 BottomNavigationViewHelper.disableShiftMode(this) 方法来设置底部图标和字体都显示并去掉点击动画
            // 升级到 28.0.0 之后，官方重构了 BottomNavigationView ，目前可以使用 labelVisibilityMode = 1 来替代
            // BottomNavigationViewHelper.disableShiftMode(this)
            /**
             * auto   当item小于等于3是，显示文字，item大于3个默认不显示，选中显示文字
            labeled   始终显示文字
            selected  选中时显示文字
            unlabeled 不显示文字
             */
            labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
            setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        }
        toolbar_title.text = "首页"
        showFragment(mIndex)
        showFragment(mIndex)
        showFragment(mIndex)
        showFragment(mIndex)

        //获取蓝牙状态
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bleManager?.let {
            state = it.adapter.isEnabled//拿到蓝牙状态
            toolbar_right_img.setImageResource(if (state) com.lhzw.bluetooth.R.drawable.icon_ble_open else com.lhzw.bluetooth.R.drawable.icon_ble_close)
        }

    }

    private val REQUEST_ENABLE_BLE = 101

    /**
     * 显示Fragment
     */
    private fun showFragment(index: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        hideFragment(transaction)
        mIndex = index
        toolbar_right_img.visibility = View.GONE
        toolbar_right_tv.visibility = View.GONE
        tv_sync.visibility = View.GONE
        im_back.visibility = View.GONE
        when (index) {
            FRAGMENT_HOME -> {
                toolbar_title.text = getString(com.lhzw.bluetooth.R.string.main_home)
                toolbar_right_img.visibility = View.VISIBLE
                toolbar_right_img.setOnClickListener {
                    // toolbar_right_img.setImageResource(if (state) R.drawable.icon_ble_open else R.drawable.icon_ble_close)
                    //打开,关闭蓝牙
                    if (!bleManager!!.adapter.isEnabled) {
                        val intentOpen = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(intentOpen, REQUEST_ENABLE_BLE)
                    } else {
                        bleManager?.adapter?.disable()
                    }

                }

                if (mHomeFragment == null) {
                    mHomeFragment = HomeFragment.getInstance()
                    transaction.add(com.lhzw.bluetooth.R.id.container, mHomeFragment!!, "home")
                } else {
                    transaction.show(mHomeFragment!!)
                }
            }

            FRAGMENT_SPORTS -> {
                toolbar_title.text = getString(com.lhzw.bluetooth.R.string.main_sport_record)
                if (mSportsFragment == null) {
                    mSportsFragment = SportsFragment.getInstance()
                    transaction.add(com.lhzw.bluetooth.R.id.container, mSportsFragment!!, "sports")
                } else {
                    transaction.show(mSportsFragment!!)
                }
            }

            FRAGMENT_SETTING -> {
                toolbar_title.text = getString(com.lhzw.bluetooth.R.string.main_setting)
                toolbar_right_tv.visibility = View.VISIBLE
                toolbar_right_tv.text = "保存"
                toolbar_right_tv.setTextColor(resources.getColor(com.lhzw.bluetooth.R.color.orange))
                toolbar_right_tv.setOnClickListener {
                    //保存bean
                    EventBus.getDefault().post(SaveUserEvent())
                }
                if (mSettingFragment == null) {
                    mSettingFragment = SettingFragment.getInstance()
                    transaction.add(com.lhzw.bluetooth.R.id.container, mSettingFragment!!, "setting")
                } else {
                    transaction.show(mSettingFragment!!)
                }
            }

            FRAGMENT_CONNECT -> {
                toolbar_title.text = getString(com.lhzw.bluetooth.R.string.main_connect)
                if (mConnectFragment == null) {
                    mConnectFragment = ConnectFragment.getInstance()
                    transaction.add(com.lhzw.bluetooth.R.id.container, mConnectFragment!!, "connect")
                } else {
                    transaction.show(mConnectFragment!!)
                    mConnectFragment?.refleshSyncState()
                }
            }
        }
        transaction.commit()
    }

    /**
     * 隐藏所有fragment
     */
    private fun hideFragment(transaction: FragmentTransaction) {
        mHomeFragment?.let { transaction.hide(it) }
        mSportsFragment?.let { transaction.hide(it) }
        mSettingFragment?.let { transaction.hide(it) }
        mConnectFragment?.let { transaction.hide(it) }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        return@OnNavigationItemSelectedListener when (item.itemId) {
            com.lhzw.bluetooth.R.id.action_home -> {
                showFragment(FRAGMENT_HOME)
                true
            }

            com.lhzw.bluetooth.R.id.action_sports -> {
                showFragment(FRAGMENT_SPORTS)
                true
            }

            com.lhzw.bluetooth.R.id.action_setting -> {
                showFragment(FRAGMENT_SETTING)
                true
            }

            com.lhzw.bluetooth.R.id.action_connect -> {
                showFragment(FRAGMENT_CONNECT)
                true
            }
            else -> {
                false
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bleStateChangeReceiver)
//        wakeLock?.release()
//        releaseWakeLock();
    }

    override fun initListener() {
    }

    private var mExitTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis().minus(mExitTime) <= 2000) {
                finish()
            } else {
                mExitTime = System.currentTimeMillis()
                showToast("再按一次退出程序")
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    //监听蓝牙开关的状态
    private inner class BleStateChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val bleState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                    when (bleState) {
                        BluetoothAdapter.STATE_ON -> {
                            //打开
                            toolbar_right_img.setImageResource(com.lhzw.bluetooth.R.drawable.icon_ble_open)
                            showToast("打开蓝牙")
                            EventBus.getDefault().post(BleStateEvent(true))
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            //关闭
                            toolbar_right_img.setImageResource(com.lhzw.bluetooth.R.drawable.icon_ble_close)
                            showToast("关闭蓝牙")
                            EventBus.getDefault().post(BleStateEvent(false))
                        }
                        else -> {
                        }
                    }
                }
                else -> {
                }
            }
        }
    }
}
