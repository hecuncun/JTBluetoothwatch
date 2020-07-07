package com.lhzw.bluetooth.mvp.contract

import android.content.Context
import com.lhzw.bluetooth.base.BaseIView
import com.lhzw.bluetooth.bean.WatchInfoBean
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.FirmBean

/**
 * Date： 2020/7/6 0006
 * Time： 14:58
 * Created by xtqb.
 */
interface UpdateContract {

    interface IModel {
        fun onDettach()
        fun queryWatchData(): List<WatchInfoBean>?

        fun getLatestApk(body: (apk: ApkBean?) -> Unit)

        fun getLatestFirm(body: (firm: FirmBean?) -> Unit)
    }

    interface IView : BaseIView {
        fun updateApkState(state: Boolean, versionName: String)
        fun updateFirmState(apollo: Boolean, apolloVersionName: String, ble: Boolean, bleVersion: String)
        fun initWatchUI(apolloVersion: String, bleVersion: String)

    }

    interface IPresenter {
        fun checkUpdate(mContext: Context)
        fun onAttach()
        fun onDettach()
        fun initWatchUI()
    }
}
