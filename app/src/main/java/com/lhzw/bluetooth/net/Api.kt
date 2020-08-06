package com.lhzw.bluetooth.net

import com.lhzw.bluetooth.bean.net.*
import io.reactivex.Observable;
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Date： 2020/6/2 0002
 * Time： 10:06
 * Created by xtqb.
 */

interface Api {
    /**
     * 登录
     */
    @GET("security/login")
    fun login(@Query("loginName") loginName: String, @Query("password") password: String): Observable<BaseBean<UserInfo>>

    /**
     * 获取最新 apk 信息
     */
    @GET("apks/latest")
    fun getLatestApk(@Query("packageName") packageName: String): Observable<BaseBean<MutableList<ApkBean>>>

    /**
     * apk 下载
     */
    @GET("attachments/apks/{id}")
    fun downloadApk(@Path("id") id: Long): Observable<Response<ResponseBody>>

    /**
     * 获取最新腕表固件
     */
    @GET("firmware/latest")
    fun getLatestFirm(@Query("model") model: String): Observable<BaseBean<MutableList<FirmBean>>>

    /**
     * 腕表固件 下载
     */
    @GET("attachments/firms/{id}")
    fun downloadDfu(@Path("id") id: Long): Observable<Response<ResponseBody>>

    /**
     * 注册用户信息
     */
    @POST(value = "security/insert/bean")
    fun insertUser(@Body user: LoginUser): Observable<BaseBean<String>>

}
