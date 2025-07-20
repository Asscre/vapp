package com.lody.virtual;

/**
 * 虚拟进程信息类
 * 存储虚拟进程的详细信息
 */
public class VProcessInfo {
    
    // 进程状态常量
    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_STARTING = 1;
    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_STOPPING = 3;
    public static final int STATUS_STOPPED = 4;
    public static final int STATUS_CRASHED = 5;
    
    /**
     * 进程ID
     */
    public int processId;
    
    /**
     * 包名
     */
    public String packageName;
    
    /**
     * APK路径
     */
    public String apkPath;
    
    /**
     * 数据目录
     */
    public String dataDir;
    
    /**
     * 进程状态
     */
    public int status;
    
    /**
     * 启动时间
     */
    public long startTime;
    
    /**
     * 停止时间
     */
    public long stopTime;
    
    /**
     * 运行时长（毫秒）
     */
    public long runTime;
    
    /**
     * 内存使用量（字节）
     */
    public long memoryUsage;
    
    /**
     * CPU使用率（百分比）
     */
    public float cpuUsage;
    
    /**
     * 线程数量
     */
    public int threadCount;
    
    /**
     * 文件描述符数量
     */
    public int fileDescriptorCount;
    
    /**
     * 虚拟用户ID
     */
    public int virtualUserId;
    
    /**
     * 虚拟进程名
     */
    public String virtualProcessName;
    
    /**
     * 主Activity类名
     */
    public String mainActivity;
    
    /**
     * 启动参数
     */
    public String[] launchArgs;
    
    /**
     * 环境变量
     */
    public String[] environmentVars;
    
    /**
     * 权限列表
     */
    public String[] permissions;
    
    /**
     * 网络策略
     */
    public String networkPolicy;
    
    /**
     * 数据隔离策略
     */
    public String dataIsolationPolicy;
    
    /**
     * 权限策略
     */
    public String permissionPolicy;
    
    /**
     * 自定义配置
     */
    public String customConfig;
    
    /**
     * 错误信息
     */
    public String errorMessage;
    
    /**
     * 崩溃堆栈
     */
    public String crashStack;
    
    public VProcessInfo() {
        // 默认构造函数
        this.status = STATUS_UNKNOWN;
        this.startTime = 0;
        this.stopTime = 0;
        this.runTime = 0;
        this.memoryUsage = 0;
        this.cpuUsage = 0.0f;
        this.threadCount = 0;
        this.fileDescriptorCount = 0;
        this.virtualUserId = 0;
    }
    
    /**
     * 复制构造函数
     */
    public VProcessInfo(VProcessInfo other) {
        this.processId = other.processId;
        this.packageName = other.packageName;
        this.apkPath = other.apkPath;
        this.dataDir = other.dataDir;
        this.status = other.status;
        this.startTime = other.startTime;
        this.stopTime = other.stopTime;
        this.runTime = other.runTime;
        this.memoryUsage = other.memoryUsage;
        this.cpuUsage = other.cpuUsage;
        this.threadCount = other.threadCount;
        this.fileDescriptorCount = other.fileDescriptorCount;
        this.virtualUserId = other.virtualUserId;
        this.virtualProcessName = other.virtualProcessName;
        this.mainActivity = other.mainActivity;
        this.launchArgs = other.launchArgs != null ? other.launchArgs.clone() : null;
        this.environmentVars = other.environmentVars != null ? other.environmentVars.clone() : null;
        this.permissions = other.permissions != null ? other.permissions.clone() : null;
        this.networkPolicy = other.networkPolicy;
        this.dataIsolationPolicy = other.dataIsolationPolicy;
        this.permissionPolicy = other.permissionPolicy;
        this.customConfig = other.customConfig;
        this.errorMessage = other.errorMessage;
        this.crashStack = other.crashStack;
    }
    
    /**
     * 获取进程标识符
     */
    public String getProcessId() {
        return packageName + "_" + processId;
    }
    
