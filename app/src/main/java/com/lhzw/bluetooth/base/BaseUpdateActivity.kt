package com.lhzw.bluetooth.base

import android.util.Log
import com.lhzw.bluetooth.mvp.contract.UpdateContract
import com.lhzw.bluetooth.mvp.presenter.MainUpdatePresenter
import com.lhzw.bluetooth.widget.LoadingView
import com.lhzw.kotlinmvp.presenter.BaseIPresenter

/**
 * Date： 2020/7/6 0006
 * Time： 15:04
 * Created by xtqb.
 */
abstract class BaseUpdateActivity<T : BaseIPresenter<UpdateContract.IView>> : BaseActivity(), UpdateContract.IView {
    protected var mPresenter: MainUpdatePresenter? = null
    private var loadingView: LoadingView? = null
    override fun initView() {
        mPresenter = getMainPresent() as MainUpdatePresenter
        mPresenter?.let {
            it.attachView(this)
            it?.onAttach()
        }
    }


    protected fun showLoadingView(note: String) {
        if (loadingView == null) {
            loadingView = LoadingView(this@BaseUpdateActivity)

        }
        loadingView?.setLoadingTitle(note)
        loadingView?.show()
    }

    protected fun cancelLoadingView() {
        loadingView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            loadingView = null
        }
        mPresenter?.let {
            mPresenter = null
        }
    }

    abstract fun getMainPresent(): T?
}