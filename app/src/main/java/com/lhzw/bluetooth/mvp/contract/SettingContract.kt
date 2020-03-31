package com.lhzw.bluetooth.mvp.contract

import com.lhzw.bluetooth.base.IModel
import com.lhzw.bluetooth.base.IPresenter
import com.lhzw.bluetooth.base.IView
import com.lhzw.bluetooth.bean.PersonalInfoBean

/**
 * Created by hecuncun on 2019/11/13
 */
interface SettingContract{

    //传到UI页面
    interface View :IView{
        fun getPersonalInfoSuccess(data :PersonalInfoBean?)
    }


    //Presenter,Model接口的方法名最好保持一致
    interface Presenter :IPresenter<View>{
        fun getPersonalInfo()
    }

    interface Model:IModel{
        fun getPersonalInfo():PersonalInfoBean?
    }
}