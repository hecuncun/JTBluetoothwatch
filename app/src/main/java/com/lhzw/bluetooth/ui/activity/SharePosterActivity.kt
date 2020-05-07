package com.lhzw.bluetooth.ui.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.bean.ShareBgBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.glide.BlurBitmapUtil
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.uitls.Preference
import kotlinx.android.synthetic.main.activity_share_poster.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//使activity都竖屏
        setContentView(R.layout.activity_share_poster)
        initView()
        setListener()
        initData()
    }

    private fun initView() {

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
        when (v?.id) {
            R.id.tv_save_poster -> {
                rl_share_poster.isDrawingCacheEnabled = true
                rl_share_poster.buildDrawingCache()
                val bmp = rl_share_poster.getDrawingCache()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                savePicture(bmp, "${formatter.format(System.currentTimeMillis())}.jpg")
                rl_share_poster.destroyDrawingCache()
                finish()
            }
            R.id.im_weixin -> {

            }
            R.id.im_circle -> {

            }
            R.id.im_qq -> {

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

    fun savePicture(bm: Bitmap?, fileName: String?) {
        Log.i("xing", "savePicture: ------------------------")
        if (null == bm) {
            Log.i("xing", "savePicture: ------------------图片为空------")
            return
        }
        val foder = File(Environment.getExternalStorageDirectory().absolutePath.toString() + "/share")
        if (!foder.exists()) {
            foder.mkdirs()
        }
        val myCaptureFile = File(foder, fileName)
        try {
            if (!myCaptureFile.exists()) {
                myCaptureFile.createNewFile()
            }
            val bos = BufferedOutputStream(FileOutputStream(myCaptureFile))
            //压缩保存到本地
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(this, "保存成功!", Toast.LENGTH_SHORT).show()
    }
}