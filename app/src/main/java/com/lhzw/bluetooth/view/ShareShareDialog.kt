package com.lhzw.bluetooth.view

import android.app.Activity
import android.support.design.widget.BottomSheetDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.BaseUtils
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.dialog_child_share.*


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/26 16:42
 *
 */


class ShareShareDialog(private var mContext: Activity) : BottomSheetDialog(mContext), View.OnClickListener {
    init {
        var view = LayoutInflater.from(mContext).inflate(R.layout.dialog_share_sheet, null)
        view?.apply {
            setContentView(view)
            ll_qq.setOnClickListener(this@ShareShareDialog)
            ll_weibo.setOnClickListener(this@ShareShareDialog)
            ll_friends.setOnClickListener(this@ShareShareDialog)
            ll_weixin.setOnClickListener(this@ShareShareDialog)
            tv_share_cancel.setOnClickListener(this@ShareShareDialog)
        }
    }

    override fun onClick(v: View?) {
        var platform: SHARE_MEDIA? = null
        if (!BaseUtils.isNetworkConnected()) {
            Toast.makeText(mContext, mContext.getString(R.string.net_connect_error), Toast.LENGTH_LONG).show()
            return
        }
        when (v?.id) {
            R.id.ll_qq -> {
                if (!BaseUtils.isAppInstall(Constants.QQ)) {
                    Toast.makeText(mContext, mContext.getString(R.string.app_installed_error), Toast.LENGTH_LONG).show()
                    this.dismiss()
                    return
                }
                platform = SHARE_MEDIA.QQ
            }
            R.id.ll_friends -> {
                if (!BaseUtils.isAppInstall(Constants.WEIXIN)) {
                    Toast.makeText(mContext, mContext.getString(R.string.app_installed_error), Toast.LENGTH_LONG).show()
                    this.dismiss()
                    return
                }
                platform = SHARE_MEDIA.WEIXIN_CIRCLE
            }
            R.id.ll_weixin -> {
                if (!BaseUtils.isAppInstall(Constants.WEIXIN)) {
                    Toast.makeText(mContext, mContext.getString(R.string.app_installed_error), Toast.LENGTH_LONG).show()
                    this.dismiss()
                    return
                }
                platform = SHARE_MEDIA.WEIXIN
            }
            R.id.ll_weibo -> {
                if (!BaseUtils.isAppInstall(Constants.SINA)) {
                    Toast.makeText(mContext, mContext.getString(R.string.app_installed_error), Toast.LENGTH_LONG).show()
                    this.dismiss()
                    return
                }
                platform = SHARE_MEDIA.SINA
            }
            R.id.tv_share_cancel -> {

            }
        }
        shareContent(platform)
        this.dismiss()
    }

    private fun shareContent(platform: SHARE_MEDIA?) {
        //        ShareAction(mContext).setDisplayList(SHARE_MEDIA.WEIXIN).withText("hello").setCallback(shareListener).open()
        platform?.let {
            ShareAction(mContext).setPlatform(it).withText("hello").setCallback(shareListener).share()
        }

    }

    private val shareListener = object : UMShareListener {
        override fun onResult(share_media: SHARE_MEDIA?) {
            Log.e("Tag", "share complete")
        }

        override fun onCancel(share_media: SHARE_MEDIA?) {
            Log.e("Tag", "share onCancel")
        }

        override fun onError(share_media: SHARE_MEDIA?, throwable: Throwable?) {
            Log.e("Tag", "share onError")
        }

        override fun onStart(share_media: SHARE_MEDIA) {
            Log.e("Tag", "share onStart")
        }
    }

    fun showDialog() {
        this.show()
    }
}
