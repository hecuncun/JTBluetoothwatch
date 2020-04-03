package com.lhzw.bluetooth.xyformatter

import android.util.Log
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/25 9:28
 *
 */
// 配速
class CustomYVFormatter_Allocation_Speed(private var drawY: Boolean, private var max : Float) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        if (drawY) {
            return valueOfY_format(value)
        } else {
            if (value < 1) {
                return "${String.format("%.2f", value)}"
            } else {
                return "${String.format("%.1f", value)}"
            }
            return value.toString()
        }
        return super.getFormattedValue(value)
    }

    private fun valueOfY_format(value: Float): String {
        Log.e("H_AllocationY", "mValue : ${value}")
        if (value != 0.0f) {
            var y_value = max - value
            val min = (y_value / 60).toInt()
            val second = (y_value % 60).toInt()
            var str = ""
            if(min < 0x0A) {
                str += "0$min${"\'"}"
            } else {
                str += "$min${"\'"}"
            }
            if(second < 0x0A) {
                str += "0$second${"\\′"}"
            } else {
                str += "$second${"\""}"
            }
            return str
        }
        return ""
    }
}