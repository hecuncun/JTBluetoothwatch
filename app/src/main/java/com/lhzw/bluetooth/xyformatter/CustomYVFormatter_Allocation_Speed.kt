package com.lhzw.bluetooth.xyformatter

import android.util.Log
import com.github.mikephil.charting.formatter.ValueFormatter
import com.lhzw.bluetooth.uitls.BaseUtils

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/25 9:28
 *
 */
// 配速
class CustomYVFormatter_Allocation_Speed(private var drawY: Boolean) : ValueFormatter() {
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
        Log.e("AllocationY", "Y $value")
        if (value != 0.0f) {
            var tem = 1 / value
            val speed_allocation_av = BaseUtils.intToByteArray(tem.toInt())
            return "${speed_allocation_av[0].toInt() and 0xFF}${"\'"}${speed_allocation_av[1].toInt() and 0xFF}${"\""}"
        }
        return ""
    }
}