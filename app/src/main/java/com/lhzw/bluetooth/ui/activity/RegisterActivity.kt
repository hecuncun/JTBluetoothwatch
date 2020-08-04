package com.lhzw.bluetooth.ui.activity

import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.ext.showToast
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.checkbox
import kotlinx.android.synthetic.main.activity_register.et_pwd
import kotlinx.android.synthetic.main.activity_register.et_user_name
import kotlinx.android.synthetic.main.activity_register.iv_clear_user_name
import kotlinx.android.synthetic.main.activity_register.iv_eye

/**
 * Created by heCunCun on 2020/8/3
 */
class RegisterActivity:BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_register

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        iv_clear_user_name.setOnClickListener {
            et_user_name.setText("")
        }
        et_pwd.setOnClickListener {
            switchPwdMode(et_pwd,iv_eye)
        }

        et_pwd2.setOnClickListener {
            switchPwdMode(et_pwd2,iv_eye2)
        }

        tv_to_register.setOnClickListener {//去注册
            if (checkbox.isChecked){
                if (et_user_name.text.toString().trim().isNotEmpty() && et_pwd.text.toString().trim().isNotEmpty() && et_pwd2.text.toString().trim().isNotEmpty()){
                    if (et_user_name.text.toString().trim().length<6){
                        showToast("不小于6位密码,区分大小写")
                        return@setOnClickListener
                    }
                    if (et_pwd.text.toString().trim() == et_pwd2.text.toString().trim()){
                        tv_pwd_tip.visibility= View.INVISIBLE
                        //走注册接口
                        showToast("请求接口")
                    }else{
                        tv_pwd_tip.visibility= View.VISIBLE
                    }
                }else{
                    showToast("请检查注册信息是否完整!")
                }
            }else{
                showToast("请先勾选我已阅读并同意《疆泰APP用户协议》")
            }


        }
    }

    /**
     * 显示密码操作
     */
    private fun switchPwdMode(
            etPwd: EditText,
            btnPwdEye: ImageView
    ) {
        if (etPwd.text.toString().isEmpty()) {
            return
        }
        //是否已经显示了
        val showPwd = etPwd.transformationMethod != PasswordTransformationMethod.getInstance()
        if (showPwd) {
            //否则隐藏密码、
            etPwd.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnPwdEye.setImageResource(R.drawable.ic_login_eye_close)
            etPwd.transformationMethod = PasswordTransformationMethod.getInstance()
            //光标最后
            etPwd.setSelection(etPwd.text.toString().length)
        } else {
            btnPwdEye.setImageResource(R.drawable.ic_login_eye_open)
            etPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
            //光标最后
            etPwd.setSelection(etPwd.text.toString().length)
        }
    }
}