package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseUpdateActivity
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.mvp.presenter.MainUpdatePresenter
import com.lhzw.bluetooth.widget.LoadingView
import kotlinx.android.synthetic.main.activity_update_func_list.*

/**
 * Date： 2020/6/18 0018
 * Time： 10:42
 * Created by xtqb.
 */

class UpdateFuncActivity : BaseUpdateActivity<MainUpdatePresenter>() {
    private val TAG = UpdateFuncActivity::class.java.simpleName
    private val PERMISS_REQUEST_CODE = 0x0001
    private val UPDATE_WATCH = 0x0015
    private val DOWNLOAD_FIRM = 0x0020
    private var update_type = DOWNLOAD_FIRM
    private var loadingView: LoadingView? = null
    private var isChecking = false
    private val DOWNLOADING = 0
    private val UPDATEFIRM = 1
    private val FREE = 2
    private var state = FREE
    override fun attachLayoutRes() = R.layout.activity_update_func_list

    override fun initData() {
        RxBus.getInstance().register(this)
        // 初始化界面
        tv_app_version.text = "JIANGTAI ${App.instance
                .packageManager.getPackageInfo(App.instance.packageName, 0).versionName}"
        tv_app_update_date.text = apk_update_time
        tv_watch_update_date.text = firm_update_time
        mPresenter?.initWatchUI()
        checkPermission()
    }

    override fun initView() {
        super.initView()
    }


    private fun checkPermission() {
        if (checkPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET))) {
            Log.e(TAG, "已获取存储权限")
            checkVersion()
        } else {
            Log.e(TAG, "请求存储权限")
            requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET), PERMISS_REQUEST_CODE)
        }
    }

    private fun checkVersion() {
        if (isChecking) return
        showLoadingView("检查版本...")
        mPresenter?.checkUpdate(this)
    }

    private fun downloadUpdate() {
        if (!connectState) {
            Toast.makeText(this, "蓝牙腕表已断开连接", Toast.LENGTH_SHORT).show()
            return
        }

        when (update_type) {
            UPDATE_WATCH -> {
                updateWatch()
            }
            DOWNLOAD_FIRM -> {
                state = DOWNLOADING
                tv_update_watch.isEnabled = false
                tv_update_watch.setTextColor(getColor(R.color.gray))
                tv_update_watch_status.text = "下载数据中..."
                startProgress1()
            }
        }
    }

    /**
     * 腕表版本校验  判断是否升级
     */
    private fun updateWatch() {
        progesss_watch.progress = 0
        progesss_watch.max = 100
        showLoadingView("进行数据解压中...")
        RxBus.getInstance().post("reconnet", "")
        Log.e("UPDATEWATCH", "updateWatch ---------  ++++")
    }

    /**
     * App版本校验 判断是否升级
     *
     */
    private fun updateApk() {
        state = DOWNLOADING
        tv_update_app.setTextColor(getColor(R.color.gray))
        tv_update_app_status.text = "下载数据中..."
        tv_update_app.isEnabled = false
        startProgress()
    }

    override fun initListener() {
//        startProgress()

//        startProgress1()

        im_back.setOnClickListener {
            if (!interceptFinish()) {
                this.finish()
            }
        }

        tv_update_app.setOnClickListener {
            updateApk()
        }

        tv_update_watch.setOnClickListener {
            downloadUpdate()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISS_REQUEST_CODE == requestCode) {
            //未初始化就 先初始化一个用户对象
            checkVersion()
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

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("onupdateprogress")])
    fun onUpdateProgress(progress: String) {
        val value = progress.toInt()
        if (value > 0) {
            cancelLoadingView()
        }
        progesss_watch.progress = value
        Log.e("UPDATEWATCH", "onUpdateProgress ---------  ++++")
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("onupdatestatus")])
    fun onUpdateStatus(status: String) {
        tv_update_watch_status.text = status
        Log.e("UPDATEWATCH", "onUpdateStatus ---------  ++++")
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getInstance().unregister(this)
        loadingView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        loadingView = null
    }

    override fun getMainPresent() = MainUpdatePresenter()
    override fun updateApkState(state: Boolean, versionName: String) {
        tv_app_version.text = "JIANGTAI $versionName"
        tv_app_version.setTextColor(getColor(if (state) R.color.blue_light else R.color.gray))
        if (state) {
            tv_app_update_date.visibility = View.GONE
            progesss_app.visibility = View.VISIBLE

            tv_update_app_status.visibility = View.VISIBLE
            tv_update_app_status.text = "等待下载"
            tv_update_app.visibility = View.VISIBLE
        } else {
            tv_app_update_date.visibility = View.VISIBLE
            tv_app_update_date.text = apk_update_time

            progesss_app.visibility = View.GONE
            tv_update_app_status.visibility = View.GONE
            tv_update_app.visibility = View.GONE
        }
        cancelLoadingView()
    }

    override fun updateFirmState(apolloState: Boolean, apolloVersion: String, bleState: Boolean, bleVersion: String) {
        tv_apollo_version.text = "Apollo $apolloVersion"
        tv_ble_version.text = "Ble $bleVersion"
        tv_apollo_version.setTextColor(getColor(if (apolloState) R.color.blue_light else R.color.gray))
        tv_ble_version.setTextColor(getColor(if (bleState) R.color.blue_light else R.color.gray))

        if (apolloState || bleState) {
            tv_update_watch_status.visibility = View.VISIBLE
            tv_update_watch_status.text = "准备下载数据"
            tv_watch_update_date.visibility = View.GONE
            progesss_watch.visibility = View.VISIBLE
            tv_update_watch.visibility = View.VISIBLE
            tv_update_watch.text = "下载固件"
            update_type = DOWNLOAD_FIRM
        } else {
            tv_update_watch_status.visibility = View.GONE
            tv_watch_update_date.visibility = View.VISIBLE
            tv_watch_update_date.text = firm_update_time

            progesss_watch.visibility = View.GONE
            tv_update_watch.visibility = View.GONE
        }
    }

    override fun initWatchUI(apolloVersion: String, bleVersion: String) {
        tv_apollo_version.text = "Apollo $apolloVersion"
        tv_ble_version.text = "Ble $bleVersion"
        if (TextUtils.isEmpty(apolloVersion) && TextUtils.isEmpty(bleVersion)) {
            tv_watch_update_date.text = "无连接"
        } else {
            tv_watch_update_date.text = firm_update_time
        }

    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && interceptFinish()) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun interceptFinish(): Boolean {
        if (state == DOWNLOADING) {
            Toast.makeText(this, "正在下载数据", Toast.LENGTH_SHORT).show()
            return true
        } else if (state == UPDATEFIRM) {
            Toast.makeText(this, "正在升级腕表固件", Toast.LENGTH_SHORT).show()
            return true
        } else if (state == FREE) {
            return false
        }
        return false
    }

    override fun reflesh() {

    }
}