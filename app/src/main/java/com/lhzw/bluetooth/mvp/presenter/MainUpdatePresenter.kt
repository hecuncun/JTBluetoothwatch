package com.lhzw.bluetooth.mvp.presenter

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.FirmBean
import com.lhzw.bluetooth.mvp.contract.UpdateContract
import com.lhzw.bluetooth.mvp.model.UpdateModel
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.kotlinmvp.presenter.BaseIPresenter

/**
 * Date： 2020/7/6 0006
 * Time： 15:11
 * Created by xtqb.
 */

class MainUpdatePresenter : BaseIPresenter<UpdateContract.IView>(), UpdateContract.IPresenter {
    private var mModel: UpdateContract.IModel? = null
    private val TAG = "MainUpdatePresenter"
    private var firm: FirmBean? = null
    private var apk: ApkBean? = null
    override fun checkUpdate(mContext: Context) {
        // 腕表信息
        val watchInfo = mModel?.queryWatchData()
        if (watchInfo != null && watchInfo.isNotEmpty()) {
            // 说明已连接过手表
            mModel?.getLatestFirm {
                if (it != null) {
                    Log.e(TAG, "${it.getApolloAppVersion()}  ${it.getBleAppVersion()}")
                    var isApolloUpdate = false
                    var isBleUpdate = false
                    firm = null
                    if (it.getApolloAppVersion() > watchInfo[0].BLE_APP_VERSION) {
                        // 说明有新的更新 暂时不支持退版本，仅支持升级
                        isApolloUpdate = true
                        firm = it
                    }

                    if (it.getBleAppVersion() > watchInfo[0].BLE_APP_VERSION) {
                        isBleUpdate = true
                        firm = it
                    }
                    var apolloVersion = ""
                    var bleVersion = ""
                    if (firm == null) {
                        apolloVersion = BaseUtils.apolloOrBleToVersion(watchInfo[0].APOLLO_APP_VERSION)
                        bleVersion = BaseUtils.apolloOrBleToVersion(watchInfo[0].BLE_APP_VERSION)
                    } else {
                        apolloVersion = BaseUtils.apolloOrBleToVersion(it.getApolloAppVersion().toInt())
                        bleVersion = BaseUtils.apolloOrBleToVersion(it.getBleAppVersion().toInt())
                    }
                    mView?.updateFirmState(isApolloUpdate, apolloVersion, isBleUpdate, bleVersion)
                    // 检查Apk
                    checkApkUpdate(mContext) { state, version ->
                        mView?.updateApkState(state, version)
                    }
                } else {
                    Log.e(TAG, "${watchInfo[0].APOLLO_APP_VERSION}    ${watchInfo[0].BLE_APP_VERSION}")
                    mView?.updateFirmState(false, BaseUtils.apolloOrBleToVersion(watchInfo[0].APOLLO_APP_VERSION),
                            false, BaseUtils.apolloOrBleToVersion(watchInfo[0].BLE_APP_VERSION))
                    mView?.updateApkState(false, App.instance
                            .packageManager.getPackageInfo(App.instance.packageName, 0).versionName)
                    Toast.makeText(mContext, "连接超时...", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 检查Apk
            checkApkUpdate(mContext) { state, version ->
                mView?.updateApkState(state, version)
            }
        }
    }

    private fun checkApkUpdate(mContext: Context, body: (isUpdate: Boolean, versionName: String) -> Unit) {
        // apk 版本信息
        var appVersionCode: Long = 0
        var appVersionName = ""
        try {
            val packageInfo = App.instance
                    .packageManager
                    .getPackageInfo(App.instance.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            appVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, e.message)
        }
        mModel?.getLatestApk {
            if (it != null) {
                Log.e(TAG, "${it.getPackageName()}")
                var isApkUpdate = false
                if (appVersionCode < it.getVersionCode()) {
                    isApkUpdate = true
                    apk = it
                }
                body(isApkUpdate, it.getVersionName()!!)
            } else {
//                Toast.makeText(mContext, "无Apk版本可升级", Toast.LENGTH_SHORT).show()
                body(false, appVersionName)
            }
        }
    }

    override fun onAttach() {
        mModel = UpdateModel()
    }


    override fun onDettach() {
        mModel?.onDettach()
        mModel = null
    }

    override fun initWatchUI() {
        // 腕表信息
        val watchInfo = mModel?.queryWatchData()
        if (watchInfo != null && watchInfo.isNotEmpty()) {
            mView?.initWatchUI(BaseUtils.apolloOrBleToVersion(watchInfo[0].APOLLO_APP_VERSION),
                    BaseUtils.apolloOrBleToVersion(watchInfo[0].BLE_APP_VERSION))
        } else {
            mView?.initWatchUI("", "")
        }
    }
}