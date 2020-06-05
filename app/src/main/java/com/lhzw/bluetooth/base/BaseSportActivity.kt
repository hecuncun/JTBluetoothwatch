package com.lhzw.kotlinmvp.base

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.amap.api.maps.MapView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseIView
import com.lhzw.bluetooth.constants.ShareBgBitmap
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.mvp.presenter.MainSportPresenter
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.view.panel.base.BaseSlidingUIPanelView
import com.lhzw.dmotest.SlidingUIPanelView
import com.lhzw.kotlinmvp.presenter.BaseSportPresenter
import com.xw.repo.supl.ISlidingUpPanel
import com.xw.repo.supl.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_sport_info.*

/**
 *
@author：created by xtqb
@description: 实现Activity基类
@date : 2019/11/12 10:02
 *
 */

abstract class BaseSportActivity<T : BaseSportPresenter<SportConstract.View>> : AppCompatActivity(), BaseIView {
    protected var mPresenter: MainSportPresenter? = null
    protected var mMapView: MapView? = null
    protected val panelViewList = ArrayList<BaseSlidingUIPanelView>()
    protected val mSize = 1
    protected var convertView: View? = null
    protected var scBitmapShot: Bitmap? = null  // 截屏
    protected var scBitmapMap: Bitmap? = null   // 原始
    protected var scMapShotPath = "/sdcard/share/sport_gaode_map_shot.jpg"
    private var backCounter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        sliding_up_panel_layout.setPanelSlideListener(panelChangeAdapter)
        loadView()
        initView()
        mMapView = map
        mMapView?.onCreate(savedInstanceState)
        initPhotoError()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume();
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState);
    }

    private fun initPhotoError() {
        // android 7.0系统解决拍照的问题
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

    private fun loadView() {
        val panel = SlidingUIPanelView(this)
        convertView = panel.getConvertView()
        panelViewList.add(panel)
        sliding_up_panel_layout.adapter = panelAdapter
        convertView?.findViewById<TextView>(R.id.tv_share_poster)?.setOnClickListener {
            if(scBitmapMap == null) {
                showToast("没有轨迹")
                return@setOnClickListener
            }
            ShareBgBitmap.bg_bitmap = scBitmapShot
            mPresenter?.startShareActivity(this@BaseSportActivity, scMapShotPath)
        }
    }

    private val panelAdapter = object : SlidingUpPanelLayout.Adapter() {
        override fun onCreateSlidingPanel(position: Int): ISlidingUpPanel<*> {
            val panel = panelViewList[position]
            panel.setFloor(mSize - position)
            var height = BaseUtils.dip2px(280)
            if (!BaseUtils.hasNavBar(this@BaseSportActivity)) {
                height -= 150
            }
            panel.setPanelHeight(if (position == 0) height else dp2px(80))
            panel.slideState = SlidingUpPanelLayout.COLLAPSED
            return panel
        }

        override fun getItemCount(): Int {
            return panelViewList.size
        }

        override fun onBindView(panel: ISlidingUpPanel<*>?, position: Int) {
            val panel = panel as BaseSlidingUIPanelView
            panel.isClickable = false
            panel.setOnClickListener(View.OnClickListener {
                if (panel.slideState != SlidingUpPanelLayout.EXPANDED) {
                    sliding_up_panel_layout.expandPanel()
                } else {
                    sliding_up_panel_layout.collapsePanel()
                }
            })
        }
    }

    protected fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                Resources.getSystem().displayMetrics).toInt()
    }

    private val panelChangeAdapter = object : SlidingUpPanelLayout.PanelSlideListenerAdapter() {
        override fun onPanelExpanded(panel: ISlidingUpPanel<*>?) {
            super.onPanelExpanded(panel)
            Log.e("panelChangeAdapter", "onPanelExpanded  -------------------- ${panel?.slideState}")
        }

        override fun onPanelCollapsed(panel: ISlidingUpPanel<*>?) {
            super.onPanelCollapsed(panel)
            Log.e("panelChangeAdapter", "onPanelCollapsed  -------------------- ${panel?.slideState}")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!getAnimationState()) {
                return false
            }

            if (panelViewList[0].slideState == SlidingUpPanelLayout.EXPANDED) {
                sliding_up_panel_layout.collapsePanel()
                return false
            }
            backCounter++
            if (backCounter == 2) {
                this.finish()
                return false
            }
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ backCounter = 0 }, 2000)
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    protected fun getAnimationState(): Boolean {
        mPresenter?.also {
            if (!it.getAnimationState()) {
                Toast.makeText(this, "等待动画结束", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    /**
     *  设置布局
     */

    abstract fun getLayoutId(): Int

    /**
     * 初始化视图
     */
    abstract fun initView()

    override fun onPause() {
        super.onPause()
        mMapView?.onPause();
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.apply {
            detachView()
            mPresenter = null
        }
        mMapView?.apply {
            onDestroy()
            mMapView = null
        }
        convertView = null

        scBitmapShot = null

        scBitmapMap = null
    }
}