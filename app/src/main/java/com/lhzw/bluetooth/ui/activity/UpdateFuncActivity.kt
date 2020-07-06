package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.util.Log
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseUpdateActivity
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.mvp.contract.UpdateContract
import com.lhzw.bluetooth.mvp.presenter.MainUpdatePresenter
import com.lhzw.bluetooth.widget.LoadingView
import kotlinx.android.synthetic.main.activity_update_func_list.*

/**
 * Date： 2020/6/18 0018
 * Time： 10:42
 * Created by xtqb.
 */

@Suppress("SpellCheckingInspection")
class UpdateFuncActivity : BaseUpdateActivity<MainUpdatePresenter>(), UpdateContract.IView {
    private val TAG = UpdateFuncActivity::class.java.simpleName
    private val PERMISS_REQUEST_CODE = 0x0001
    private val UPDATE_APP = 0x0010
    private val UPDATE_WATCH = 0x0015
    private var update_type = UPDATE_APP
    private var loadingView: LoadingView? = null
    private var isChecking = false
    override fun attachLayoutRes() = R.layout.activity_update_func_list

    override fun initData() {
        RxBus.getInstance().register(this)
//        checkVersion()
        checkPermission()
    }

    override fun initView() {

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
        mPresenter?.checkUpdate(this)
    }

    private fun update() {
        when (update_type) {
            UPDATE_APP -> {
                updateApk()
            }
            UPDATE_WATCH -> {
                updateWatch()
            }
        }
    }

    /**
     * 腕表版本校验  判断是否升级
     */
    private fun updateWatch() {
        progesss_watch.progress = 0
        progesss_watch.max = 100
//        tv_update_watch_status.text = "进行解压数据..."
        showLoadingView("进行数据解压中...")
        RxBus.getInstance().post("reconnet", "")

        Log.e("UPDATEWATCH", "updateWatch ---------  ++++")
    }

    /**
     * App版本校验 判断是否升级
     *
     */
    private fun updateApk() {

    }

    override fun initListener() {
//        startProgress()

//        startProgress1()

        im_back.setOnClickListener {
            this.finish()
        }

        tv_update_app.setOnClickListener {
            update_type = UPDATE_APP
            checkPermission()
        }

        tv_update_watch.setOnClickListener {
            update_type = UPDATE_WATCH
            checkPermission()
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISS_REQUEST_CODE == requestCode) {
            //未初始化就 先初始化一个用户对象
            update()
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

    }

    override fun updateFirmState(state: Boolean, versionName: String) {

    }

    override fun reflesh() {

    }
}