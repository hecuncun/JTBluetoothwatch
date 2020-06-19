package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.os.Environment
import android.util.Log
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.dfu.DfuConfigCallBack
import com.lhzw.bluetooth.dfu.DfuUtils
import kotlinx.android.synthetic.main.activity_update_func_list.*

/**
 * Date： 2020/6/18 0018
 * Time： 10:42
 * Created by xtqb.
 */

class UpdateFuncActivity : BaseActivity(), DfuConfigCallBack {
    // duf 文件路径
    private val DFU_PATH by lazy {
        Environment.getExternalStorageDirectory().toString() + "/dfu_file"
    }
    private val TAG = UpdateFuncActivity::class.java.simpleName
    private val PERMISS_REQUEST_CODE = 0x0001
    private val UPDATE_APP = 0x0010
    private val UPDATE_WATCH = 0x0015
    private var update_type = UPDATE_APP
    override fun attachLayoutRes() = R.layout.activity_update_func_list

    override fun initData() {

    }

    override fun initView() {

    }

    private fun checkPermission() {
        if (checkPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET))) {
            Log.e(TAG, "已获取存储权限")
            update()
        } else {
            Log.e(TAG, "请求存储权限")
            requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET), PERMISS_REQUEST_CODE)
        }
    }

    private fun checkVersion() {
        // app


        // 腕表


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
        val duf = DfuUtils(this, "${DFU_PATH}/dfu.zip", DFU_PATH)

        Log.e(TAG, "${duf.getApolloBinSize()}  ${duf.getApolloBinPath()}\n " +
                "${duf.getApolloDatSize()}   ${duf.getApolloBootSettingPath()}\n" +
                "${duf.getNrf52BinSize()}   ${duf.getNrf52BinPath()}\n" +
                "${duf.getNrf52DatSize()}   ${duf.getNrf52BootSettingPath()}")
    }

    /**
     * App版本校验 判断是否升级
     *
     */
    private fun updateApk() {

    }

    override fun initListener() {
        startProgress()

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

    override fun onDfuConfigCallback(response: String) {
        Log.e(TAG, "解压成功 ---------------------------------------")
    }
}