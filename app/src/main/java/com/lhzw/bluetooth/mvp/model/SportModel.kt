package com.lhzw.bluetooth.mvp.model

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.FlatSportBean
import com.lhzw.bluetooth.bean.SportDetailInfobean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.uitls.BaseUtils.checkMySelfPermission
import com.lhzw.bluetooth.uitls.BaseUtils.shouldShowMyRequestPermissionRationale
import com.lhzw.bluetooth.xyformatter.CustomYVFormatter_Allocation_Speed
import com.lhzw.bluetooth.xyformatter.CustomYVFormatter_Speed_Walk
import com.lhzw.bluetooth.xyformatter.CustomYVFormatter_Speed_heart
import com.lhzw.bluetooth.xyformatter.HorizontalBarXYFormatter
import kotlinx.android.synthetic.main.activity_sport_info.*
import org.litepal.crud.LitePalSupport


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/18 15:37
 *
 */

class SportModel(var mark: String) : SportConstract.Model {
    val AXISEMax = 12
    override fun checkPermissions(activity: Activity, permissions: Array<String>): Boolean {
        var isHash: Boolean = true
        try {
            if (Build.VERSION.SDK_INT >= 23 &&
                    App.instance.applicationInfo.targetSdkVersion >= 23) {
                var needRequestPermissonList = ArrayList<String>()
                permissions.forEach {
                    if (checkMySelfPermission(it) != PackageManager.PERMISSION_GRANTED
                            || shouldShowMyRequestPermissionRationale(it)) {
                        needRequestPermissonList.add(it)
                    }
                }
                if (needRequestPermissonList.size > 0) {
                    ActivityCompat.requestPermissions(activity, permissions, 0)
                    isHash = false
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            isHash = false
        }
        return isHash
    }

    override fun initMap(mMapView: MapView?): AMap? {
        return mMapView?.map
    }

    // 初始化图表
    override fun initChart(activity: Activity) {
        // 配速
        val speed_list = queryData(mark = mark, type = Constants.SPEED)
        speed_list?.let {
            val values = ArrayList<Entry>()
            var max = 0.0f
            val tem_List = speed_list.filter {
                it.value > 0
            }.map {
                (1000000.0f / it.value)
            }
            tem_List.forEach {
                if(max < it){
                    max = it
                }
            }
            var index = 0.0f
            tem_List.forEach { v ->
                values.add(Entry(index, max - v))
                index += 1
            }
            setLineChart(activity.linechart_allocation_speed, values, R.drawable.gradient_allocation_speed, 6, 4,
                    CustomYVFormatter_Allocation_Speed(false, max), CustomYVFormatter_Allocation_Speed(true, max))
        }

        //心率
        val heart_list = queryData(mark = mark, type = Constants.HEART_RATE)
        val value = ArrayList<Entry>()
        var axisMax = 0
        var index = 0.0f
        heart_list?.forEach {
            value.add(Entry(index, it.value.toFloat()))
            index += 1.0f
            if (axisMax < it.value) {
                axisMax = it.value
            }
        }
        var temValue = value.map {
            Entry(it.x, it.y / (axisMax / AXISEMax))
        }.toMutableList()
        value.clear()
        setLineChart(activity.linechart_speed_heart, temValue, R.drawable.gradient_speed_heart, 6, 4,
                CustomYVFormatter_Speed_heart(false, 0), CustomYVFormatter_Speed_heart(true, axisMax / AXISEMax))

        //步频
        var value1 = ArrayList<Entry>()
        axisMax = 0
        val steps = queryData(mark = mark, type = Constants.STEP)
        index = 0.0f
        steps?.forEach {
            value1.add(Entry(index, it.value.toFloat()))
            index += 1.0f
            if (axisMax < it.value) {
                axisMax = it.value
            }
        }
        var temValue1 = value1.map {
            Entry(it.x, it.y / (axisMax / AXISEMax))
        }.toMutableList()
        setLineChart(activity.linechart_speed_walk, temValue1, R.drawable.gradient_speed_walk, 6, 4,
                CustomYVFormatter_Speed_Walk(false, 0), CustomYVFormatter_Speed_Walk(true, axisMax / AXISEMax))

//        //步幅
//        setLineChart(activity.linechart_step_stride, values, R.drawable.gradient_step_stride,
//                CustomYVFormatter_Step_Stride(false), CustomYVFormatter_Step_Stride((true)))

        // 横向柱状图
        var detail = queryData<FlatSportBean>(mark)
        var count = 4
        var range = 100
        var colorBar = mutableListOf<Int>(activity.resources.getColor(R.color.red_dot_color),
                activity.resources.getColor(R.color.yellow_little1), activity.resources.getColor(R.color.green2),
                activity.resources.getColor(R.color.blue_little2), activity.resources.getColor(R.color.white))
        var barWidth = 4f
        var spaceForBar = 10f
        var yValus = ArrayList<BarEntry>()

        var lables = activity.resources.getStringArray(R.array.heart_rate_types)
        var valueDescrip = arrayOf("01:53", "09:11", "13:20", "01:18", "06:24")
        detail?.let {
            var sum = it[0].warmup_heart_rate + it[0].flaming_heart_rate + it[0].aerobic_heart_rate + it[0].anaerobic_heart_rate + it[0].limitation_heart_rate
            if (sum == 0) {
                sum = 1
            }
            val value = arrayOf(
                    100 * it[0].limitation_heart_rate.toFloat() / sum,
                    100 * it[0].anaerobic_heart_rate.toFloat() / sum,
                    100 * it[0].aerobic_heart_rate.toFloat() / sum,
                    100 * it[0].flaming_heart_rate.toFloat() / sum,
                    100 * it[0].warmup_heart_rate.toFloat() / sum
            )
            var warmup_heart_rate_str = ""
            var tem = it[0].warmup_heart_rate / 1000 / 60
            if (tem < 0x0A) {
                warmup_heart_rate_str += "0$tem"
            } else {
                warmup_heart_rate_str += "$tem"
            }
            tem = it[0].warmup_heart_rate / 1000 % 60
            if (tem < 0x0A) {
                warmup_heart_rate_str += "0$tem"
            } else {
                warmup_heart_rate_str += "$tem"
            }

            var valueDescrip = arrayOf(
                    formatHeartRate(it[0].limitation_heart_rate / 1000 / 60, it[0].limitation_heart_rate / 1000 % 60),
                    formatHeartRate(it[0].anaerobic_heart_rate / 1000 / 60, it[0].anaerobic_heart_rate / 1000 % 60),
                    formatHeartRate(it[0].aerobic_heart_rate / 1000 / 60, it[0].aerobic_heart_rate / 1000 % 60),
                    formatHeartRate(it[0].flaming_heart_rate / 1000 / 60, it[0].flaming_heart_rate / 1000 % 60),
                    formatHeartRate(it[0].warmup_heart_rate / 1000 / 60, it[0].warmup_heart_rate / 1000 % 60)
            )
            Log.e("valueDescrip", "value : ${valueDescrip}")
            for (i in 0..count) {
                yValus.add(BarEntry(i * spaceForBar, value[i], activity.resources.getDrawable(R.drawable.gradient_speed_heart)))
            }
            setHorizontalBar(activity.horizontalbar, yValus, barWidth, HorizontalBarXYFormatter(false, lables), HorizontalBarXYFormatter(true, lables), colorBar, valueDescrip)
        }
    }

    override fun queryData(mark: String, type: Int): List<SportDetailInfobean>? {
        val map = HashMap<String, String>()
        map["sport_detail_mark"] = mark
        map["data_type"] = "$type"
        val list = CommOperation.query(SportDetailInfobean::class.java, map, "data_index asc");
        map.clear()
        return list;
    }

    override fun <T : LitePalSupport> queryData(mark: String): List<T>? {
        return CommOperation.query(FlatSportBean::class.java, "sport_detail_mark", mark) as List<T>
    }

    // 设置LineChart
    private fun setLineChart(lineChart: LineChart, values: MutableList<Entry>,
                             fillColor: Int, xLabelCount: Int, yLabelCount: Int, valueFormatter_X: ValueFormatter, valueFormatter_Y: ValueFormatter) {
        lineChart?.apply {
            setViewPortOffsets(90.toFloat(), 60.toFloat(), 60.toFloat(), 60.toFloat())
            description.isEnabled = false
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setGridBackgroundColor(App.instance.resources.getColor(R.color.gray_transparent))
            axisLeft.setStartAtZero(true)
            xAxis?.apply {
                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM;
                textSize = 8f;
                setDrawAxisLine(true);
                setLabelCount(xLabelCount, false)
                textColor = App.instance.resources.getColor(R.color.white)
                //textColor = App.instance.resources.getColor(R.color.gray_transparent)
                setDrawGridLines(false)
                axisLineWidth = 1.2f
                valueFormatter = valueFormatter_X
            }
            axisLeft?.apply {
                typeface = Typeface.createFromAsset(App.instance.assets, "OpenSans-Light.ttf")
                setLabelCount(yLabelCount, false)
                textSize = 8f
                textColor = App.instance.resources.getColor(R.color.white)
                //textColor = App.instance.resources.getColor(R.color.gray_transparent)
                setDrawGridLines(false)
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                axisLineColor = App.instance.resources.getColor(R.color.gray_lite)
                axisLineWidth = 0.8f
                setDrawZeroLine(false)
                valueFormatter = valueFormatter_Y
            }

            axisRight.isEnabled = false;
            legend.isEnabled = false;
            animateXY(2000, 2000);
            // don't forget to refresh the drawing
            invalidate()
            val set1: LineDataSet
            if (data != null && data.dataSetCount > 0) {
                set1 = data.getDataSetByIndex(0) as LineDataSet
                set1.values = values
                data.notifyDataChanged()
                notifyDataSetChanged()
            } else {
                // create a dataset and give it a type
                set1 = LineDataSet(values, "DataSet 1")
                set1.enableDashedLine(10f, 5f, 0f);
                set1.enableDashedHighlightLine(10f, 5f, 0f);
                set1.mode = LineDataSet.Mode.CUBIC_BEZIER
                set1.cubicIntensity = 0.2f
                set1.setDrawFilled(true)
                set1.setDrawCircles(false)
                set1.lineWidth = 0f
                set1.circleRadius = 4f
//                set1.setCircleColor(App.instance.resources.getColor(R.color.btn_green_focused))
//                set1.highLightColor = App.instance.resources.getColor(R.color.btn_green_focused)
//                set1.color = App.instance.resources.getColor(fillColor)
//                set1.fillColor = App.instance.resources.getColor(fillColor)
                set1.fillDrawable = App.instance.resources.getDrawable(fillColor)
                set1.fillAlpha = 255
//                set1.setGradientColor(App.instance.getColor(R.color.gray_lite),fillColor)
                set1.setDrawHorizontalHighlightIndicator(false)
                set1.fillFormatter = IFillFormatter { _, _ -> this.axisLeft.axisMinimum }
                // create a data object with the data sets
                val data = LineData(set1)
                data.setValueTypeface(Typeface.createFromAsset(App.instance.assets, "OpenSans-Light.ttf"))
                data.setValueTextSize(9f)
                data.setDrawValues(false)
                this.data = data
                this.invalidate()
            }
        }
    }

    private fun setHorizontalBar(horizontalbar: HorizontalBarChart, values: MutableList<BarEntry>,
                                 barWidth: Float, xFormatter: HorizontalBarXYFormatter, yFormatter: HorizontalBarXYFormatter, colors: MutableList<Int>, valueDescrip: Array<String>) {
        horizontalbar?.apply {
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            setMaxVisibleValueCount(100)
            setPinchZoom(false)
            setScaleEnabled(false)
            setFitBars(true)
            axisRight.isEnabled = false;  //不绘制右侧轴线
            isDrawHoriDescript = true

            xAxis?.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = xFormatter
                textColor = App.instance.resources.getColor(R.color.gray_transparent)
                axisLineWidth = 2.0f
                textSize = 14f
                setDrawAxisLine(false);
                setDrawGridLines(false)
                typeface = Typeface.createFromAsset(App.instance.assets, "OpenSans-Light.ttf")
                granularity = -0.5f
//                setExtraOffsets(0f, 20f, 0f,0f)
            }
            axisLeft?.apply {
                typeface = Typeface.createFromAsset(App.instance.assets, "OpenSans-Light.ttf")
                textColor = App.instance.resources.getColor(R.color.red_little)
                setDrawGridLines(false)
                setDrawAxisLine(false);
                isEnabled = false
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                textSize = 10f
                valueFormatter = yFormatter
                axisMinimum = 0f  // 柱状条平移 offset

            }

            axisRight?.apply {
                typeface = Typeface.createFromAsset(App.instance.assets, "OpenSans-Light.ttf")
                textColor = App.instance.resources.getColor(R.color.gray_transparent)
                setDrawGridLines(false)
                setDrawAxisLine(false);
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                textSize = 10f
                axisMinimum = 0f
                extraTopOffset = 20f
            }

            //数据
            val set1: BarDataSet
            if (data != null && data.dataSetCount > 0) {
                set1 = data.getDataSetByIndex(0) as BarDataSet
                set1.values = values
                data.notifyDataChanged()
                notifyDataSetChanged()
            } else {
                set1 = BarDataSet(values, "DataSet 1")
                set1.setDrawIcons(false)
                val dataSets = ArrayList<IBarDataSet>()
                dataSets.add(set1)
                set1.setmHoriDescrip(valueDescrip)
                set1.setHoriDesripColor(context.getColor(R.color.gray_transparent))
                set1.colors = colors
                set1.valueTextColor = App.instance.resources.getColor(R.color.white)
                set1.valueFormatter = yFormatter
                set1.valueTypeface = Typeface.createFromAsset(App.instance.assets, "OpenSans-Light.ttf")
                val data = BarData(dataSets)
                data.setValueTextSize(10f)
                data.setValueTypeface(Typeface.createFromAsset(App.instance.assets, "OpenSans-Light.ttf"))
                data.barWidth = barWidth
                setData(data)
            }
            setFitBars(true);
            animateY(2500);
            legend.isEnabled = false
        }
    }

    private fun formatHeartRate(min: Int, second: Int): String {
        var tem = ""
        if (min < 0x0A) {
            tem += "0$min"
        } else {
            tem += "$min"
        }
        tem += ":"
        if (second < 0x0A) {
            tem += "0$second"
        } else {
            tem += "$second"
        }
        return tem
    }
}

