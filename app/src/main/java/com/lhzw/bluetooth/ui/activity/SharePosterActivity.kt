package com.lhzw.bluetooth.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.bean.ShareBgBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.glide.BlurBitmapUtil
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.Preference
import kotlinx.android.synthetic.main.activity_share_poster.*
import java.io.File
import java.text.SimpleDateFormat


/**
 *
@author：created by xtqb
@description:
@date : 2020/5/6 10:50
 *
 */
class SharePosterActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private var photoPath: String? by Preference(Constants.PHOTO_PATH, "")
    private val WX_QUEST = 0x0001
    private val QQ_QUEST = 0x0005
    private var path: String? = "/sdcard/share/xxxxxx.jpg"
    private var shareFile: File? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//使activity都竖屏
        setContentView(R.layout.activity_share_poster)
        initView()
        setListener()
        initData()
        initPhotoError()
    }

    private fun saveShareUI2Bitmap() {
        if(shareFile == null) {
            rl_share_poster.isDrawingCacheEnabled = true
            rl_share_poster.buildDrawingCache()
            val bitmap = rl_share_poster.getDrawingCache()
            shareFile = BaseUtils.saveBitmapFile(bitmap, path)!!
            rl_share_poster.destroyDrawingCache()
        }
    }

    private fun initPhotoError() {
        // android 7.0系统解决拍照的问题
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

    private fun initView() {
        tv_save_poster.paint.flags = Paint.UNDERLINE_TEXT_FLAG //下划线
        tv_save_poster.paint.isAntiAlias = true//抗锯齿
    }

    private fun setListener() {
        tv_save_poster.setOnClickListener(this)
        im_weixin.setOnClickListener(this)
        im_circle.setOnClickListener(this)
        im_qq.setOnClickListener(this)
        im_cancel.setOnClickListener(this)
        tv_save_poster.setOnTouchListener(this)
    }

    private fun initData() {
        GlideUtils.showCircleWithBorder(iv_head_photo, photoPath, R.drawable.pic_head, resources.getColor(R.color.white))
        val bg_bitmap = intent.getSerializableExtra("bg_bitmap") as ShareBgBean
        if (bg_bitmap.path == null || "".equals(bg_bitmap.path)) {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_share_default)
            var lenght = bitmap.width
            if (lenght > bitmap.height) {
                lenght = bitmap.height
            }
            var bg = BlurBitmapUtil.centerSquareScaleBitmap(bitmap, lenght)
            im_bg_share.setImageBitmap(bg)
        } else {
            val bitmap = BitmapFactory.decodeFile(bg_bitmap.path)
            var lenght = bitmap.width
            if (lenght > bitmap.height) {
                lenght = bitmap.height
            }
            val bg = BlurBitmapUtil.centerSquareScaleBitmap(bitmap, lenght)
            im_bg_share.setImageBitmap(bg)
        }
    }

    override fun onClick(v: View?) {
        saveShareUI2Bitmap()
        when (v?.id) {
            R.id.tv_save_poster -> {
                rl_share_poster.isDrawingCacheEnabled = true
                rl_share_poster.buildDrawingCache()
                val bmp = rl_share_poster.getDrawingCache()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                if (BaseUtils.savePicture(bmp, "${formatter.format(System.currentTimeMillis())}.jpg")) {
                    Toast.makeText(this, "保存成功!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "保存失败!", Toast.LENGTH_SHORT).show()
                }

                rl_share_poster.destroyDrawingCache()
                finish()
            }
            R.id.im_weixin -> {
                if (!BaseUtils.isAppInstall(this@SharePosterActivity, "com.tencent.mm")) {
                    Toast.makeText(this, "微信未安装", Toast.LENGTH_SHORT).show()
                    return
                }
                val send = Intent()
                send.setAction(Intent.ACTION_SEND)
                send.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
                send.setType("image/*");
                send.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");//微信朋友圈，仅支持分享图片
                startActivityForResult(send, WX_QUEST);
            }
            R.id.im_circle -> {
                if (!BaseUtils.isAppInstall(this@SharePosterActivity, "com.tencent.mm")) {
                    Toast.makeText(this, "微信未安装", Toast.LENGTH_SHORT).show()
                    return
                }
                val send = Intent()
                send.setAction(Intent.ACTION_SEND)
                send.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
                send.setType("image/*");
                send.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");//微信朋友圈，仅支持分享图片
                startActivityForResult(send, WX_QUEST);
            }
            R.id.im_qq -> {
                if (!BaseUtils.isAppInstall(this@SharePosterActivity, "com.tencent.mobileqq")) {
                    Toast.makeText(this, "QQ未安装", Toast.LENGTH_SHORT).show()
                    return
                }
                val send = Intent()
                send.setAction(Intent.ACTION_SEND)
                send.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
                send.setType("image/*");
                send.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");//微信朋友圈，仅支持分享图片
                startActivityForResult(send, WX_QUEST);
            }
            R.id.im_cancel -> {
                finish()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.tv_save_poster -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        tv_save_poster.setTextColor(getColor(R.color.white))
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        tv_save_poster.setTextColor(getColor(R.color.gray_little1))
                    }
                }

            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        shareFile?.let {
            if(it.isFile && it.exists()) {
                it.delete()
            }
        }
        shareFile = null
        path = null;
    }
}