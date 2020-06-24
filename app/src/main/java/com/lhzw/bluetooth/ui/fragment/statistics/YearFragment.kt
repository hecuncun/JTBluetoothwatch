package com.lhzw.bluetooth.ui.fragment.statistics

import android.view.View
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.XAxisValueFormatter
import com.lhzw.bluetooth.widget.XYMarkerView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_year.*

/**
 * Created by heCunCun on 2020/6/23
 */
class YearFragment:BaseFragment() {
    override fun attachLayoutRes(): Int= R.layout.fragment_year

    override fun initView(view: View) {
        tv_step_num.typeface= Constants.font_futurn_num
        tv_cal_num.typeface= Constants.font_futurn_num
        initBarChart(bar_step, resources.getColor(R.color.color_gray_757575))
        initBarChart(bar_cal, resources.getColor(R.color.color_gray_757575))
        val stepValues = ArrayList<BarEntry>()
        for (i in 0..11) {
            val num = (Math.random() * 1000).toFloat()
            //   val num = dailyInfoList[i].daily_steps.toFloat()+dailyInfoList[i].sport_steps.toFloat()
            stepValues.add(BarEntry(i.toFloat(), num))
        }
        Logger.e("加载month数据")
        initBarData(bar_step, stepValues, resources.getColor(R.color.green_path), resources.getColor(R.color.green_33CC99))
        initBarData(bar_cal, stepValues, resources.getColor(R.color.color_pink_FF00FF), resources.getColor(R.color.color_pink_FF0099))
    }

    override fun lazyLoad() {
    }

    private fun initBarChart(barChat: BarChart, mainColor: Int) {
        val times = arrayOf("一月", "·", "·", "·", "·","六月" ,"·","·","·","·","·", "十二月")

        barChat.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                //选中值
                Logger.e("value=${e.toString()}")
            }
        })

        barChat.apply {
            description.isEnabled = false
            setMaxVisibleValueCount(12)//最大显示24个值
            setPinchZoom(false)//手指缩放
            setDrawBarShadow(true)//画条形图背景

            setDrawGridBackground(false)
            setDrawValueAboveBar(false)
            setScaleEnabled(false)
            extraBottomOffset = 0f
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                gridColor = resources.getColor(R.color.white)
                textColor = mainColor
                valueFormatter = XAxisValueFormatter(times)
                axisLineColor = mainColor
                setLabelCount(12, false)//设置标签个数,true为精确
            }

            axisLeft.apply {
                isEnabled = true
                setDrawLabels(false)//标签
                setDrawAxisLine(false)//轴线
                setDrawGridLines(false)//网格线
                gridColor = resources.getColor(R.color.gray)
                axisMinimum = 0f
                //  enableGridDashedLine(20f, 5f, 0f)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            val mv = XYMarkerView(activity, XAxisValueFormatter(times))
            mv.chartView = barChat
            marker = mv
        }

    }

    private fun initBarData(barChat: BarChart, values: ArrayList<BarEntry>, startColor: Int, endColor: Int) {
        val set1: BarDataSet?
        if (barChat.data != null && barChat.data.dataSetCount > 0) {
            set1 = barChat.data.getDataSetByIndex(0) as BarDataSet
            set1.values = values
            barChat.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "Data Set")
            // * 可变参数展开操作符
            // set1.setColors(*ColorTemplate.VORDIPLOM_COLORS)
            set1.setGradientColor(startColor, endColor)
            if (barChat == bar_step) {
                set1.barShadowColor = resources.getColor(R.color.color_green_0C1A00)
            } else {
                set1.barShadowColor = resources.getColor(R.color.color_red_180008)
            }

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.barWidth = 0.5f
            barChat.data = data
            barChat.setFitBars(true) // 在bar开头结尾两边添加一般bar宽的留白
        }
        barChat.invalidate()
    }
}