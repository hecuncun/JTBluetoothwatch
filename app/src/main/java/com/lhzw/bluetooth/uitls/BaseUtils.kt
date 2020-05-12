@file:Suppress("DEPRECATION")

package com.lhzw.bluetooth.uitls

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.constants.Constants
import java.util.*
import java.util.regex.Pattern


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/19 14:18
 *
 */
object BaseUtils {
    fun checkMySelfPermission(perm: String): Int {
        try {
            val method = javaClass.getMethod("checkSelfPermission", *arrayOf<Class<*>>(String::class.java))
            return method.invoke(this, perm) as Int
        } catch (e: Throwable) {
        }

        return -1
    }

    fun shouldShowMyRequestPermissionRationale(perm: String): Boolean {
        try {
            val method = javaClass.getMethod("shouldShowRequestPermissionRationale", *arrayOf<Class<*>>(String::class.java))
            return method.invoke(this, perm) as Boolean
        } catch (e: Throwable) {
        }

        return false
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        try {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * 检测是否安装
     *
     * @param context
     * @return
     */
    fun isAppInstall(appPackage: String): Boolean {
        var pinfo = App.instance.packageManager.getInstalledPackages(0)// 获取所有已安装程序的包信息
        pinfo?.forEach {
            Log.e("Tag", "packageName  :  ${it.packageName}")
            if (it.packageName.equals(appPackage)) {
                return true
            }
        }
        return false
    }

    fun isNetworkConnected(): Boolean {
        var mConnectivityManager = App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var mNetworkInfo = mConnectivityManager.activeNetworkInfo;
        if (mNetworkInfo != null) {
            return mNetworkInfo.isConnected
        }
        return false
    }

    /**
     * 判断服务是否运行
     */
    fun isServiceRunning(className: String): Boolean {
        val activityManager = App.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val info = activityManager?.getRunningServices(Integer.MAX_VALUE)
        if (info == null || info.size == 0) return false
        for (aInfo in info) {
            if (className == aInfo.service.className) return true
        }
        return false
    }

    fun <T1, T2> ifNotNull(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)) {
        if (value1 != null && value2 != null) {
            bothNotNull(value1, value2)
        }
    }

    fun <T> ifNotNull(value: T?, notNull: (T) -> (Unit)) {
        value?.let { notNull(it) }
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    fun byte2HexStr(b: ByteArray): String? {
        var stmp = ""
        val sb = StringBuilder("")
        for (n in b.indices) {
            stmp = Integer.toHexString(b[n].toInt() and 0xFF)
            sb.append(if (stmp.length == 1) "0$stmp" else stmp)
            sb.append(" ")
        }
        return sb.toString().toUpperCase().trim { it <= ' ' }
    }

    /*
    * byte数据转换为Int
    * */
    fun byteToInt(content: List<Byte>?): Int {
//        Log.e("result", "result : ${byte2HexStr(content!!.toByteArray())}")
        var tmp: Int = 0
        var counter = 0
        content?.forEach {
            tmp = tmp or ((it.toInt() and 0xFF) shl (8 * counter))
            counter++
        }
        return tmp
    }

    /*
    * byte数据转换为Long
    * */
    fun byteToLong(content: List<Byte>?): Long {
        var tmp: Long = 0L
        var counter = 0
        content?.forEach {
            tmp = tmp or ((it.toLong() and 0xFF) shl (8 * counter))
            counter++
        }
        return tmp
    }

    /**
     * 将String里的数字提取出来
     */
    fun keepDigital(oldString: String): String {
        val newString = StringBuffer()
        val matcher = Pattern.compile("\\d").matcher(oldString)
        while (matcher.find()) {
            newString.append(matcher.group())
        }
        return newString.toString()
    }


    /**
     * 将int转换为byte数组
     *
     * */
    fun intToByteArray(value: Int): Array<Byte> {
        var arr = arrayOf(
                (value and 0xff).toByte(),
                ((value shr 8) and 0xff).toByte(),
                ((value shr 16) and 0xff).toByte(),
                ((value shr 24) and 0xff).toByte()
        )
        return arr
    }

    /**
     * 将Long转换为Bytes
     *
     */
    fun longToByteArray(value: Long): Array<Byte> {
        var arr = arrayOf(
                (value and 0xff).toByte(),
                ((value shr 8) and 0xff).toByte(),
                ((value shr 16) and 0xff).toByte(),
                ((value shr 24) and 0xff).toByte(),
                ((value shr 32) and 0xff).toByte(),
                ((value shr 40) and 0xff).toByte(),
                ((value shr 48) and 0xff).toByte(),
                ((value shr 56) and 0xff).toByte()
        )
        return arr
    }

    /**
     *  日期格式化
     */
    fun formatData(start: Long, end: Long): List<String> {
        var bytes = longToByteArray(start)
        var bytes1 = longToByteArray(end)
        var ymd = "20${bytes[0]}."
        if (bytes[1] < 0x0A) {
            ymd += "0${bytes[1]}."
        } else {
            ymd += "${bytes[1]}."
        }
        if (bytes[2] < 0x0A) {
            ymd += "0${bytes[2]}"
        } else {
            ymd += "${bytes[2]}"
        }
        var hms = ""
        if (bytes[3] < 0x0A) {
            hms += "0${bytes[3]}:"
        } else {
            hms += "${bytes[3]}:"
        }
        if (bytes[4] < 0x0A) {
            hms += "0${bytes[4]}:"
        } else {
            hms += "${bytes[4]}:"
        }
        if (bytes[5] < 0x0A) {
            hms += "0${bytes[5]}"
        } else {
            hms += "${bytes[5]}"
        }
        var value = (bytes1[3] * 3600 + bytes1[4] * 60 + bytes1[5]) - (bytes[3] * 3600 + bytes[4] * 60 + bytes[5])
        var hour = value / 3600
        var timeLag = ""
        if (hour < 0x0A) {
            timeLag += "0$hour:"
        } else {
            timeLag += "$hour:"
        }
        var min = (value - hour * 3600) / 60
        if (min < 0x0A) {
            timeLag += "0$min:"
        } else {
            timeLag += "$min:"
        }
        var second = value % 60
        if (second < 0x0A) {
            timeLag += "0$second"
        } else {
            timeLag += "$second"
        }
        return listOf(ymd, hms, timeLag)
    }

    /**
     * 判断一个字符为中文
     * @param c
     * @return
     */

    fun isChinese(c: Char): Boolean {
        val str = c.toString()
        val p = Pattern.compile("[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】]")
        val m = p.matcher(str)
        return m.find()
    }

    private fun transformLat(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
        ret += (20.0 * Math.sin(6.0 * x * Constants.PI) + 20.0 * Math.sin(2.0 * x * Constants.PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(y * Constants.PI) + 40.0 * Math.sin(y / 3.0 * Constants.PI)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(y / 12.0 * Constants.PI) + 320 * Math.sin(y * Constants.PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLon(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + (0.1
                * Math.sqrt(Math.abs(x)))
        ret += (20.0 * Math.sin(6.0 * x * Constants.PI) + 20.0 * Math.sin(2.0 * x * Constants.PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(x * Constants.PI) + 40.0 * Math.sin(x / 3.0 * Constants.PI)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(x / 12.0 * Constants.PI) + 300.0 * Math.sin(x / 30.0
                * Constants.PI)) * 2.0 / 3.0
        return ret
    }

    /**
     * 84 ==》 高德
     * @param lat
     * @param lon
     * @return
     */
    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param lat
     * @param lon
     * @return
     */
    fun gps84_To_Gcj02(lat: Double, lon: Double): DoubleArray {
        if (outOfChina(lat, lon)) {
            return doubleArrayOf(lat, lon)
        }
        var dLat = transformLat(lon - 105.0, lat - 35.0)
        var dLon = transformLon(lon - 105.0, lat - 35.0)
        val radLat: Double = lat / 180.0 * Constants.PI
        var magic = Math.sin(radLat)
        magic = 1 - Constants.EE * magic * magic
        val sqrtMagic = Math.sqrt(magic)
        dLat = dLat * 180.0 / (Constants.A * (1 - Constants.EE) / (magic * sqrtMagic) * Constants.PI)
        dLon = dLon * 180.0 / (Constants.A / sqrtMagic * Math.cos(radLat) * Constants.PI)
        val mgLat = lat + dLat
        val mgLon = lon + dLon
        return doubleArrayOf(mgLat, mgLon)
    }


    private fun outOfChina(lat: Double, lon: Double): Boolean {
        if (lon < 72.004 || lon > 137.8347) return true
        return if (lat < 0.8293 || lat > 55.8271) true else false
    }

    // 获取当前日期
    fun getCurrentData(): String {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar[Calendar.YEAR] % 100
        val month = calendar[Calendar.MONTH] + 1
        val day = calendar[Calendar.DAY_OF_MONTH]
        return "$year-$month-$day"
    }

    fun dip2px(dpValue: Int): Int {
        val scale = App.context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}