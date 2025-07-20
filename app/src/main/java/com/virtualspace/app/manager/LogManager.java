package com.virtualspace.app.manager;

import android.content.Context;
import android.util.Log;

import com.virtualspace.app.network.ApiService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 日志管理器
 * 负责日志记录、日志轮转和错误上报
 */
public class LogManager {
    private static final String TAG = "LogManager";
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "virtualspace_";
    private static final String LOG_FILE_SUFFIX = ".log";
    private static final int MAX_LOG_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_LOG_FILES = 5;
    
    private Context mContext;
    private ApiService mApiService;
    private ExecutorService mExecutor;
    private File mCurrentLogFile;
    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mTimeFormat;
    
    public enum LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }
    
    public LogManager(Context context, ApiService apiService) {
        this.mContext = context;
        this.mApiService = apiService;
        this.mExecutor = Executors.newSingleThreadExecutor();
        this.mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.mTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        
        initLogFile();
    }
    
    /**
     * 初始化日志文件
     */
    private void initLogFile() {
        File logDir = new File(mContext.getExternalFilesDir(null), LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        
        String fileName = LOG_FILE_PREFIX + mDateFormat.format(new Date()) + LOG_FILE_SUFFIX;
        mCurrentLogFile = new File(logDir, fileName);
    }
    
    /**
     * 记录日志
     */
    public void log(LogLevel level, String tag, String message) {
        log(level, tag, message, null);
    }
    
    /**
     * 记录日志（带异常）
     */
    public void log(LogLevel level, String tag, String message, Throwable throwable) {
        String logEntry = formatLogEntry(level, tag, message, throwable);
        
        // 输出到Android Log
        switch (level) {
            case VERBOSE:
                Log.v(tag, message, throwable);
                break;
            case DEBUG:
                Log.d(tag, message, throwable);
                break;
            case INFO:
                Log.i(tag, message, throwable);
                break;
            case WARN:
                Log.w(tag, message, throwable);
                break;
            case ERROR:
                Log.e(tag, message, throwable);
                break;
        }
        
        // 写入文件
        writeToFile(logEntry);
        
        // 如果是错误日志，上报到服务器
        if (level == LogLevel.ERROR) {
            reportError(logEntry);
        }
    }
    
    /**
     * 格式化日志条目
     */
    private String formatLogEntry(LogLevel level, String tag, String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(mTimeFormat.format(new Date()));
        sb.append(" [").append(level.name()).append("] ");
        sb.append(tag).append(": ").append(message);
        
        if (throwable != null) {
            sb.append("\n").append(getStackTrace(throwable));
        }
        
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        
        StackTraceElement[] elements = throwable.getStackTrace();
        for (StackTraceElement element : elements) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        
        Throwable cause = throwable.getCause();
        if (cause != null) {
            sb.append("Caused by: ").append(getStackTrace(cause));
        }
        
        return sb.toString();
    }
    
    /**
     * 写入文件
     */
    private void writeToFile(final String logEntry) {
        mExecutor.execute(() -> {
            try {
                // 检查文件大小，如果超过限制则轮转
                if (mCurrentLogFile.exists() && mCurrentLogFile.length() > MAX_LOG_SIZE) {
                    rotateLogFiles();
                }
                
                // 写入日志
                try (FileWriter writer = new FileWriter(mCurrentLogFile, true);
                     PrintWriter printWriter = new PrintWriter(writer)) {
                    printWriter.print(logEntry);
                    printWriter.flush();
                }
                
            } catch (IOException e) {
                Log.e(TAG, "写入日志文件失败", e);
            }
        });
    }
    
    /**
     * 轮转日志文件
     */
    private void rotateLogFiles() {
        File logDir = mCurrentLogFile.getParentFile();
        if (logDir == null) return;
        
        // 删除最旧的日志文件
        File[] logFiles = logDir.listFiles((dir, name) -> 
            name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_SUFFIX));
        
        if (logFiles != null && logFiles.length >= MAX_LOG_FILES) {
            // 按修改时间排序，删除最旧的
            java.util.Arrays.sort(logFiles, (f1, f2) -> 
                Long.compare(f1.lastModified(), f2.lastModified()));
            
            for (int i = 0; i < logFiles.length - MAX_LOG_FILES + 1; i++) {
                logFiles[i].delete();
            }
        }
        
        // 创建新的日志文件
        initLogFile();
    }
    
    /**
     * 上报错误日志
     */
    private void reportError(String errorLog) {
        if (mApiService != null) {
            mApiService.reportError(errorLog).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "错误日志上报成功");
                    } else {
                        Log.w(TAG, "错误日志上报失败: " + response.message());
                    }
                }
                
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.w(TAG, "错误日志上报失败", t);
                }
            });
        }
    }
    
    /**
     * 获取日志文件列表
     */
    public File[] getLogFiles() {
        File logDir = new File(mContext.getExternalFilesDir(null), LOG_DIR);
        if (!logDir.exists()) {
            return new File[0];
        }
        
        return logDir.listFiles((dir, name) -> 
            name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_SUFFIX));
    }
    
    /**
     * 清理日志文件
     */
    public void cleanupLogs() {
        mExecutor.execute(() -> {
            File logDir = new File(mContext.getExternalFilesDir(null), LOG_DIR);
            if (logDir.exists()) {
                File[] logFiles = logDir.listFiles();
                if (logFiles != null) {
                    for (File file : logFiles) {
                        if (file.isFile() && file.getName().endsWith(LOG_FILE_SUFFIX)) {
                            file.delete();
                        }
                    }
                }
            }
        });
    }
    
    /**
     * 获取日志文件内容
     */
    public String getLogContent(File logFile) {
        if (!logFile.exists()) {
            return "";
        }
        
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(logFile));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            reader.close();
            return content.toString();
            
        } catch (IOException e) {
            Log.e(TAG, "读取日志文件失败", e);
            return "";
        }
    }
    
    /**
     * 关闭日志管理器
     */
    public void shutdown() {
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
    }
    
    // 便捷方法
    public void v(String tag, String message) {
        log(LogLevel.VERBOSE, tag, message);
    }
    
    public void d(String tag, String message) {
        log(LogLevel.DEBUG, tag, message);
    }
    
    public void i(String tag, String message) {
        log(LogLevel.INFO, tag, message);
    }
    
    public void w(String tag, String message) {
        log(LogLevel.WARN, tag, message);
    }
    
    public void e(String tag, String message) {
        log(LogLevel.ERROR, tag, message);
    }
    
    public void e(String tag, String message, Throwable throwable) {
        log(LogLevel.ERROR, tag, message, throwable);
    }
} 