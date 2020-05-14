package com.lhzw.bluetooth.uitls

import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.*
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App.Companion.context
import com.lhzw.bluetooth.constants.Constants


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/26 11:21
 *
 */

class LocationUtils : AMapLocationListener {
    private var start: Marker? = null
    private var stop: Marker? = null
    private var paths: MutableList<Polyline>? = ArrayList<Polyline>()
    private var callBack: ILocationCallBack? = null
    private var center_lat by Preference(Constants.CENTER_LAT, Constants.LAT)
    private var center_lgt by Preference(Constants.CENTER_LGT, Constants.LGT)
    private var mGradientHelper: GradientHelper = GradientHelper(Constants.startColor, Constants.endColor)

    fun startLocate() {
        var aMapLocationClient = AMapLocationClient(context);
        aMapLocationClient.apply {
            //设置监听回调
            setLocationListener(this@LocationUtils);
            //初始化定位参数
            var clientOption = AMapLocationClientOption();
            clientOption.apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving;
                isNeedAddress = true;
                isOnceLocation = false;
                //设置是否强制刷新WIFI，默认为强制刷新
                isWifiActiveScan = true;
                //设置是否允许模拟位置,默认为false，不允许模拟位置
                isMockEnable = false;
                //设置定位间隔
                interval = 2000;
                aMapLocationClient?.setLocationOption(clientOption);
            }
            startLocation();
        }
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功完成回调
                aMapLocation?.apply {
                    var country = aMapLocation.country
                    var province = aMapLocation.province
                    var city = aMapLocation.city
                    var district = aMapLocation.district
                    var street = aMapLocation.street
                    var lat = aMapLocation.latitude
                    var lgt = aMapLocation.longitude
                    val distance = AMapUtils.calculateLineDistance(LatLng(lat, lgt), LatLng(center_lat, center_lgt))
                    if (distance > 5) {
                        center_lat = lat
                        center_lgt = lgt
                    }
                    callBack?.callBack(country + province + city + district + street, lat, lgt, aMapLocation)
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.errorCode + ", errInfo:"
                        + aMapLocation.errorInfo)
            }
        }
    }

    /**
     * 自定义图标
     *
     * @return
     */
    private fun getMarkerOption(str: String, latLng: LatLng, icon_id: Int): MarkerOptions {
        var markerOptions = MarkerOptions()
        markerOptions.icon(BitmapDescriptorFactory.fromResource(icon_id))
        markerOptions.position(latLng)
        markerOptions.title(str);
        markerOptions.snippet("纬度:${latLng.latitude}   经度:${latLng.longitude}")
        markerOptions.period(100)

        return markerOptions
    }

    /**
     *
     * 绘制路径
     *
     */
    fun drawPath(aMap: AMap, list: MutableList<LatLng>) {
        start?.remove()
        stop?.remove()
        paths?.forEach {
            it.remove()
        }
        val path_colors = arrayOf(R.color.red_path, R.color.yellow_path, R.color.green_path, R.color.cyan_path)
        if (list != null && list.size > 0) {
            start = aMap.addMarker(getMarkerOption("起点", list[0], R.mipmap.location_start))
            var oldLatLng: LatLng = list[0]
            var polyLineList = ArrayList<PolylineOptions>()
            mGradientHelper.retValue(-1)
            mGradientHelper.retPoint(list.size)
            list.forEach { it ->
                val polylineOptions = PolylineOptions()
                polylineOptions.width(15f);
                polylineOptions.color(mGradientHelper.getGradient())
                polylineOptions.useGradient(true)
                polylineOptions.add(oldLatLng, it)
                polyLineList.add(polylineOptions)
                oldLatLng = it;
            }
            polyLineList.forEach {
                paths?.add(aMap.addPolyline(it))
            }
            stop = aMap.addMarker(getMarkerOption("终点", list.get(list.size - 1), R.mipmap.location_stop))
            polyLineList.clear()
            list.clear()
        }
    }

    interface ILocationCallBack {
        fun callBack(str: String, lat: Double, lgt: Double, aMapLocation: AMapLocation);
    }

    fun detachCallBack() {
        paths?.apply {
            forEach {
                it.remove()
            }
            clear()
            paths = null
        }
        start?.apply {
            remove()
            start = null
        }

        stop?.apply {
            remove()
            stop = null
        }
        callBack = null
    }

    fun setLocationCallBack(callBack: ILocationCallBack) {
        this@LocationUtils.callBack = callBack
    }
}
