package com.lhzw.bluetooth.mvp.model

import android.util.Log
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.WatchInfoBean
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.FirmBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.mvp.contract.UpdateContract
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.DownloadTransformer
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import com.lhzw.bluetooth.net.rxnet.RxNet
import com.lhzw.bluetooth.net.rxnet.callback.DownloadCallback
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

/**
 * Date： 2020/7/6 0006
 * Time： 15:09
 * Created by xtqb.
 */
class UpdateModel : UpdateContract.IModel {
    override fun onDettach() {
        // 清空数据
    }

    override fun queryWatchData(): List<WatchInfoBean>? {
        return CommOperation.query(WatchInfoBean::class.java);
    }

    override fun getLatestApk(body: (apk: ApkBean?) -> Unit) {
        val response = SLMRetrofit.getInstance().getApi()?.getLatestApk(App.instance.packageName)
        response?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<MutableList<ApkBean>>?>() {
            override fun onSucceed(bean: BaseBean<MutableList<ApkBean>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        val beans = it.getData()
                        beans?.let {
                            body(beans[0])
                        }
                    }
                }
            }

            override fun onFailed() {
                body(null)
            }
        })
    }

    override fun getLatestFirm(body: (firm: FirmBean?) -> Unit) {
        val response = SLMRetrofit.getInstance().getApi()?.getLatestFirm(Constants.WATCH_TYPE)
        response?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<MutableList<FirmBean>>?>() {
            override fun onSucceed(bean: BaseBean<MutableList<FirmBean>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        val beans = it.getData()
                        beans?.let {
                            body(beans[0])
                        }
                    }
                }
            }

            override fun onFailed() {
                body(null)
            }
        })
    }

    override fun downloadApk(attachmentId: Long, body: (mResponse: Response<ResponseBody>?) -> Unit) {

        SLMRetrofit.getInstance().getApi()?.downloadApk(attachmentId)
                ?.compose(DownloadTransformer())
                ?.subscribe(object : Observer<Response<ResponseBody>> {
                    override fun onNext(response: Response<ResponseBody>) {
                        Log.e("downloadApk", "----------------------------------------------------")
                        body(response)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }

    override fun downloadDfu(attachmentId: Long, body: (mResponseBody: Response<ResponseBody>?) -> Unit) {
        SLMRetrofit.getInstance().getApi()?.downloadDfu(attachmentId)
                ?.compose(DownloadTransformer())
                ?.subscribe(object : Observer<Response<ResponseBody>> {
                    override fun onNext(response: Response<ResponseBody>) {
                        body(response)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }

    /**
     *
     */
    override fun dowloadFile(url: String, path: String, listener: DownloadCallback) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        RxNet().download(App.instance.getToken(), url, path, listener)
    }
}