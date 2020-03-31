package com.lhzw.bluetooth.mvp.presenter

import com.lhzw.bluetooth.base.BasePresenter
import com.lhzw.bluetooth.mvp.contract.SettingContract
import com.lhzw.bluetooth.mvp.model.SettingModel

/**
 * Created by hecuncun on 2019/11/13
 */
class SettingPresenter : BasePresenter<SettingContract.Model, SettingContract.View>(), SettingContract.Presenter {

    override fun createModel(): SettingContract.Model? = SettingModel()

    override fun getPersonalInfo() {
        mView?.getPersonalInfoSuccess(mModel?.getPersonalInfo()) // 回传view
    }
}