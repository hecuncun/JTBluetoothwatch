package com.lhzw.bluetooth.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.telephony.SmsMessage
import android.text.TextUtils
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.NotificationEvent
import com.lhzw.bluetooth.uitls.PhoneUtil
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.Logger
import me.jessyan.autosize.utils.LogUtils

/**
 * Created by heCunCun on 2020/4/7
 * 来电  短信  服务信息通知
 */
class SmsAndPhoneService : Service() {
    private var connectState: Boolean by Preference(Constants.CONNECT_STATE, false)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Logger.e("SmsAndPhoneService ====onCreate")
        val localIntentFilter = IntentFilter()
        localIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        localIntentFilter.addAction("android.intent.action.PHONE_STATE")
        registerReceiver(NLServerReceiver, localIntentFilter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        LogUtils.e("SmsAndPhoneService--------onStartCommand--------->")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        LogUtils.e("SmsAndPhoneService----onDestroy----unregisterReceiver--------->$NLServerReceiver")
        unregisterReceiver(NLServerReceiver)
        super.onDestroy()
    }

    private val NLServerReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(paramContext: Context, paramIntent: Intent) {
            val action = paramIntent.action ?: return
            if (action == "android.provider.Telephony.SMS_RECEIVED") {
                var str = ""
                //---接收传入的消息---
                val bundle = paramIntent.extras
                var msgs: Array<SmsMessage?>? = null
                if (bundle != null) { //---查询到达的消息---
                    val pdus = bundle["pdus"] as Array<Any>
                    msgs = arrayOfNulls(pdus.size)
                    for (i in msgs.indices) {
                        msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        if (i == 0) { //---获取发送者手机号---
                            str += msgs[i]?.getOriginatingAddress()
                            str += ":"
                        }
                        //---获取消息内容---
                        str += msgs[i]?.getMessageBody().toString()
                    }
                    //---显示SMS消息---
                    LogUtils.e("收到短息==>$str")
                    if (connectState){
                        RxBus.getInstance().post("notification", NotificationEvent(100,str,Constants.MMS))
                    }
                }
            }
            if (action == "android.intent.action.PHONE_STATE") {
                val state = paramIntent.getStringExtra("state")
                val incoming_number = paramIntent.getStringExtra("incoming_number")
                LogUtils.e("state==$state----------incoming_number==$incoming_number")
                if ("RINGING" == state) {
                    if (!TextUtils.isEmpty(incoming_number)) {
                        var phoneNumber = "" // 剔除号码中的分隔符
                        var name = ""
                        try {
                            phoneNumber = incoming_number.replace("-", "").replace(" ", "")
                            name = PhoneUtil.getContactNameByPhoneNumber(paramContext, phoneNumber)
                            Logger.e("收到来电phoneNumber==$phoneNumber===name==$name")
                            if (connectState){
                                RxBus.getInstance().post("notification", NotificationEvent(101,name+phoneNumber,Constants.CALL))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                if ("IDLE" == state) { //挂断
                }
                return
            }
        }
    }
}