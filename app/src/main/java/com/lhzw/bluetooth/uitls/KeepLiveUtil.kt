package com.lhzw.bluetooth.uitls

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.support.annotation.RequiresApi
import com.lhzw.bluetooth.application.App.Companion.context
import java.lang.Exception

/**
 * Created by heCunCun on 2020/7/13
 * 配置权限https://juejin.im/post/5dfaeccbf265da33910a441d#heading-2
 * <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
 */
class KeepLiveUtil {
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun isIgnoringBatteryOptimizations(): Boolean {
        var isIgnoring = false
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        return isIgnoring
    }

    @SuppressLint("BatteryLife")
    @RequiresApi(api = Build.VERSION_CODES.M)
     fun requestIgnoreBatteryOptimizations(){
        try {
            val intent =Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:"+ context.packageName)
            context.startActivity(intent)
        }catch (e:Exception){
            e.printStackTrace();
        }

    }

}