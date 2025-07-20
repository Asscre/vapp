package com.lody.virtual.optimization;

import java.io.Serializable;
import java.util.Map;

/**
 * 缓存响应类
 * 用于网络优化中的缓存响应
 */
public class CachedResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public byte[] data;
    public Map<String, String> headers;
    public long timestamp;
    public long expirationTime;
    public String contentType;
    public int statusCode;
    
    public CachedResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public CachedResponse(byte[] data, Map<String, String> headers, String contentType, int statusCode) {
        this.data = data;
        this.headers = headers;
        this.contentType = contentType;
        this.statusCode = statusCode;
        this.timestamp = System.currentTimeMillis();
        this.expirationTime = timestamp + (30 * 60 * 1000); // 默认30分钟过期
    }
    
    /**
     * 检查是否已过期
     * @return 是否过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
    
    /**
     * 获取剩余有效时间（毫秒）
     * @return 剩余时间
     */
    public long getRemainingTime() {
        long remaining = expirationTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }
    
    /**
     * 设置过期时间
     * @param expirationTime 过期时间戳
     */
    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
    
    /**
     * 设置过期时间（相对时间）
     * @param durationMs 持续时间（毫秒）
     */
    public void setExpirationDuration(long durationMs) {
        this.expirationTime = System.currentTimeMillis() + durationMs;
    }
    
    /**
     * 获取数据大小
     * @return 数据大小（字节）
     */
    public int getDataSize() {
        return data != null ? data.length : 0;
    }
    
    /**
     * 检查是否有数据
     * @return 是否有数据
     */
    public boolean hasData() {
        return data != null && data.length > 0;
    }
    
    /**
     * 检查是否有头部信息
     * @return 是否有头部信息
     */
    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }
    
    /**
     * 获取指定头部值
     * @param name 头部名称
     * @return 头部值
     */
    public String getHeader(String name) {
        return headers != null ? headers.get(name) : null;
    }
    
    /**
     * 设置头部值
     * @param name 头部名称
     * @param value 头部值
     */
    public void setHeader(String name, String value) {
        if (headers == null) {
            headers = new java.util.HashMap<>();
        }
        headers.put(name, value);
    }
    
    /**
     * 获取缓存年龄（毫秒）
     * @return 缓存年龄
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * 获取缓存年龄（秒）
     * @return 缓存年龄（秒）
     */
    public long getAgeInSeconds() {
        return getAge() / 1000;
    }
    
    /**
     * 获取缓存年龄（分钟）
     * @return 缓存年龄（分钟）
     */
    public long getAgeInMinutes() {
        return getAgeInSeconds() / 60;
    }
    
    /**
     * 获取缓存年龄（小时）
     * @return 缓存年龄（小时）
     */
    public long getAgeInHours() {
        return getAgeInMinutes() / 60;
    }
    
    /**
     * 获取缓存年龄（天）
     * @return 缓存年龄（天）
     */
    public long getAgeInDays() {
        return getAgeInHours() / 24;
    }
    
    /**
     * 检查是否是成功的响应
     * @return 是否成功
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * 检查是否是重定向响应
     * @return 是否重定向
     */
    public boolean isRedirect() {
        return statusCode >= 300 && statusCode < 400;
    }
    
    /**
     * 检查是否是客户端错误响应
     * @return 是否客户端错误
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * 检查是否是服务器错误响应
     * @return 是否服务器错误
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
    
    /**
     * 检查是否是错误响应
     * @return 是否错误
     */
    public boolean isError() {
        return isClientError() || isServerError();
    }
    
    @Override
    public String toString() {
        return "CachedResponse{" +
                "dataSize=" + getDataSize() +
                ", statusCode=" + statusCode +
                ", contentType='" + contentType + '\'' +
                ", timestamp=" + timestamp +
                ", expirationTime=" + expirationTime +
                ", age=" + getAgeInSeconds() + "s" +
                ", expired=" + isExpired() +
                '}';
    }
} 