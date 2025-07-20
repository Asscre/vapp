package com.lody.virtual.hooks;

/**
 * Hook信息类
 * 用于存储Hook的详细信息
 */
public class HookInfo {
    
    /**
     * 目标类名
     */
    public String targetClass;
    
    /**
     * 目标方法名
     */
    public String targetMethod;
    
    /**
     * 目标参数类型
     */
    public String[] targetParameterTypes;
    
    /**
     * Hook方法名
     */
    public String hookMethod;
    
    /**
     * 备份方法名
     */
    public String backupMethod;
    
    /**
     * Hook优先级
     */
    public int priority;
    
    /**
     * 是否启用
     */
    public boolean enabled;
    
    /**
     * Hook状态
     */
    public HookStatus status;
    
    /**
     * 错误信息
     */
    public String errorMessage;
    
    /**
     * 创建时间
     */
    public long createTime;
    
    /**
     * Hook时间
     */
    public long hookTime;
    
    /**
     * Hook状态枚举
     */
    public enum HookStatus {
        PENDING,    // 等待Hook
        HOOKED,     // 已Hook
        FAILED,     // Hook失败
        UNHOOKED    // 已取消Hook
    }
    
    public HookInfo() {
        this.createTime = System.currentTimeMillis();
        this.enabled = true;
        this.status = HookStatus.PENDING;
        this.priority = 100;
    }
    
    public HookInfo(String targetClass, String targetMethod, String hookMethod) {
        this();
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.hookMethod = hookMethod;
    }
    
    public HookInfo(String targetClass, String targetMethod, String[] targetParameterTypes, 
                   String hookMethod, int priority) {
        this(targetClass, targetMethod, hookMethod);
        this.targetParameterTypes = targetParameterTypes;
        this.priority = priority;
    }
    
    /**
     * 获取完整的方法签名
     * @return 方法签名
     */
    public String getMethodSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(targetClass).append(".").append(targetMethod);
        
        if (targetParameterTypes != null && targetParameterTypes.length > 0) {
            sb.append("(");
            for (int i = 0; i < targetParameterTypes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(targetParameterTypes[i]);
            }
            sb.append(")");
        } else {
            sb.append("()");
        }
        
        return sb.toString();
    }
    
    /**
     * 检查是否已Hook
     * @return 是否已Hook
     */
    public boolean isHooked() {
        return status == HookStatus.HOOKED;
    }
    
    /**
     * 检查是否失败
     * @return 是否失败
     */
    public boolean isFailed() {
        return status == HookStatus.FAILED;
    }
    
    /**
     * 检查是否等待Hook
     * @return 是否等待Hook
     */
    public boolean isPending() {
        return status == HookStatus.PENDING;
    }
    
    /**
     * 检查是否已取消Hook
     * @return 是否已取消Hook
     */
    public boolean isUnhooked() {
        return status == HookStatus.UNHOOKED;
    }
    
    /**
     * 获取Hook耗时（毫秒）
     * @return Hook耗时
     */
    public long getHookDuration() {
        if (hookTime > 0 && createTime > 0) {
            return hookTime - createTime;
        }
        return 0;
    }
    
    /**
     * 获取Hook耗时（格式化）
     * @return 格式化的Hook耗时
     */
    public String getFormattedHookDuration() {
        long duration = getHookDuration();
        if (duration < 1000) {
            return duration + "ms";
        } else {
            return String.format("%.2fs", duration / 1000.0);
        }
    }
    
    @Override
    public String toString() {
        return "HookInfo{" +
                "targetClass='" + targetClass + '\'' +
                ", targetMethod='" + targetMethod + '\'' +
                ", hookMethod='" + hookMethod + '\'' +
                ", priority=" + priority +
                ", enabled=" + enabled +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        HookInfo hookInfo = (HookInfo) obj;
        
        if (targetClass != null ? !targetClass.equals(hookInfo.targetClass) : hookInfo.targetClass != null)
            return false;
        if (targetMethod != null ? !targetMethod.equals(hookInfo.targetMethod) : hookInfo.targetMethod != null)
            return false;
        return hookMethod != null ? hookMethod.equals(hookInfo.hookMethod) : hookInfo.hookMethod == null;
    }
    
    @Override
    public int hashCode() {
        int result = targetClass != null ? targetClass.hashCode() : 0;
        result = 31 * result + (targetMethod != null ? targetMethod.hashCode() : 0);
        result = 31 * result + (hookMethod != null ? hookMethod.hashCode() : 0);
        return result;
    }
} 