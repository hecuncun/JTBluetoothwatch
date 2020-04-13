package com.lhzw.bluetooth.event

import android.bluetooth.BluetoothDevice
import android.content.Context

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/7 10:18
 *
 */
data class BlutoothEvent(val device: BluetoothDevice, val context: Context)