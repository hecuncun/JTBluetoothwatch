package com.lhzw.bluetooth.mvp.model

import com.lhzw.bluetooth.base.BaseModel
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.mvp.contract.SettingContract
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by hecuncun on 2019/11/13
 */
class SettingModel : BaseModel(), SettingContract.Model {

    override fun getPersonalInfo(): PersonalInfoBean? {
        val list = LitePal.findAll<PersonalInfoBean>()
        return  list[0]
    }
}