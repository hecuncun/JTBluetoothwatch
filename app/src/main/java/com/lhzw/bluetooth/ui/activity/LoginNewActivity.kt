package com.lhzw.bluetooth.ui.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.UserInfo
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.ext.underline
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.LoadingView
import kotlinx.android.synthetic.main.activity_login_new.*
import java.text.SimpleDateFormat

/**
 * Created by heCunCun on 2020/7/31
 */
class LoginNewActivity:BaseActivity() {

    private var apk_update_time: String? by Preference(Constants.APK_UPDATE_TIME, "")
    private var apk_ip_change: Boolean? by Preference(Constants.APK_IP_CHANGE, false)
    override fun attachLayoutRes(): Int= R.layout.activity_login_new

    override fun initData() {
    }

    override fun initView() {
        tv_register.underline()
        if(!apk_ip_change!! && packageManager.getPackageInfo(packageName, 0).versionName == "v3.2.2"){
            http_token = ""
            apk_ip_change = true
            return
        }
        if ("" != http_token) {
            jumpToMain()
        }
    }

    override fun initListener() {
        tv_login.setOnClickListener {
            if (et_user_name.text.toString().trim().isNotEmpty() && et_pwd.text.toString().trim().isNotEmpty()){
                if (checkbox.isChecked){
                    login()
                }else{
                    showToast("请先勾选我已阅读并同意《疆泰APP用户协议》")
                }
            }else{
                showToast("请检查登录信息是否为空！")
            }
        }

        iv_clear_user_name.setOnClickListener {
            et_user_name.setText("")
        }

        iv_eye.setOnClickListener {
            switchPwdMode(et_pwd,iv_eye)
        }

        tv_register.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
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



    private var loadingView: LoadingView? = null
    private fun login() {
        if (loadingView == null) {
            loadingView = LoadingView(this)
        }
        loadingView?.setLoadingTitle("登录中...")
        loadingView?.show()
        val response = SLMRetrofit.getInstance().getApi()?.login(et_user_name.text.toString(), et_pwd.text.toString())
        response?.compose(ThreadSwitchTransformer<BaseBean<UserInfo>>())?.subscribe(object : CallbackListObserver<BaseBean<UserInfo>?>() {
            override fun onSucceed(bean: BaseBean<UserInfo>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        http_token = it.getData()?.getToken()
                        showToast("登录成功")
                        if ("" == apk_update_time) {// 说明第一次登陆软件
                            val sdf = SimpleDateFormat("yyyy年MM月dd日更新")
                            apk_update_time = sdf.format(System.currentTimeMillis())
                        }
                        jumpToMain()
                    } else {
                        showToast("${it.getMessage()}")
                        tv_user_name_tip.visibility= View.VISIBLE
                    }
                    if (loadingView != null && loadingView!!.isShowing) {
                        loadingView?.cancel()
                    }
                }
            }

            override fun onFailed() {
                showToast("登录失败")
                if (loadingView != null && loadingView!!.isShowing) {
                    loadingView?.cancel()
                }
            }
        })
    }
    private fun jumpToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}