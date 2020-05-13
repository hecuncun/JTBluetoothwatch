package com.lhzw.bluetooth.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.mvp.presenter.MainSportPresenter
import com.lhzw.bluetooth.uitls.BitmapUtil
import com.lhzw.bluetooth.uitls.ShareUtils
import com.lhzw.kotlinmvp.base.BaseSportActivity
import com.orhanobut.logger.Logger
import com.umeng.socialize.UMShareAPI
import com.xw.repo.supl.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_sport_info.*
import kotlinx.android.synthetic.main.toolbar.*


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/18 15:25
 *
 */

class SportInfoActivity : BaseSportActivity<MainSportPresenter>(), SportConstract.View, AMap.OnMapClickListener {
    private var aMap: AMap? = null
    override fun getLayoutId(): Int {
        return com.lhzw.bluetooth.R.layout.activity_sport_info
    }

    override fun initView() {
//        CommonUtil.setMaxAspect(this)
        initTileBar()
        // 设置高度
//        val params = rl_map.layoutParams
//        var bar = findViewById<LinearLayout>(com.lhzw.bluetooth.R.id.toolbar)
//        //  实际高度   =         屏幕高度                       -        标题栏高度  -     状态栏高度                               -  虚拟键盘高度
//        params.height = resources.displayMetrics.heightPixels - bar.measuredHeight - CommonUtil.getStatusBarHeight(this) - CommonUtil.getNavigationBarHeight(this)
//        rl_map.layoutParams = params
        // 界面数据适配
        val mark = intent.getStringExtra("mark")
        val type = intent.getIntExtra("type", 0)
        val duration = intent.getStringExtra("duration")
        mPresenter = MainSportPresenter(mark, "${duration}", type)
        Log.e("Tag", "mPresenter == null ? ${mPresenter == null}")
        mPresenter?.apply {
            attachView(this@SportInfoActivity)
            initChart(this@SportInfoActivity, convertView!!)
            initView(this@SportInfoActivity, convertView!!)
            if (requirePermission(this@SportInfoActivity)) {
                aMap = initMap(mMapView)
                aMap?.setOnMapClickListener(this@SportInfoActivity)
            }
        }
    }

    private fun initTileBar() {
        toolbar_title.text = intent.getStringExtra("ymt")
        im_back.visibility = View.VISIBLE
        im_back.setOnClickListener {
            finish()
        }
        toolbar_right_img.visibility = View.VISIBLE
        toolbar_right_img.setBackgroundResource(R.drawable.icon_share_def)
        toolbar_right_img.setOnClickListener {
            val bitmap = BitmapUtil.shotScrollView(convertView?.findViewById(R.id.scrollview))
            Logger.e("bitmap=$bitmap")
            val uri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, null, null))
            ShareUtils.shareImage(this, uri, "分享到")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("Tag", "requestCode  $requestCode")
        when (requestCode) {
            Constants.REQUESTCODE -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    aMap = mPresenter?.initMap(mMapView!!)
                    aMap?.setOnMapClickListener(this@SportInfoActivity)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
    }

    // 刷新view
    override fun reflesh() {

    }

    override fun onDestroy() {
        super.onDestroy()
        aMap?.apply {
            aMap = null
        }
    }

    override fun onMapClick(latLgt: LatLng?) {
        Log.e("onMap", "onClick ....")
        if (panelViewList[0].slideState == SlidingUpPanelLayout.COLLAPSED) {
            sliding_up_panel_layout.hiddedPanel()
        } else if (panelViewList[0].slideState == SlidingUpPanelLayout.HIDDEN) {
            sliding_up_panel_layout.collapsePanel()
        }
    }
}