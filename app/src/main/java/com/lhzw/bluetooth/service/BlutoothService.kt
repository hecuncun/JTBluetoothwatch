package com.lhzw.bluetooth.service

import android.content.Intent
import android.util.Log
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.BlutoothEvent
import com.lhzw.bluetooth.event.NotificationEvent
import com.lhzw.bluetooth.event.RefreshTargetStepsEvent
import com.lhzw.bluetooth.event.SyncDataEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus


/**
 *
@author：created by xtqb
@description:
@date : 2020/1/7 8:57
 *
 */

class BlutoothService : BaseBlutoothService() {
    private var enablePhone: Boolean by Preference(Constants.TYPE_PHONE, true)
    private var enableMsg: Boolean by Preference(Constants.TYPE_MSG, true)
    private var enableQQ: Boolean by Preference(Constants.TYPE_QQ, true)
    private var enableWx: Boolean by Preference(Constants.TYPE_WX, true)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY;
    }

    //手动更新个人信息
    override fun onPersonalInfoSaveResponse(response: ByteArray?) {
        response(response, Constants.UPDATE_PERSON_INFO_RESPONSE_CODE) {
            Log.e("Watch", "onPersonalInfoSaveResponse .... ")
            Logger.e("个人信息设置到手表成功")
            showToast("保存个人信息成功")
            //刷新首页目标步数
            EventBus.getDefault().post(RefreshTargetStepsEvent())
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("connect")])
    fun connect(event: BlutoothEvent) {
        Logger.e("收到了RxBus  连接手表的指令")
        mContext = event.context
        myBleManager?.let {
            currentAddrss = event.device.address
            it.connect(event.device).retry(3, 100)
                    .useAutoConnect(false)
                    .timeout(10000)
                    .enqueue()
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("sync")])
    fun syncData(event: SyncDataEvent) {
        myBleManager?.connection_update(true)
        Log.e("SyncData", "sync data ...")
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("disconnect")])
    fun disconnect(str: String) {
        Logger.e("收到RxBus断开蓝牙指令")
        myBleManager?.device_disconnect()
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("updatePersonInfo")])
    fun updatePersonInfo(str: String) {
        myBleManager?.personal_info_save(PersonalInfoBean.createBytes())
    }

    private var acceptMsg: Boolean by Preference(Constants.ACCEPT_MSG, false)//同步数据完成后再开始接受通知

    // 向手推送消息
    override fun sendToPhoneData(event: NotificationEvent) {
        val data = ByteArray(214)
        for (i in data.indices) {
            data[i] = 0
        }
        data[0] = 0x0F.toByte()
        when (event.packageName) {
            Constants.CALL, Constants.CALL_COMING, Constants.CALL_CONTACT -> {
                data[1] = 1//1：来电，2：微信，3：QQ，4：短信
                if (!enablePhone) {
                    cycleSendData()
                    return
                }
            }
            Constants.WEIXIN -> {
                data[1] = 2//1：来电，2：微信，3：QQ，4：短信
                if (!enableWx) {
                    cycleSendData()
                    return
                }
            }
            Constants.QQ -> {
                data[1] = 3//1：来电，2：微信，3：QQ，4：短信
                if (!enableQQ) {
                    cycleSendData()
                    return
                }
            }
            Constants.MMS -> {
                data[1] = 4//1：来电，2：微信，3：QQ，4：短信
                if (!enableMsg) {
                    cycleSendData()
                    return
                }
            }
            else -> {
                cycleSendData()
                return
            }
        }
        //获取当前时间
        val timeString = DateUtils.getLongToDateString(System.currentTimeMillis())
        Logger.e("消息时间==>$timeString")
        val digital = BaseUtils.keepDigital(timeString)
        Logger.e("消息时间timeNum==>$digital")
        val date_time = byteArrayOf(digital[0].toByte(), digital[1].toByte(), digital[2].toByte(), digital[3].toByte(), digital[4].toByte(), digital[5].toByte(), digital[6].toByte(), digital[7].toByte(), 'T'.toByte(), digital[8].toByte(), digital[9].toByte(), digital[10].toByte(), digital[11].toByte(), digital[12].toByte(), digital[13].toByte(), 0)
        System.arraycopy(date_time, 0, data, 2, date_time.size)//从原数组date_time 0位置开始 复制 到 date数组的起始位置2  复制长度date_time.size

        //消息标题 最大64字节
        val title = ByteArray(64)
        for (i in title.indices) {
            title[i] = 0
        }

        var title_string = ""
        var title_tmp = ByteArray(0)
        val split = event.tickerText.split(":")
        if (split.isNotEmpty()) {
            title_string = split[0].trim()//来电姓名或号码
            Logger.e("title_string==1${title_string}1")
        }
        try {
            title_string.forEach {

                // Logger.e("${it}是中文==${BaseUtils.isChinese(it)}")
                if (BaseUtils.isChinese(it)) {//是中文
                    title_tmp = title_tmp.plus(it.toString().toByteArray(charset("GBK")))
                } else {//不是中文
                    title_tmp = title_tmp.plus(it.toString().toByteArray(charset("US-ASCII")))
                }
            }
            Logger.e("title_tmp byte[]==${BaseUtils.byte2HexStr(title_tmp)}")
            if (title_tmp.size >= 64) {
                title_tmp[63] = 0
            }

            // title_tmp =  title_string.toByteArray(charset("GBK"))
        } catch (e: java.io.UnsupportedEncodingException) {
            e.printStackTrace()
        }
        Logger.e("title_tmp.size==${title_tmp.size}")
        System.arraycopy(title_tmp, 0, title, 0, if (title_tmp.size > 64) (title.size) else title_tmp.size)
        System.arraycopy(title, 0, data, 18, title.size)

        //来电城市  最大  32字节
        val subtitle = ByteArray(32)
        for (i in subtitle.indices) {
            subtitle[i] = 0
        }
        val subtitle_string = ""
        var subtitle_tmp = ByteArray(0)
        try {
            subtitle_string.forEach {

                if (BaseUtils.isChinese(it)) {//是中文
                    subtitle_tmp = subtitle_tmp.plus(it.toString().toByteArray(charset("GBK")))
                } else {//不是中文
                    subtitle_tmp = subtitle_tmp.plus(it.toString().toByteArray(charset("US-ASCII")))
                }
            }

            if (subtitle_tmp.size >= 32) {
                subtitle_tmp[31] = 0
            }
            // subtitle_tmp =  subtitle_string.toByteArray(charset("GBK"))

        } catch (e: java.io.UnsupportedEncodingException) {
            e.printStackTrace()
        }


        System.arraycopy(subtitle_tmp, 0, subtitle, 0, if (subtitle_tmp.size > 32) (subtitle.size) else subtitle_tmp.size)
        System.arraycopy(subtitle, 0, data, 82, subtitle.size)

        val message = ByteArray(96)
        for (i in message.indices) {
            message[i] = 0
        }

        var message_string = ""//短消息内容  最大96字节
        var message_tmp = ByteArray(0)
        if (split.size > 1) {
            message_string = split[1].trim()
        }
        try {

            message_string.forEach {
                //  Logger.e("${it}是中文==${BaseUtils.isChinese(it)}")
                if (BaseUtils.isChinese(it)) {//是中文
                    message_tmp = message_tmp.plus(it.toString().toByteArray(charset("GBK")))
                } else {//不是中文
                    it.toString()
                    val v = it.toString().toByteArray(charset("US-ASCII"))
                    message_tmp = message_tmp.plus(it.toString().toByteArray(charset("US-ASCII")))
                }
            }
            Logger.e("message_tmp byte[]==${BaseUtils.byte2HexStr(message_tmp)}")
            if (message_tmp.size >= 96) {
                message_tmp[95] = 0
            }

            // message_tmp=  message_string.toByteArray(charset("GBK"))
        } catch (e: java.io.UnsupportedEncodingException) {
            e.printStackTrace()
        }
        System.arraycopy(message_tmp, 0, message, 0, if (message_tmp.size > 96) message.size else message_tmp.size)
        System.arraycopy(message, 0, data, 114, message.size)

        //消息大小  最大4字节
        val message_size = ByteArray(4)
        for (i in message_size.indices) {
            message_size[i] = 0
        }
        val message_size_string = message_string.length.toString()//短消息内容大小
        var message_size_tmp = ByteArray(0)
        try {
            message_size_string.forEach {
                if (BaseUtils.isChinese(it)) {//是中文
                    message_size_tmp = message_size_tmp.plus(it.toString().toByteArray(charset("GBK")))
                } else {//不是中文
                    message_size_tmp = message_size_tmp.plus(it.toString().toByteArray(charset("US-ASCII")))
                }
            }

            if (message_size_tmp.size >= 4) {
                message_size_tmp[3] = 0
            }
            //  message_size_tmp=  message_size_string.toByteArray(charset("GBK"))
        } catch (e: java.io.UnsupportedEncodingException) {
            e.printStackTrace()

        }

        System.arraycopy(message_size_tmp, 0, message_size, 0, if (message_size_tmp.size > 4) (message_size.size) else message_size_tmp.size)
        System.arraycopy(message_size, 0, data, 210, message_size.size)

        myBleManager?.app_short_msg(data)
    }


    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("notification")])
    fun notifyWatchMsg(event: NotificationEvent) {
        Logger.e("BlutoothService收到推送==>package==${event.packageName}")
        if (acceptMsg) {
            listMsg.add(event)
            // 执行发送数据
            if (!isSending) {
                isSending = true
                sendToPhoneData(listMsg[0])
            }
        }
    }

    private fun cycleSendData() {
        listMsg.removeAt(0)
        if (listMsg.size > 0) {
            sendToPhoneData(listMsg[0])
        } else {
            isSending = false
        }
    }

    private var connectState: Boolean by Preference(Constants.CONNECT_STATE, false)

    // 清理数据
    override fun onClear() {
        connectState = false
        mContext = null
        Logger.e("重置connectState=false")
        myBleManager?.device_disconnect()
        acceptMsg = false
    }

    override fun onDestroy() {
        mContext=null
        super.onDestroy()
    }
}