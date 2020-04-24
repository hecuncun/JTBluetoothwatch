package com.lhzw.bluetooth.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseMvpFragment
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.SaveUserEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.mvp.contract.SettingContract
import com.lhzw.bluetooth.mvp.presenter.SettingPresenter
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.view.EditNameDialog
import com.lhzw.bluetooth.view.SelectDialog
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_setting.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal


/**
 * Created by hecuncun
 */
class SettingFragment : BaseMvpFragment<SettingContract.View, SettingContract.Presenter>(), SettingContract.View {

    private var photoPath: String? by Preference(Constants.PHOTO_PATH, "")
    private var birthday: String? by Preference(Constants.BIRTHDAY, "")
    private var nickName: String? by Preference(Constants.NICK_NAME, "用户昵称")

    private var enablePhone: Boolean by Preference(Constants.TYPE_PHONE, true)
    private var enableMsg: Boolean by Preference(Constants.TYPE_MSG, true)
    private var enableQQ: Boolean by Preference(Constants.TYPE_QQ, true)
    private var enableWx: Boolean by Preference(Constants.TYPE_WX, true)
    override fun useEventBus()=true

    companion object {
        fun getInstance(): SettingFragment = SettingFragment()
    }

    override fun createPresenter(): SettingContract.Presenter = SettingPresenter()

    override fun attachLayoutRes(): Int = R.layout.fragment_setting

    override fun lazyLoad() {
        //初始化数据
        mPresenter?.getPersonalInfo()
    }

    override fun getPersonalInfoSuccess(data: PersonalInfoBean?) {
        //设置数据
        if (data != null) {
            if (data.gender == 1) {
                rg_btn_man.isChecked = true
            } else {
                rg_btn_women.isChecked = true
            }
          //  Logger.e("显示个人信息身高=="+ data.height)

            counter_height.initNum = data.height
            et_weight.setText(data.weight.toString())
            counter_step_length.initNum=data.step_len
            et_target_step_num.setText(data.des_steps.toString())
            et_target_cal_num.setText(data.des_calorie.toString())
            tv_name.text=nickName

            et_target_distance_num.setText(data.des_distance.toString())
            counter_max_heart.initNum=data.heart_rate
            tv_birthday.text=if (birthday!!.isEmpty()) "请选择 > " else  birthday

            nuan_shen.text = "区间[${data.heart_rate.times(0.5).toInt()}-${data.heart_rate.times(0.6).toInt() - 1}]"
            ran_zhi.text = "区间[${data.heart_rate.times(0.6).toInt()}-${data.heart_rate.times(0.7).toInt() - 1}]"
            you_yang.text = "区间[${data.heart_rate.times(0.7).toInt()}-${data.heart_rate.times(0.8).toInt() - 1}]"
            ru_suan.text = "区间[${data.heart_rate.times(0.8).toInt()}-${data.heart_rate.times(0.9).toInt() - 1}]"
            wu_yang.text = "区间[${data.heart_rate.times(0.9).toInt()}-${data.heart_rate}]"
        }


    }

