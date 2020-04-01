package com.lhzw.bluetooth.service

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.lhzw.bluetooth.bean.*
import com.lhzw.bluetooth.ble.*
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.event.ConnectEvent
import com.lhzw.bluetooth.event.HideDialogEvent
import com.lhzw.bluetooth.event.RefreshEvent
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/7 9:07
 *
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
abstract class BaseBlutoothService : Service(), BleManagerCallbacks {
    protected var myBleManager: BleManager? = null
    private var noFlashMap = HashMap<String, MutableList<Byte>>()
    private lateinit var readDailyBean: ReadFlashBean<DailyDataBean>
    private lateinit var readActivityBean: ReadSportAcitvityBean<SportActivityBean>
    private var readSportDetailList: MutableList<ReadDetailFlashBean> = ArrayList()
    private var readSportDetailMap = HashMap<String, HashMap<Int, MutableList<Byte>>>()
    private val DYNAMIC_DATE = 0x01
    private val MTU_DELAY = 0x02
    protected var currentAddrss = ""
    protected var lastDeviceMacAddress: String by Preference(Constants.LAST_DEVICE_ADDRESS, "")

    override fun onCreate() {
        super.onCreate()
        RxBus.getInstance().register(this)
        myBleManager = BleManager(this)
        myBleManager?.setGattCallbacks(this)
        Log.e("Tag", "start service ...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("Tag", "start onStartCommand ...")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onActivityAddressRequestResponse(response: ByteArray?) {
        Log.e("Tag", "parserBoundaryAdrr   ${BaseUtils.byte2HexStr(response!!)}")
        val list = CommOperation.query(BoundaryAdrrBean::class.java)
        if (list.isEmpty()) {
            BoundaryAdrrBean.parserBoundaryAdrr(response)
        }
        // 读取活动数据
        val activities = CommOperation.query(SportActivityBean::class.java).filter {
            it.current_activity_num > 0
        }
        var isOver = true
        var request_date: ByteArray = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        var request_mark: Byte = 0x00
        if (activities.isNotEmpty()) {
            isOver = false
            request_date = BaseUtils.longToByteArray(activities[0].request_date).toByteArray().copyOfRange(0, 6)
            request_mark = activities[0].current_activity_mark.toByte()

            readActivityBean = ReadSportAcitvityBean(
                    0x0D,
                    activities,
                    request_date,
                    request_mark,
                    0,
                    0,   // 默认值是什么? 0 : 1
                    isOver)
            readSportActivities()
        } else {
            // 没有活动数据
            Log.e("Tag", "no sport data ...")
            // 设置手表蓝牙为低功耗
            myBleManager?.settinng_connect_parameter(false)
        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        Log.e("Watch", "onDeviceDisconnected .... ")
        EventBus.getDefault().post(ConnectEvent(false))
    }

    override fun onAppShortMsgResponse(response: ByteArray?) {
        Logger.d("onAppShortMsgResponse")
    }

    // 连接成功
    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.e("Watch", "onDeviceConnected .... ")
        if(currentAddrss.isNotEmpty() && !currentAddrss.equals(lastDeviceMacAddress)){
//            CommOperation.deleteAll(WatchInfoBean::class.java)
//            CommOperation.deleteAll(SportInfoBean::class.java)
//            CommOperation.deleteAll(BoundaryAdrrBean::class.java)
//            CommOperation.deleteAll(ClimbingSportBean::class.java)
//            CommOperation.deleteAll(CurrentDataBean::class.java)
//            CommOperation.deleteAll(DailyDataBean::class.java)
//            CommOperation.deleteAll(DailyInfoDataBean::class.java)
//            CommOperation.deleteAll(FlatSportBean::class.java)
//            CommOperation.deleteAll(SportActivityBean::class.java)
//            CommOperation.deleteAll(SportInfoAddrBean::class.java)
        }
        EventBus.getDefault().post(ConnectEvent(true))
        //1.连接成功  特征使能
        // myBleManager?.notification_enable()
//        Observable.just(true).delay(3, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe {
//            //2.更新连接参数
//            Log.e("Tag", "update ....")
//            myBleManager?.connection_update(it)
//        }
//        Handler().postDelayed(Runnable {
//            Log.e("Tag", "update ....")
//            myBleManager?.connection_update(true)
//         //   myBleManager?.mtu_update()
//        },500)


        /*
         //3.更新mtu大小
         myBleManager?.mtu_update()
         //4.更新手表时间
         myBleManager?.watch_time_update()
         //5.读取手表信息
         myBleManager?.device_info()
         //6.更新用户个人信息  TODO:传值更新个人信息
         myBleManager?.personal_info_update()
         //7.同步日常数据onDailyDataReqest 读取日常数据,判断运动天数是否大于0,大于0再去读NorFlash(日常数据地址  日常数据大小 )
         myBleManager?.daily_data_request()

         //8.发送更新蓝牙连接参数命令，将蓝牙连接间隙修改为慢速连接,
         // 注意：如果连接流程中有短消息需要推送，将短消息推迟到整个连接流程结束再推送给手表。
         myBleManager?.connection_update(false)
         Log.e("Tag", "connect success ...")
         */
    }

    override fun onBondingRequired(device: BluetoothDevice) {

    }

    override fun onDeviceNotSupported(device: BluetoothDevice) {

    }

    override fun onPersonalInfoReadResponse(response: ByteArray?) {

    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {

    }

    override fun onLinkLossOccurred(device: BluetoothDevice) {

    }

    override fun onBonded(device: BluetoothDevice) {

    }

    override fun onSettingConnectParameter(response: ByteArray?) {
        // 设置手表为低功率状态
        Log.e("Watch", "onSettingConnectParameter   ${BaseUtils.byte2HexStr(response!!)} ....")
        response(response, Constants.CONNECT_RESPONSE_CODE) {
            //开始连接进入进度条,连接并初始化成功后再发成功
            EventBus.getDefault().post(HideDialogEvent())
            // 刷新界面
            RxBus.getInstance().post("reflesh", "")
        }
    }

    override fun onMtuUpdateResponse(response: ByteArray?) {
        Log.e("Watch", "onMtuUpdateResponse   ${BaseUtils.byte2HexStr(response!!)} ....")
        mHandler.removeMessages(MTU_DELAY)
        response(response, Constants.MTU_RESPONSE_CODE) {
            myBleManager?.watch_time_update()
//            myBleManager?.device_info()
        }
    }

    // 蓝牙本页入口
    override fun onConnectionUpdateResponse(response: ByteArray?) {
        //更新连接成功后    更新mtu
//        Log.e("Tag", "onConnectionUpdateResponse ...  $response")
        Log.e("Watch", "onConnectionUpdateResponse .... ${BaseUtils.byte2HexStr(response!!)}")
        response(response, Constants.CONNECT_RESPONSE_CODE) {
//            myBleManager?.mtu_update()
            mHandler.sendEmptyMessage(MTU_DELAY)
        }
    }

    override fun onDeviceInfoResponse(response: ByteArray?) {
//        Log.e("Tag", "onDeviceInfoResponse ...  ${BaseUtils.byte2HexStr(response!!)}")
        Log.e("Watch", "onDeviceInfoResponse .... ${BaseUtils.byte2HexStr(response!!)}")
        var bean = WatchInfoBean.createBean(response)
        bean?.let {
            var list = CommOperation.query(WatchInfoBean::class.java)
            list?.let {
                CommOperation.deleteAll(WatchInfoBean::class.java)
            }
            CommOperation.insert(it)
        }
        myBleManager?.personal_info_update(PersonalInfoBean.createBytes())  // 更新个人数到手表
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {

    }

    override fun onDeviceReady(device: BluetoothDevice) {

    }

    override fun onWatchTimeUpdateResponse(response: ByteArray?) {
        Log.e("Watch", "onWatchTimeUpdateResponse .... ${BaseUtils.byte2HexStr(response!!)}")
        response(response, Constants.UPDATE_TIME_RESPONSE_CODE) {
            myBleManager?.device_info()
        }
    }

    override fun onPersonalInfoUpdateResponse(response: ByteArray?) {
//        Log.e("Tag", "onPersonalInfoUpdateResponse ...  ${BaseUtils.byte2HexStr(response!!)}")
        Log.e("Watch", "onPersonalInfoUpdateResponse .... ${BaseUtils.byte2HexStr(response!!)}")
        //更新个人信息成功就开始同步动态数据
        response(response, Constants.UPDATE_PERSON_INFO_RESPONSE_CODE) {
//            myBleManager?.current_data_update()
            mHandler.sendEmptyMessageDelayed(DYNAMIC_DATE, 1000)
        }
    }

    // 获取活动数据
    override fun onSportsParamReadResponse(response: ByteArray?, ID: String) {
        Log.e("Tag", "onSportsParamReadResponse ...   ${BaseUtils.byte2HexStr(response!!)}")
        //解析当前活动
        SportInfoAddrBean.parserSportInfoAddr(response, ID) { data, mark ->
            Log.e("mark", "mark = $mark")
            readActivityBean.request_date = data
            readActivityBean.request_mark = mark
            readNextActivity()
        }
    }

    override fun onSportsParamUpdateResponse(response: ByteArray?) {

    }

    override fun onDailyDataRequestResponse(response: ByteArray?) {
        Log.e("Tag", "onDailyDataRequestResponse ...  ${BaseUtils.byte2HexStr(response!!)}")
        response?.let {
            if(response[0].toInt() == 0x0C){
                DailyDataBean.parserDailyData(response) { datas ->
                    if (datas.size > 0) {
                        noFlashMap.clear()
                    }
                    // 从noFlash获取数据信息
                    var len = Constants.MTU_MAX
                    var isOver = false
                    if (datas[0].data_len < Constants.MTU_MAX) {
                        len = datas[0].data_len
                        if (datas.size == 1) {
                            isOver = true
                        }
                    }
                    readDailyBean = ReadFlashBean(datas, 0, 0x0C, datas[0].start_addr, len, 1, isOver)
                    readDailyNoFlash()
                }
            }
        }
    }

    override fun onSportDetailInfoResponse(response: ByteArray?, request_code: Byte, type: Int, ID: String) {
//        Log.e("SportDetail", "readSportDetailData  type : $type  ... ${BaseUtils.byte2HexStr(response!!)}")
        if (request_code.toInt() == 0x0D) {
            response?.let {
                if (readSportDetailMap.get(ID) == null) {
                    val detail = HashMap<Int, MutableList<Byte>>()
                    readSportDetailMap.put(ID, detail)
                }
                val map = readSportDetailMap.get(ID)
                if (map!!.get(type) == null) {
                    val list = ArrayList<Byte>()
                    val tmp = it.toList()
                    list.addAll(tmp.subList(11, tmp.size))
                    map.put(type, list)
                    Log.e("sportdetail", "type $type  ${BaseUtils.byte2HexStr(tmp.subList(11, tmp.size).toByteArray())}")
                } else {
                    val tmp = it.toList()
                    map.get(type)?.addAll(tmp.subList(11, tmp.size))
                }
                readSportDetailBean()
            }
        }
    }

    override fun onNorFlashEraseResponse(response: ByteArray?) {

    }

    override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {

    }

    override fun onBondingFailed(device: BluetoothDevice) {

    }

    override fun onNorFlashReadResponse(response: ByteArray?, ID: String) {
//        Log.e("Tag", "onNorFlashReadResponse ...  ${BaseUtils.byte2HexStr(response!!)}   ${response.size}")
        response?.let {
            if(response[0].toInt() == 0x04 && response[1].toInt() == 0x0C){
                Log.e("dailyinfo", "$ID : ${BaseUtils.byte2HexStr(response)}")
                if (noFlashMap.get(ID) == null) {
                    val list = ArrayList<Byte>()
                    val tmp = it.toList()
                    list.addAll(tmp.subList(11, tmp.size))
                    noFlashMap.put(ID, list)
                } else {
                    val tmp = it.toList()
                    noFlashMap.get(ID)!!.addAll(tmp.subList(11, tmp.size))
                    if (noFlashMap.get(ID)!!.size == 836) {
                        Log.e("result", "$ID : ${BaseUtils.byte2HexStr(noFlashMap.get(ID)!!.toByteArray())}")
                    }
                }
                if (readDailyBean.isOver) {
                    DailyInfoDataBean.parserDailyInfoBean(noFlashMap) {
                        Log.e("Tag", "read boundary addr ...")
                        myBleManager?.read_boundary_address()
                    }
                } else {
                    readNextAddr()
                }
            }
        }
    }

    // 读取下一个地址数据
    private fun readNextAddr() {
        when (readDailyBean.request_code) {
            Constants.UPDATE_DAILY_INFO_RESPONSE_CODE -> {
                val bean = readDailyBean.list[readDailyBean.index]
                if (bean.data_len > (readDailyBean.counter_mtu + 1) * Constants.MTU_MAX) {
                    readDailyBean.counter_mtu++
                    readDailyBean.current_addr += Constants.MTU_MAX
                    readDailyBean.read_len = Constants.MTU_MAX
                    readDailyNoFlash()
                } else {
                    readDailyBean.current_addr += Constants.MTU_MAX
                    readDailyBean.read_len = bean.data_len - readDailyBean.counter_mtu * Constants.MTU_MAX
                    readDailyNoFlash()
                    if (readDailyBean.list.size == readDailyBean.index + 1) {
                        readDailyBean.isOver = true
                    } else {
                        readDailyBean.index++
                        readDailyBean.current_addr = readDailyBean.list[readDailyBean.index].start_addr - Constants.MTU_MAX
                        readDailyBean.counter_mtu = 0
                        readDailyBean.isOver = false
                    }
                }
            }
        }
    }

    override fun onNorFlashWriteResponse(response: ByteArray?) {

    }

    override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
        Log.e("Bluetooth", "${device}    ${message}     ${errorCode}")
    }

    override fun onWatchDataResponse(response: ByteArray?) {
        // 获取数据

    }

    override fun onCurrentDataUpdate(response: ByteArray?) {
        mHandler.removeMessages(DYNAMIC_DATE)
        //  解析当前数据
        response?.let {
            Log.e("currentData", "currentdata  ${BaseUtils.byte2HexStr(response)}")
            if (it[0] == Constants.CURRENT_DATA_UPDATE_RESPONSE_CODE) {
                val bean = CurrentDataBean.createBean(response)
                bean?.let {
                    val list = CommOperation.query(CurrentDataBean::class.java)
                    list.let {
                        CommOperation.deleteAll(CurrentDataBean::class.java)
                    }
                    CommOperation.insert(it)
                }
                //保存到数据库,并刷新页面
                Logger.e("当前步数==${bean?.dailyStepNumTotal}")
                EventBus.getDefault().post(RefreshEvent(Constants.TYPE_CURRENT_DATA))
                //动态数据后,TODO 请求日常数据
                myBleManager?.daily_data_request()
            }
        }
    }

    // 读取noflash中日常数据
    private fun readDailyNoFlash() {
        var bean = readDailyBean.list[readDailyBean.index]
        val content = ArrayList<Byte>()
        content.add(0x04)
        content.add(bean.response.toByte())
        content.addAll(BaseUtils.intToByteArray(readDailyBean.current_addr))
        content.add((readDailyBean.read_len and 0xff).toByte())
        Log.e("Daily", "${bean.sport_date}  ${BaseUtils.byte2HexStr(content.toByteArray())}")
        myBleManager?.norFlash_read(content.toByteArray(), bean.sport_date)
        content.clear()
    }

    /**
     * 读取日常活动数据
     */
    private fun readSportActivities() {
        if (readActivityBean.isOver) {
//            Log.e("Tag", "read sport activity over ...")
            val bean = readActivityBean.list[readActivityBean.bean_index]
            val list = CommOperation.query(SportActivityBean::class.java, "daily_date", bean.daily_date)
            var value = ContentValues()
            value.put("request_date", BaseUtils.byteToLong(readActivityBean.request_date.toList()))
            value.put("current_activity_mark", readActivityBean.request_mark.toInt())
            CommOperation.update(SportActivityBean::class.java, value, list[0].id)
            init_sport_detail_addr()
        } else {
            val bean = readActivityBean.list[readActivityBean.bean_index]
            val activities_addr = BaseUtils.intToByteArray(bean.activities_addr)
            val content = ArrayList<Byte>()
            content.add(readActivityBean.request_code)
            content.addAll(readActivityBean.request_date.toList())
            content.add(readActivityBean.request_mark)
            content.addAll(activities_addr)
            Log.e("Tag", "readSportActivities : $bean.daily_date  ${BaseUtils.byte2HexStr(content.toByteArray())}")
            myBleManager?.sports_param_read(content.toByteArray(), bean.daily_date + "-" + BaseUtils.byte2HexStr(byteArrayOf(readActivityBean.request_mark)))
            content.clear()
        }
    }

    /**
     *
     * 读取下一个活动  判断 bean_index activity_index
     *
     */
    private fun readNextActivity() {
        val bean = readActivityBean.list[readActivityBean.bean_index]
        if (bean.current_activity_num == readActivityBean.activity_index + 1) {
            if (readActivityBean.list.size == readActivityBean.bean_index + 1) {
                readActivityBean.isOver = true
            } else {
                // 更新bean
                val list = CommOperation.query(SportActivityBean::class.java, "daily_date", bean.daily_date)
//                list[0].request_date = BaseUtils.byteToLong(readActivityBean.request_date.toList())
//                list[0].current_activity_mark = readActivityBean.request_mark.toInt()
                var value = ContentValues()
                value.put("request_date", BaseUtils.byteToLong(readActivityBean.request_date.toList()))
                value.put("current_activity_mark", readActivityBean.request_mark.toInt())
                CommOperation.update(SportActivityBean::class.java, value, list[0].id)
                // 请求下一个bean 数据
                readActivityBean.bean_index++
                readActivityBean.activity_index = 0
                readActivityBean.request_date = BaseUtils.longToByteArray(readActivityBean.list[readActivityBean.bean_index].request_date).toByteArray().copyOfRange(0, 6)
                readActivityBean.request_mark = readActivityBean.list[readActivityBean.bean_index].current_activity_mark.toByte()
            }
        } else {
            readActivityBean.activity_index++
        }
        readSportActivities()
    }

    // 初始化读取的地址信息
    private fun init_sport_detail_addr() {
        if (readSportDetailList.isNotEmpty()) {
            readSportDetailList.clear()
        }
        // 根据地址读取数据
        val activities = CommOperation.query(SportInfoAddrBean::class.java)
        val boundaries = CommOperation.query(BoundaryAdrrBean::class.java)
        activities?.forEach {
            for (type in 0 until Constants.TOTAL) {
                Log.e("SportDetail", "${it.sport_detail_mark}   $type")
                when (type) {
                    Constants.STEP -> {
                        if (it.step_start_addr > it.step_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.STEP, it.step_start_addr, boundaries[0].record_steps_end)
                            addDetailBean(it.sport_detail_mark, Constants.STEP, boundaries[0].record_steps_start, it.step_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.STEP, it.step_start_addr, it.step_end_addr)
                        }
                    }
                    Constants.HEART_RATE -> {
                        if (it.heart_rate_start_addr > it.heart_rate_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.HEART_RATE, it.heart_rate_start_addr, boundaries[0].heart_rate_end)
                            addDetailBean(it.sport_detail_mark, Constants.HEART_RATE, boundaries[0].heart_rate_start, it.heart_rate_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.HEART_RATE, it.heart_rate_start_addr, it.heart_rate_end_addr)
                        }
                    }
                    Constants.AIR_PRESSURE -> {
                        if (it.air_pressure_start_addr > it.air_pressure_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.AIR_PRESSURE, it.air_pressure_start_addr, boundaries[0].air_pressure_end)
                            addDetailBean(it.sport_detail_mark, Constants.AIR_PRESSURE, boundaries[0].air_pressure_start, it.air_pressure_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.AIR_PRESSURE, it.air_pressure_start_addr, it.air_pressure_end_addr)
                        }
                    }
                    Constants.GPS -> {
                        if (it.gps_start_addr > it.gps_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.GPS, it.gps_start_addr, boundaries[0].gps_end)
                            addDetailBean(it.sport_detail_mark, Constants.GPS, boundaries[0].gps_start, it.gps_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.GPS, it.gps_start_addr, it.gps_end_addr)
                        }
                    }
                    Constants.DISTANCE -> {
                        if (it.distance_start_addr > it.distance_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.DISTANCE, it.distance_start_addr, boundaries[0].distance_end)
                            addDetailBean(it.sport_detail_mark, Constants.DISTANCE, boundaries[0].distance_start, it.distance_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.DISTANCE, it.distance_start_addr, it.distance_end_addr)
                        }
                    }
                    Constants.CALORIE -> {
                        if (it.calorie_start_addr > it.calorie_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.CALORIE, it.calorie_start_addr, boundaries[0].calorie_end)
                            addDetailBean(it.sport_detail_mark, Constants.CALORIE, boundaries[0].calorie_start, it.calorie_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.CALORIE, it.calorie_start_addr, it.calorie_end_addr)
                        }
                    }
                    Constants.SPEED -> {
                        if (it.speed_start_addr > it.speed_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.SPEED, it.speed_start_addr, boundaries[0].speed_end)
                            addDetailBean(it.sport_detail_mark, Constants.SPEED, boundaries[0].speed_start, it.speed_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.SPEED, it.speed_start_addr, it.speed_end_addr)
                        }
                    }
                }
            }
        }
