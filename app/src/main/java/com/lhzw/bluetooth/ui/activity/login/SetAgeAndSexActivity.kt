package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.uitls.DateUtils
import kotlinx.android.synthetic.main.activity_set_age_and_sex.*

/**
 * Created by heCunCun on 2020/8/12
 */
class SetAgeAndSexActivity : BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_set_age_and_sex
    override fun initData() {

    }

    override fun initView() {

    }

    var age = 26
    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        btn_next.setOnClickListener {
            Intent(this, SetWeightActivity::class.java).apply {
                startActivity(this)
            }
        }

        //日期选择
        val dialogTime = TimePickerDialog.Builder()
                .setCallBack { _, millseconds ->
                    val dateString = DateUtils.longToString(millseconds, "yyyy.MM.dd")
                    tv_birthday.text = dateString
                    val birthYear = DateUtils.longToString(millseconds, "yyyy").toInt()
                    val nowYear = DateUtils.longToString(System.currentTimeMillis(), "yyyy").toInt()
                    age = nowYear - birthYear
                }
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId("生日")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis() - 100L * 365 * 1000 * 60 * 60 * 24L)
                .setMaxMillseconds(System.currentTimeMillis())
                .setCurrentMillseconds(System.currentTimeMillis() - 25L * 365 * 1000 * 60 * 60 * 24L)
                .setThemeColor(resources.getColor(R.color.orange))
                .setType(Type.YEAR_MONTH_DAY)
                .setWheelItemTextNormalColor(resources.getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(resources.getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(12)
                .build()

        tv_birthday.setOnClickListener {
            dialogTime.show(supportFragmentManager, "a")
        }
    }
}