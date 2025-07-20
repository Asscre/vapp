package com.virtualspace.app.network;

import com.virtualspace.app.network.model.VersionInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * API服务接口
 */
public interface ApiService {
    
    /**
     * 检查版本更新
     * @param currentVersion 当前版本号
     * @return 版本信息
     */
    @GET("api/version/check")
    Call<VersionInfo> checkVersion(@Query("version") int currentVersion);
    
    /**
     * 下载APK文件
     * @param url 下载地址
     * @return 响应体
     */
    @GET
    @Streaming
    Call<ResponseBody> downloadApk(@Url String url);
    
    /**
     * 获取应用信息
     * @param packageName 包名
     * @return 应用信息
     */
    @GET("api/app/info/{packageName}")
    Call<Object> getAppInfo(@Path("packageName") String packageName);
    
    /**
     * 上报错误日志
     * @param errorLog 错误日志
     * @return 上报结果
     */
    @GET("api/log/error")
    Call<Object> reportError(@Query("log") String errorLog);
} 