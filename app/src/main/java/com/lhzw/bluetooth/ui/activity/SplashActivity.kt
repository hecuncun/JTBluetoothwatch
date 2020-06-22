package com.lhzw.bluetooth.ui.activity

import android.content.Intent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Created by hecuncun on 2019/11/12
 */
class SplashActivity : BaseActivity() {
    private var alphaAnimation: AlphaAnimation? = null

    override fun attachLayoutRes(): Int = R.layout.activity_splash
    override fun initData() {
    }

    override fun initView() {
        if (firest_login) {
            fadeIn(tv_login, 0.1f, 1.0f, 2300)
            tv_login.setOnClickListener {
                firest_login = false
                jumpToMain()
            }
        }
        alphaAnimation = AlphaAnimation(0.3F, 1.0F)
        alphaAnimation?.run {
            duration = 2000
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    if (!firest_login) {
                        jumpToMain()
                    }
                }

                override fun onAnimationStart(p0: Animation?) {
                }
            })
        }
        splash_view.startAnimation(alphaAnimation)
    }

    private fun jumpToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun initListener() {
    }

    private fun fadeIn(view: View, startAlpha: Float, endAlpha: Float, duration: Long) {
        if (view.visibility == View.VISIBLE) return
        view.visibility = View.VISIBLE
        val animation: Animation = AlphaAnimation(startAlpha, endAlpha)
        animation.duration = duration
        view.startAnimation(animation)
    }

    private fun fadeIn(view: View) {
        fadeIn(view, 0f, 1f, 400)
        view.isEnabled = true
    }

    private fun fadeOut(view: View) {
        if (view.visibility != View.VISIBLE) return
        view.isEnabled = false
        val animation: Animation = AlphaAnimation(1f, 0f)
        animation.duration = 400
        view.startAnimation(animation)
        view.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        alphaAnimation = null
    }

}