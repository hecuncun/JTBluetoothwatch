package com.lhzw.kotlinmvp.base

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import com.amap.api.maps.MapView
import com.lhzw.bluetooth.base.BaseIView
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.mvp.presenter.MainSportPresenter
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        sliding_up_panel_layout.setPanelSlideListener(panelChangeAdapter)
        loadView()
        initView()
        mMapView = map
        mMapView?.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume();
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState);
    }

    private fun loadView() {
        panelViewList.add(SlidingUIPanelView(this))
        sliding_up_panel_layout.adapter = panelAdapter
    }

    private val panelAdapter = object : SlidingUpPanelLayout.Adapter() {
        override fun onCreateSlidingPanel(position: Int): ISlidingUpPanel<*> {
            val panel = panelViewList[position]
            panel.setFloor(mSize - position)
            panel.setPanelHeight(if (position == 0) dp2px(280) else dp2px(80))
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

    open fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                Resources.getSystem().displayMetrics).toInt()
    }

    private val panelChangeAdapter = object : SlidingUpPanelLayout.PanelSlideListenerAdapter() {
        override fun onPanelExpanded(panel: ISlidingUpPanel<*>?) {
            super.onPanelExpanded(panel)
        }

        override fun onPanelCollapsed(panel: ISlidingUpPanel<*>?) {
            super.onPanelCollapsed(panel)
        }
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
    }
}