    override fun initView(view: View) {
        super.initView(view)
        GlideUtils.showCircleWithBorder(iv_head_photo, photoPath, R.drawable.pic_head, resources.getColor(R.color.white))
        initListener()

        initIvState()


    }
//初始化消息接收状态
    private fun initIvState() {
        iv_phone.setImageResource(if (enablePhone) R.drawable.icon_phone else R.drawable.icon_phone_normal )
        iv_msg.setImageResource(if (enableMsg) R.drawable.icon_msg else R.drawable.icon_msg_normal )
        iv_qq.setImageResource(if (enableQQ) R.drawable.icon_qq else R.drawable.icon_qq_normal )
        iv_wx.setImageResource(if (enableWx) R.drawable.icon_wx else R.drawable.icon_wx_normal )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun saveUser(event: SaveUserEvent) {
        //获取所有的设置信息,并保存
        val gender = if (rg_sex.checkedRadioButtonId == R.id.rg_btn_man) 1 else 0
        val height = counter_height.initNum
        val weight = et_weight.text.toString().toInt()
        val step_len = counter_step_length.initNum
        val des_steps = et_target_step_num.text.toString().toInt()
        val des_calorie = et_target_cal_num.text.toString().toInt()
        val des_distance = et_target_distance_num.text.toString().toInt()
        val heart_rate = counter_max_heart.initNum

        val personalInfoBean = PersonalInfoBean("9", gender, age, height, weight, step_len, des_steps, des_calorie, des_distance, heart_rate)

        Logger.e(personalInfoBean.toString())
        //先删除所有的bean对象再去添加
        if (connectState) {
            //已连接才能保存
            LitePal.deleteAll(PersonalInfoBean::class.java)
            tv_birthday.text=birthday
            personalInfoBean.save()

            //计算显示心率区间
            nuan_shen.text = "区间[${heart_rate.times(0.5).toInt()}-${heart_rate.times(0.6).toInt() - 1}]"
            ran_zhi.text = "区间[${heart_rate.times(0.6).toInt()}-${heart_rate.times(0.7).toInt() - 1}]"
            you_yang.text = "区间[${heart_rate.times(0.7).toInt()}-${heart_rate.times(0.8).toInt() - 1}]"
            ru_suan.text = "区间[${heart_rate.times(0.8).toInt()}-${heart_rate.times(0.9).toInt() - 1}]"
            wu_yang.text = "区间[${heart_rate.times(0.9).toInt()}-${heart_rate}]"
            //发指令更新个人信息
            RxBus.getInstance().post("updatePersonInfo", "")
        } else {
            //请先连接手表后保存
            showToast("请先连接手表")
        }


    }

    private var age: Int = 25

    private fun initListener() {
        //头像选择
        val dialog = SelectDialog(activity)
        //日期选择
        val dialogTime = TimePickerDialog.Builder()
                .setCallBack { _, millseconds ->
                    val dateString = DateUtils.longToString(millseconds, "yyyy年MM月dd日")
                    tv_birthday.text = dateString
                    val birthYear = DateUtils.longToString(millseconds, "yyyy").toInt()
                    val nowYear = DateUtils.longToString(System.currentTimeMillis(), "yyyy").toInt()
                    age = nowYear - birthYear
                }
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId("生日")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis() - 100L * 365 * 1000 * 60 * 60 * 24L)
                .setMaxMillseconds(System.currentTimeMillis())
                .setCurrentMillseconds(System.currentTimeMillis() - 25L * 365 * 1000 * 60 * 60 * 24L)
                .setThemeColor(resources.getColor(R.color.orange))
                .setType(Type.YEAR_MONTH_DAY)
                .setWheelItemTextNormalColor(resources.getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(resources.getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(12)
                .build()


        dialog.setOnChoseListener { resId ->
            when (resId) {
                R.id.tv_camera -> selectImage(0)
                R.id.tv_photos -> selectImage(1)
                else -> {

                }
            }
        }
        iv_head_photo.setOnClickListener {
            //选择相册或者拍照
            dialog.show()
        }

        tv_name.setOnClickListener {
            //弹窗编辑昵称
            val dialog = EditNameDialog(activity!!)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setOnConfirmListener(object : EditNameDialog.OnConfirmListener {
                override fun onConfirm(name: String) {
                    dialog.dismiss()
                    nickName = name
                    tv_name.text=nickName
                }
            })
        }

        tv_birthday.setOnClickListener {
            dialogTime.show(fragmentManager, "a")

        }

        //消息通知开关
        iv_phone.setOnClickListener {
           enablePhone=!enablePhone
            initIvState()
        }

        iv_msg.setOnClickListener {
            enableMsg =!enableMsg
            initIvState()
        }

        iv_qq.setOnClickListener {
            enableQQ = !enableQQ
            initIvState()
        }

        iv_wx.setOnClickListener {
            enableWx =!enableWx
            initIvState()
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    if (selectList.size > 0) {
                        GlideUtils.showCircleWithBorder(iv_head_photo, selectList[0].compressPath, R.drawable.icon_head_photo, resources.getColor(R.color.white))
                        //保存头像地址
                        photoPath = selectList[0].compressPath
                    } else {
                        showToast("图片出现问题")
                    }
                }
            }
        }
    }

    private fun selectImage(i: Int) {
        if (i == 0) {
            PictureSelector.create(this@SettingFragment)
                    .openCamera(PictureMimeType.ofImage())
                    .enableCrop(true)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .withAspectRatio(3, 2)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .circleDimmedLayer(false)// 是否圆形裁剪 true or false
                    .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                    .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                    .minimumCompressSize(200)// 小于100kb的图片不压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
                    .scaleEnabled(true)// 裁剪是否可放大缩小图片 true or false
                    .isDragFrame(false)// 是否可拖动裁剪框(固定)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
        } else {
            PictureSelector.create(this@SettingFragment)
                    .openGallery(PictureMimeType.ofImage()) //全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .maxSelectNum(1)// 最大图片选择数量 int
                    .imageSpanCount(3)
                    .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                    .previewImage(true)// 是否可预览图片 true or false
                    .isCamera(true)// 是否显示拍照按钮 true or false
                    .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .enableCrop(true)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .withAspectRatio(3, 2)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .circleDimmedLayer(false)// 是否圆形裁剪 true or false
                    .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                    .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                    .minimumCompressSize(200)// 小于100kb的图片不压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
                    .scaleEnabled(true).// 裁剪是否可放大缩小图片 true or false
                            isDragFrame(false).// 是否可拖动裁剪框(固定)
                            forResult(PictureConfig.CHOOSE_REQUEST)
        }
    }

}