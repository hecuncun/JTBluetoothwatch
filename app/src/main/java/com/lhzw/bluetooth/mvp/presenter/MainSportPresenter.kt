package com.lhzw.bluetooth.mvp.presenter

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.maps.*
import com.amap.api.maps.model.LatLng
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.ClimbingSportBean
import com.lhzw.bluetooth.bean.FlatSportBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.mvp.model.SportModel
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.LocationUtils
import com.lhzw.bluetooth.view.ShareShareDialog
import com.lhzw.kotlinmvp.presenter.BaseSportPresenter
import kotlinx.android.synthetic.main.activity_sport_info.*
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList


/**
 *
@author：created by xtqb
@description: 实现处理model数据 刷新View界面
@date : 2019/11/12 10:28
 *
 */

class MainSportPresenter(var mark: String, var duration: String, val type: Int) : BaseSportPresenter<SportConstract.View>(), SportConstract.Presenter, LocationUtils.ILocationCallBack, LocationSource {
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var aMap: AMap? = null
    private var locationUtils: LocationUtils? = null
    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener?) {
        mListener = onLocationChangedListener;
        locationUtils?.startLocate()
    }

    override fun deactivate() {
        mListener = null
    }

    override fun callBack(str: String, lat: Double, lgt: Double, aMapLocation: AMapLocation) {
        //根据获取的经纬度，将地图移动到定位位置
        aMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lgt), Constants.ZOOM))
