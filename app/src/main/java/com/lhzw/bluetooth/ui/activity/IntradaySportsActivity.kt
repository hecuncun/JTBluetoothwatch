package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.SportAdapter
import com.lhzw.bluetooth.base.BaseShareActivity
import com.lhzw.bluetooth.bean.ShareBgBean
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.BitmapUtil
import com.makeramen.roundedimageview.RoundedDrawable
import kotlinx.android.synthetic.main.activity_intraday_ports.*


/**
 *
@author：created by xtqb
@description:
@date : 2020/4/29 11:09
 *
 */
class IntradaySportsActivity : BaseShareActivity(), View.OnClickListener {
    private val PICK_PHOTO = 0x00102

    override fun attachLayoutRes(): Int {
        return R.layout.activity_intraday_ports
    }

    override fun initData() {
        //拿到初始图
        sharBean = ShareBgBean(R.drawable.icon_share_default, null)
        val bg = BitmapFactory.decodeResource(resources, R.drawable.icon_share_default)
        //处理得到模糊效果的图
        val blurBitmap = blurBitmap(this, bg, 20f);
        im_background.setImageBitmap(blurBitmap);
        GlideUtils.showCircleWithBorder(iv_head_photo, photoPath, R.drawable.pic_head, resources.getColor(R.color.white))

        // 获取 当天的活动
        Log.e("ShareSport", "${BaseUtils.getCurrentData()}")
        val adapter = SportAdapter(translateSportBeans())
        adapter?.openLoadAnimation { view ->
            arrayOf(ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f, 1.0f), ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f, 1.0f))
        }
        adapter?.setOnItemClickListener { _, view, position ->
//            body(view, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


    }

    override fun initView() {

    }

    override fun initListener() {
        im_replace.setOnClickListener(this)
        im_back.setOnClickListener(this)
        im_share.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.im_replace -> {
                    requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0x00099)
                }
                R.id.im_share -> {
                    val intent = Intent(this, SharePosterActivity::class.java)
                    intent.putExtra("bg_bitmap", sharBean)
                    startActivity(intent)
                }
                R.id.im_back -> {
                    this.finish()
                }
                else -> {
                }
            }
        }
    }

    override fun permissionSuccess(requestCode: Int) {
        if (requestCode == 0x00099) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            //Intent.ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT"
            intent.setType("image/*")
            startActivityForResult(intent, PICK_PHOTO) // 打开相册
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    var imagePath: String? = null
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        imagePath = handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        imagePath = handleImageBeforeKitKat(data);
                    }
                    Log.e("ImagePath", "resultCode : ${resultCode} , ${imagePath}")
                    displayImage(imagePath)
                }
            }
        }
    }

    private fun displayImage(path: String?) {
        if (path != null) {
            Log.e("ImagePath", "path : $path")
            sharBean?.path = path
            val bg = BitmapFactory.decodeFile(path)
            val blurBitmap = blurBitmap(this, bg, 20f);
            im_background.setImageBitmap(blurBitmap);
        } else {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    protected fun divImage(bitmap: Bitmap?) {
        //得到模糊图片
        var overlay: Bitmap? = bitmap
        if (null != overlay) {
            im_background.setImageBitmap(overlay)
        }
        //将old_image对象转化为bitmap对象
        im_background.buildDrawingCache()
        var mBitmap: Bitmap = im_background.getDrawingCache()
        mBitmap = (im_background.getDrawable() as RoundedDrawable).sourceBitmap
        //这两个数字是控制模糊度的
        val scaleFactor = 10.0f
        val radius = 10.0f
        val width = mBitmap.getWidth()
        val height = mBitmap.getHeight()
        overlay = Bitmap.createBitmap((width / scaleFactor).toInt(), (height / scaleFactor).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(overlay)
        canvas.scale(1 / scaleFactor, 1 / scaleFactor)
        val paint = Paint()
        paint.setFlags(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, paint!!)
        overlay = BitmapUtil.doBlur(overlay, radius.toInt(), true);
        im_background.setImageBitmap(overlay);
    }

}