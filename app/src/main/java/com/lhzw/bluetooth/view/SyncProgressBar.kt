package com.lhzw.bluetooth.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import com.lhzw.bluetooth.R
import kotlinx.android.synthetic.main.dialog_progress_bar.*

/**
 *
@author：created by xtqb
@description:
@date : 2020/4/9 11:43
 *
 */
class SyncProgressBar(context: Context) : AlertDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress_bar)
        init()
    }

    private fun init() {
        setCancelable(false)
        window.decorView.setBackgroundResource(R.color.transparent)
    }

    fun setProgressBarMax(max: Int) {
        progesss.max = max
    }

    // 更新进度条
    fun refleshProgressBar(value: Int, content: String) {
        tv_progress_content.text = content
        progesss.setProgress(value)
    }

    fun startProgress() {
        progesss.max = 100
        progesss.setProgress(0)
        var counter = 0
        Thread {
            while (counter < 101) {
                counter++
                progesss.setProgress(counter)
                Thread.sleep(50)
            }
        }.start()
    }
}