    /**
     * 检查进程是否运行
     */
    public boolean isRunning() {
        return status == STATUS_RUNNING;
    }
    
    /**
     * 检查进程是否已停止
     */
    public boolean isStopped() {
        return status == STATUS_STOPPED || status == STATUS_CRASHED;
    }
    
    /**
     * 检查进程是否崩溃
     */
    public boolean isCrashed() {
        return status == STATUS_CRASHED;
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case STATUS_UNKNOWN:
                return "Unknown";
            case STATUS_STARTING:
                return "Starting";
            case STATUS_RUNNING:
                return "Running";
            case STATUS_STOPPING:
                return "Stopping";
            case STATUS_STOPPED:
                return "Stopped";
            case STATUS_CRASHED:
                return "Crashed";
            default:
                return "Unknown";
        }
    }
    
    /**
     * 计算运行时长
     */
    public void calculateRunTime() {
        if (startTime > 0) {
            if (stopTime > 0) {
                runTime = stopTime - startTime;
            } else {
                runTime = System.currentTimeMillis() - startTime;
            }
        }
    }
    
    /**
     * 获取运行时长（格式化）
     */
    public String getFormattedRunTime() {
        calculateRunTime();
        
        long seconds = runTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * 获取内存使用量（格式化）
     */
    public String getFormattedMemoryUsage() {
        if (memoryUsage < 1024) {
            return memoryUsage + " B";
        } else if (memoryUsage < 1024 * 1024) {
            return String.format("%.1f KB", memoryUsage / 1024.0);
        } else if (memoryUsage < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", memoryUsage / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", memoryUsage / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 获取CPU使用率（格式化）
     */
    public String getFormattedCpuUsage() {
        return String.format("%.1f%%", cpuUsage);
    }
    
    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        if (permissions == null) {
            return false;
        }
        
        for (String perm : permissions) {
            if (permission.equals(perm)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 添加启动参数
     */
    public void addLaunchArg(String arg) {
        if (launchArgs == null) {
            launchArgs = new String[0];
        }
        
        String[] newArgs = new String[launchArgs.length + 1];
        System.arraycopy(launchArgs, 0, newArgs, 0, launchArgs.length);
        newArgs[launchArgs.length] = arg;
        launchArgs = newArgs;
    }
    
    /**
     * 添加环境变量
     */
    public void addEnvironmentVar(String name, String value) {
        if (environmentVars == null) {
            environmentVars = new String[0];
        }
        
        String[] newVars = new String[environmentVars.length + 1];
        System.arraycopy(environmentVars, 0, newVars, 0, environmentVars.length);
        newVars[environmentVars.length] = name + "=" + value;
        environmentVars = newVars;
    }
    
    /**
     * 设置错误信息
     */
    public void setError(String errorMessage, String crashStack) {
        this.errorMessage = errorMessage;
        this.crashStack = crashStack;
        this.status = STATUS_CRASHED;
        this.stopTime = System.currentTimeMillis();
    }
    
    /**
     * 重置进程信息
     */
    public void reset() {
        this.status = STATUS_UNKNOWN;
        this.startTime = 0;
        this.stopTime = 0;
        this.runTime = 0;
        this.memoryUsage = 0;
        this.cpuUsage = 0.0f;
        this.threadCount = 0;
        this.fileDescriptorCount = 0;
        this.errorMessage = null;
        this.crashStack = null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VProcessInfo that = (VProcessInfo) obj;
        return processId == that.processId;
    }
    
    @Override
    public int hashCode() {
        return processId;
    }
    
    @Override
    public String toString() {
        return "VProcessInfo{" +
                "processId=" + processId +
                ", packageName='" + packageName + '\'' +
                ", status=" + getStatusDescription() +
                ", runTime=" + getFormattedRunTime() +
                ", memoryUsage=" + getFormattedMemoryUsage() +
                ", cpuUsage=" + getFormattedCpuUsage() +
                '}';
    }
} 