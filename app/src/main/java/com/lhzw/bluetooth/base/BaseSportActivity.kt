package com.lhzw.kotlinmvp.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.amap.api.maps.MapView
import com.lhzw.bluetooth.base.BaseIView
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.mvp.presenter.MainSportPresenter
import com.lhzw.kotlinmvp.presenter.BaseSportPresenter
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        initView()
        mMapView = map
        mMapView?.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume();
    }

//    override fun onSaveInstanceState(outState: Bundle?) {
//        super.onSaveInstanceState(outState)
//        mMapView?.onSaveInstanceState(outState);
//    }

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