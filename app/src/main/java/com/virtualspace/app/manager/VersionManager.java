package com.virtualspace.app.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.virtualspace.app.BuildConfig;
import com.virtualspace.app.network.ApiService;
import com.virtualspace.app.network.model.VersionInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 版本管理器
 * 负责版本检查、自动更新和更新安装
 */
public class VersionManager {
    private static final String TAG = "VersionManager";
    private static final String UPDATE_DIR = "updates";
    private static final String APK_NAME = "VirtualSpaceApp.apk";
    
    private Context mContext;
    private ApiService mApiService;
    private VersionUpdateListener mUpdateListener;
    
    public interface VersionUpdateListener {
        void onUpdateAvailable(VersionInfo versionInfo);
        void onUpdateProgress(int progress);
        void onUpdateComplete(File apkFile);
        void onUpdateError(String error);
    }
    
    public VersionManager(Context context, ApiService apiService) {
        this.mContext = context;
        this.mApiService = apiService;
    }
    
    /**
     * 检查版本更新
     */
    public void checkForUpdate(VersionUpdateListener listener) {
        this.mUpdateListener = listener;
        
        mApiService.checkVersion(getCurrentVersionCode()).enqueue(new Callback<VersionInfo>() {
            @Override
            public void onResponse(Call<VersionInfo> call, Response<VersionInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VersionInfo versionInfo = response.body();
                    if (versionInfo.isUpdateAvailable()) {
                        if (mUpdateListener != null) {
                            mUpdateListener.onUpdateAvailable(versionInfo);
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<VersionInfo> call, Throwable t) {
                Log.e(TAG, "检查版本更新失败", t);
                if (mUpdateListener != null) {
                    mUpdateListener.onUpdateError("检查更新失败: " + t.getMessage());
                }
            }
        });
    }
    
    /**
     * 下载更新
     */
    public void downloadUpdate(String downloadUrl, VersionUpdateListener listener) {
        this.mUpdateListener = listener;
        
        // 创建下载目录
        File updateDir = new File(mContext.getExternalFilesDir(null), UPDATE_DIR);
        if (!updateDir.exists()) {
            updateDir.mkdirs();
        }
        
        File apkFile = new File(updateDir, APK_NAME);
        
        mApiService.downloadApk(downloadUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveApkFile(response.body(), apkFile);
                } else {
                    if (mUpdateListener != null) {
                        mUpdateListener.onUpdateError("下载失败: " + response.message());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "下载更新失败", t);
                if (mUpdateListener != null) {
                    mUpdateListener.onUpdateError("下载失败: " + t.getMessage());
                }
            }
        });
    }
    
    /**
     * 保存APK文件
     */
    private void saveApkFile(ResponseBody body, File apkFile) {
        try (InputStream inputStream = body.byteStream();
             OutputStream outputStream = new FileOutputStream(apkFile)) {
            
            byte[] buffer = new byte[4096];
            long totalSize = body.contentLength();
            long downloadedSize = 0;
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                downloadedSize += bytesRead;
                
                if (totalSize > 0 && mUpdateListener != null) {
                    int progress = (int) ((downloadedSize * 100) / totalSize);
                    mUpdateListener.onUpdateProgress(progress);
                }
            }
            
            if (mUpdateListener != null) {
                mUpdateListener.onUpdateComplete(apkFile);
            }
            
        } catch (IOException e) {
            Log.e(TAG, "保存APK文件失败", e);
            if (mUpdateListener != null) {
                mUpdateListener.onUpdateError("保存文件失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 安装更新
     */
    public void installUpdate(File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(mContext, 
                    mContext.getPackageName() + ".fileprovider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
            
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            mContext.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "安装更新失败", e);
            if (mUpdateListener != null) {
                mUpdateListener.onUpdateError("安装失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取当前版本号
     */
    public int getCurrentVersionCode() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager()
                .getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "获取版本号失败", e);
            return 0;
        }
    }
    
    /**
     * 获取当前版本名
     */
    public String getCurrentVersionName() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager()
                .getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "获取版本名失败", e);
            return "unknown";
        }
    }
    
    /**
     * 检查是否为最新版本
     */
    public boolean isLatestVersion(int serverVersionCode) {
        return getCurrentVersionCode() >= serverVersionCode;
    }
    
    /**
     * 清理更新文件
     */
    public void cleanupUpdateFiles() {
        File updateDir = new File(mContext.getExternalFilesDir(null), UPDATE_DIR);
        if (updateDir.exists()) {
            File[] files = updateDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".apk")) {
                        file.delete();
                    }
                }
            }
        }
    }
} 