//            //添加定位图标
//            addMarker(locationUtils?.getMarkerOption(str, lat, lgt));
//            Log.e("LatLon", "lat  $lat   lgt   $lgt")

        }
        mListener?.onLocationChanged(aMapLocation)
    }

    override fun requirePermission(activity: Activity): Boolean {
        try {
            var needPermissions = arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Constants.BACK_LOCATION_PERMISSION
            )
            if (Build.VERSION.SDK_INT >= 23) {
                return model.checkPermissions(activity, needPermissions)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    //初始化地图
    override fun initMap(mMapView: MapView?): AMap? {
        aMap = model.initMap(mMapView)
        // 定位蓝点
        aMap?.apply {
            mapType = AMap.MAP_TYPE_SATELLITE// 矢量地图模式
//            uiSettings.isZoomControlsEnabled = false//隐藏放大缩小按钮
//            uiSettings.isScrollGesturesEnabled = false
//            uiSettings.isZoomGesturesEnabled = false
//            uiSettings.isRotateGesturesEnabled = false
//            uiSettings.isTiltGesturesEnabled = false
            uiSettings.setAllGesturesEnabled(false)
            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(Constants.LAT, Constants.LGT), Constants.ZOOM))
            locationUtils = LocationUtils()
            locationUtils?.setLocationCallBack(this@MainSportPresenter)
            //设置定位监听
            setLocationSource(this@MainSportPresenter);
            //设置缩放级别
            moveCamera(CameraUpdateFactory.zoomTo(Constants.ZOOM))
            //显示定位层并可触发，默认false
            isMyLocationEnabled = true
            Log.e("Tag", "drawPaths ....")
            // 绘制路径
            drawPaths()
        }
        return aMap!!
    }

    override fun getSha1(): String? {
        try {
            val info = App.context.packageManager
                    .getPackageInfo(App.context.packageName,
                            PackageManager.GET_SIGNATURES)
            val cert = info.signatures[0].toByteArray()
            val md = MessageDigest.getInstance("SHA1")
            val publicKey = md.digest(cert)
            val hexString = StringBuffer()
            for (i in publicKey.indices) {
                val appendString = Integer.toHexString(0xFF and publicKey[i].toInt())
                        .toUpperCase(Locale.US)
                if (appendString.length == 1)
                    hexString.append("0")
                hexString.append(appendString)
            }
            return hexString.toString()
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return null
    }


    // 初始化图表
    override fun initChart(activity: Activity) {
        model.initChart(activity)
    }

    // 显示分享对话框
    override fun showSharePopuWindow(activity: Activity) {
        var dialog = ShareShareDialog(activity)
        dialog.showDialog()

    }

    override fun initView(activity: Activity) {
        // 步数
        val list = model.queryData(mark = mark, type = Constants.STEP)
        val detail = model.queryData<FlatSportBean>(mark)
        var step = 0
        var step_max = 0
        list?.forEach {
            step += it.value;
            if (step_max < it.value) {
                step_max = it.value
            }
        }
//        activity.tv_step_num.text = "$step"
        // 平均步频
        activity.tv_step_frequency_av.text = "${step / list!!.size}"
        // 图表数据适配
        activity.tv_speed_walk_av.text = "${step / list!!.size}"
        activity.tv_speed_walk_best.text = "$step_max"
//         平均心率
//        val list1 = model.queryData(mark = mark, type = Constants.HEART_RATE)
//        var heart_rate = 0
//        var heart_rate_max = 0
//        list1?.forEach {
//            heart_rate += it.value
//            if (heart_rate_max < it.value) {
//                heart_rate_max = it.value
//            }
//        }
//        activity.tv_speed_heart_av.text = "${heart_rate / list1!!.size}"
        // 图表数据适配
//        activity.tv_speed_heart_best.text = "$heart_rate_max"
//        activity.tv_speed_heart1_av.text = "${heart_rate / list1!!.size}"
        // 总的距离

        // 总的运动时间
        activity.tv_sport_time.text = duration
        // 最佳配速

        // 热量
        val list2 = model.queryData(mark, Constants.CALORIE)
        var calorie = 0
        list2?.forEach {
            calorie += it.value
        }
        activity.tv_heat_quantity.text = "$calorie"

        if (type == Constants.ACTIVITY_CLIMBING) {
            val detail = model.queryData<ClimbingSportBean>(mark)
            // 暂时没有
        } else {
            val detail = model.queryData<FlatSportBean>(mark)
            detail?.let {
                activity.tv_step_num.text = "${it[0].step_num}"
                activity.tv_speed_heart_av.text = "${it[0].average_heart_rate}"
                activity.tv_speed_heart1_av.text = "${it[0].average_heart_rate}"
                activity.tv_speed_heart_best.text = "${it[0].max_heart_rate}"
                if (it[0].distance < 100) {
                    activity.tv_distance.text = "${String.format("%.2f", it[0].distance.toFloat() / 1000)}"
                } else {
                    activity.tv_distance.text = "${String.format("%.1f", it[0].distance.toFloat() / 1000)}"
                }

                val speed_allocation_av = BaseUtils.intToByteArray(it[0].speed)
                activity.tv_speed_allocation_av.text = "${speed_allocation_av[0].toInt() and 0xFF}${"\'"}${speed_allocation_av[1].toInt() and 0xFF}${"\""}"
                activity.tv_allocation_speed_av.text = "${speed_allocation_av[0].toInt() and 0xFF}${"\'"}${speed_allocation_av[1].toInt() and 0xFF}${"\""}"
                val speed_allocation_best = BaseUtils.intToByteArray(it[0].best_speed)
                activity.tv_speed_allocation_best.text = "${speed_allocation_best[0].toInt() and 0xFF}${"\'"}${speed_allocation_best[1].toInt() and 0xFF}${"\""}"
                // 平均配速
                activity.tv_allocation_speed_best.text = "${speed_allocation_best[0].toInt() and 0xFF}${"\'"}${speed_allocation_best[1].toInt() and 0xFF}${"\""}"
            }
        }
    }

    private fun drawPaths() {
        // 绘制轨迹
        var latLngs = model.queryData(mark, Constants.GPS)
        BaseUtils.ifNotNull(latLngs, aMap) { it, amp ->
            var list = ArrayList<LatLng>()
            var latLng = LatLng(it[0].gps_latitude / 100000, it[0].gps_longitude / 100000)
            if (it[0].gps_latitude != 0.0 && it[0].gps_longitude != 0.0) {
                list.add(latLng)
            }
            it.forEach {
                var values = BaseUtils.gps84_To_Gcj02(it.gps_latitude / 100000, it.gps_longitude / 100000)
                var tmp = LatLng(values[0], values[1])
                if (it.gps_latitude != 0.0 && it.gps_longitude != 0.0 &&
                        AMapUtils.calculateLineDistance(latLng, tmp) > 1) {
                    latLng = tmp
                    list.add(tmp)
                }
            }
            Log.e("LatLon", "draw paths ....")
            if (list.size > 0) {
                locationUtils?.drawPath(amp, list)
            }
        }
    }

    override fun detachView() {
        super.detachView()
        locationUtils?.detachCallBack()
        locationUtils = null
        aMap = null
    }

    private var model: SportModel = SportModel(mark)
}