//        readSportDetailList?.forEach {
//            Log.e("SportDetail", "${it.sport_detail_mark}   ${it.data_type}  ${it.request_code}  ${it.read_len}")
//        }
        readSportDetailBean()
    }

    private fun readSportDetailBean() {
        if (readSportDetailList.size > 0) {
            val bean = readSportDetailList[readSportDetailList.size - 1]
            var content = ArrayList<Byte>()
            content.add(0X04)
            content.add(0x0D)
            content.addAll(BaseUtils.intToByteArray(bean.current_addr))
            var len = 0;
            if (bean.read_len > Constants.MTU_MAX) {
                len = Constants.MTU_MAX
                bean.current_addr += Constants.MTU_MAX
                bean.read_len -= Constants.MTU_MAX
            } else {
                len = bean.read_len
                readSportDetailList.removeAt(readSportDetailList.size - 1)
            }
            content.add((len and 0xFF).toByte())
//            Log.e("SportDetail", "sendCMD:  ${BaseUtils.byte2HexStr(content.toByteArray())}")
            myBleManager?.sport_detail_info_request(content.toByteArray(), 0x0D, bean.data_type, bean.sport_detail_mark)
        } else {
            Log.e("Tag", "parser sport detail addr over ...")
            SportDetailInfobean.parserSportDetailInfo(readSportDetailMap)
            readSportDetailMap.clear()

            // 设置手表蓝牙为低功耗
            myBleManager?.settinng_connect_parameter(false)
        }
    }

    private fun addDetailBean(mark: String, type: Int, start_addr: Int, end_addr: Int) {
        if (end_addr - start_addr > 0) {
            val bean = ReadDetailFlashBean(0x0D, mark, type, start_addr, end_addr - start_addr)
            readSportDetailList.add(bean)
        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // 杀死线程清理数据
        try {
            RxBus.getInstance().unregister(this)
            onClear()
        } catch (e: Exception) {
            Log.e("Bluetooth", e.message)
        }

    }

    protected fun response(bytes: ByteArray?, RESPONSE: Byte, body: () -> Unit) {
        bytes?.let {
            if (it[0].toByte() == RESPONSE) {
                if (it[1].toInt() == 0) {
                    body()
                }
            }
        }
    }

    private var mHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                DYNAMIC_DATE -> {
                    Log.e("Tag", "retry send dynamic data update ...")
                    myBleManager?.current_data_update()
                    sendEmptyMessageDelayed(DYNAMIC_DATE, 1000)
                }
                MTU_DELAY ->{
                    Log.e("Watch", "retry send mtu data update ...")
                    myBleManager?.mtu_update()
                    sendEmptyMessageDelayed(DYNAMIC_DATE, 2000)
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Tag", "start service onDestroy ...")
        RxBus.getInstance().unregister(this)
        onClear()
    }

    // 清理线程清理数据接口
    open abstract fun onClear()
}