package com.lhzw.bluetooth.net

import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.UserInfo
import io.reactivex.Observable;
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Date： 2020/6/2 0002
 * Time： 10:06
 * Created by xtqb.
 */

interface Api {
    @GET("security/login")
    fun login(@Query("loginName") loginName: String, @Query("password") password: String): Observable<BaseBean<UserInfo>>
}