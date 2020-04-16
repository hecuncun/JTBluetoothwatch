package com.lhzw.bluetooth.ui.fragment

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
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
import com.lhzw.bluetooth.bean.CurrentDataBean
import com.lhzw.bluetooth.bean.DailyInfoDataBean
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.*
import com.lhzw.bluetooth.ui.activity.DailyStatisticsActivity
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.XAxisValueFormatter
import com.lhzw.bluetooth.widget.XYMarkerView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.findAll


/**
 * Created by hecuncun on 2019/11/13
 */
class HomeFragment : BaseFragment() {
    private var bleManager: BluetoothManager? = null
    private var state = false
    override fun useEventBus() = true

    companion object {
        fun getInstance(): HomeFragment = HomeFragment()
    }

    override fun attachLayoutRes(): Int = R.layout.fragment_home

    override fun initView(view: View) {
        rl_step_num_container.setOnClickListener {
            jumpToDailyStatisticsActivity()
        }
        bleManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleManager?.let {
            state = it.adapter.isEnabled//拿到蓝牙状态
        }
        if (connectState) {//已连接就隐藏提示
            tv_ble_state_tip.visibility = View.GONE
        } else {
            tv_ble_state_tip.text = if (state) {
                "在连接中添加设备"
            } else "蓝牙关闭"
        }

        initBarChart(bar_chart_step, resources.getColor(R.color.blue_light))

        initBarChart(bar_chart_cal, resources.getColor(R.color.orange))
    }

    private fun initBarChart(barChat: BarChart, mainColor: Int) {
        val times = arrayOf("1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00",
                "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "24:00")

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
            setMaxVisibleValueCount(24)//最大显示24个值
            setPinchZoom(false)
            setDrawBarShadow(false)
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
                axisLineColor = resources.getColor(R.color.colorPrimary)
                setLabelCount(5, true)//设置标签个数,true为精确
            }

            axisLeft.apply {
                isEnabled = true
                setDrawLabels(false)//标签
                setDrawAxisLine(false)//轴线
                setDrawGridLines(true)//网格线
                gridColor = resources.getColor(R.color.gray)
                axisMinimum = 0f
                enableGridDashedLine(20f, 5f, 0f)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            val mv = XYMarkerView(activity, XAxisValueFormatter(times))
            mv.chartView = barChat
            marker = mv
        }

    }


    private fun jumpToDailyStatisticsActivity() {
        val intent = Intent(activity, DailyStatisticsActivity::class.java)
        startActivity(intent)
    }

    override fun lazyLoad() {

        if (connectState){
            //处于连接状态,就显示数据
            initWatchData()
            refresh(RefreshEvent(Constants.TYPE_CURRENT_DATA))
            tv_ble_state_tip.visibility = View.GONE
            rl_step_num_container.background = resources.getDrawable(R.mipmap.icon_colorful_bg)
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
            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.barWidth = 0.3f
            barChat.data = data
            barChat.setFitBars(true) // 在bar开头结尾两边添加一般bar宽的留白
        }
        barChat.invalidate()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBleStateChanged(event: BleStateEvent) {
        if (event.state) {//打开
            tv_ble_state_tip.text = "在连接中添加设备"
        } else {//关闭
            tv_ble_state_tip.text = "蓝牙关闭"
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchConnectChanged(event: ConnectEvent) {
        if (event.isConnected) {
            tv_ble_state_tip.visibility = View.GONE
            rl_step_num_container.background = resources.getDrawable(R.mipmap.icon_colorful_bg)
            //已连接成功后再显示数据



        } else {
            tv_ble_state_tip.visibility = View.VISIBLE
            rl_step_num_container.background = resources.getDrawable(R.mipmap.icon_black_circle_bg)
            tv_current_step_num.text = "--"
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideDialog(event: HideDialogEvent){
        if (event.success){
            initWatchData()
        }

    }



    //连接成功后初始化手表数据
    private fun initWatchData() {
        refreshTargetSteps(RefreshTargetStepsEvent())
        //查询当天步数,cal
        val currentList = LitePal.findAll<CurrentDataBean>()
        if (currentList.isNotEmpty()) {
            tv_step_num_total.text = (currentList[0].dailyStepNumTotal+currentList[0].sportStepNumTotal).toString()
            tv_cal_total.text = (currentList[0].dailyCalTotal+currentList[0].sportCalTotal).toString()
        }
        //获取当天24小时的信息list
        val dateNow = DateUtils.longToString(System.currentTimeMillis(), "yyyy-MM-dd")
        val list = dateNow.split("-")
        val sb = StringBuilder()
        val year = list[0].substring(2, 4).toInt().toString()
        val month = list[1].toInt().toString()
        val day = list[2].toInt().toString()
        val daily_date = sb.append(year).append("-").append(month).append("-").append(day).trim().toString()//20-1-18
        //24小时数据
        val dailyInfoList = LitePal.where("daily_date = ?", daily_date).find(DailyInfoDataBean::class.java)
        //初始化24小时步数图表的值
        if (dailyInfoList.isNotEmpty()){
            val stepValues = ArrayList<BarEntry>()
            for (i in 0..23) {
                //val num = (Math.random() * 1000).toFloat()
                val num = dailyInfoList[i].daily_steps.toFloat()+dailyInfoList[i].sport_steps.toFloat()
                stepValues.add(BarEntry(i.toFloat(), num))
            }
            //初始化24小时cal表的值
            val calValues = ArrayList<BarEntry>()
            for (i in 0..23) {
                //val num = (Math.random() * 1000).toFloat()
                val num = dailyInfoList[i].daily_calorie.toFloat()+dailyInfoList[i].sport_calorie.toFloat()
                calValues.add(BarEntry(i.toFloat(), num))
            }
            initBarData(bar_chart_step, stepValues, resources.getColor(R.color.blue_light), resources.getColor(R.color.green_circle))
            initBarData(bar_chart_cal, calValues, resources.getColor(R.color.orange_light), resources.getColor(R.color.orange))
        }


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: RefreshEvent) {
        when (event.type) {
            Constants.TYPE_CURRENT_DATA -> {
                val list = LitePal.findAll(CurrentDataBean::class.java)
                if (list.isNotEmpty()) {
                    //当前步数
                    //Logger.e("当前步数更新==${list[0].dailyStepNumTotal.toString()}")
                    tv_current_step_num.text = (list[0].dailyStepNumTotal+ list[0].sportStepNumTotal).toString()
                }

            }


        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshTargetSteps(event: RefreshTargetStepsEvent) {
        val list = LitePal.findAll<PersonalInfoBean>()
        if (list.isNotEmpty()) {
            tv_goal_step_num.text = list[0].des_steps.toString()
        }
       // showToast("成功")

    }

}
