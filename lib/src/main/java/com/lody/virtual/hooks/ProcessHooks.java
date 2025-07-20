package com.lody.virtual.hooks;

import android.os.Process;
import android.util.Log;
import com.lody.virtual.HookWrapper;
import com.lody.virtual.VirtualCore;

/**
 * 进程Hook类
 * 实现进程信息虚拟化
 */
public class ProcessHooks {
    
    private static final String TAG = "ProcessHooks";
    
    /**
     * Hook Process.myPid()
     * 返回虚拟进程ID
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Process",
        targetMethod = "myPid",
        priority = 100
    )
    public static int hookMyPid() {
        try {
            Log.d(TAG, "Hook Process.myPid()");
            
            // 获取虚拟进程ID
            int virtualPid = VirtualCore.getInstance().getVirtualProcessId();
            if (virtualPid > 0) {
                Log.d(TAG, "Returning virtual process ID: " + virtualPid);
                return virtualPid;
            }
            
            // 如果没有虚拟进程ID，调用原始方法
            Log.d(TAG, "Calling original myPid()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in myPid hook", e);
        }
        
        return Process.myPid();
    }
    
    /**
     * Hook Process.myUid()
     * 返回虚拟用户ID
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Process",
        targetMethod = "myUid",
        priority = 100
    )
    public static int hookMyUid() {
        try {
            Log.d(TAG, "Hook Process.myUid()");
            
            // 获取虚拟用户ID
            int virtualUid = VirtualCore.getInstance().getVirtualUserId();
            if (virtualUid > 0) {
                Log.d(TAG, "Returning virtual user ID: " + virtualUid);
                return virtualUid;
            }
            
            // 如果没有虚拟用户ID，调用原始方法
            Log.d(TAG, "Calling original myUid()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in myUid hook", e);
        }
        
        return Process.myUid();
    }
    
    /**
     * Hook Process.myUserHandle()
     * 返回虚拟用户句柄
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Process",
        targetMethod = "myUserHandle",
        priority = 100
    )
    public static Object hookMyUserHandle() {
        try {
            Log.d(TAG, "Hook Process.myUserHandle()");
            
            // 获取虚拟用户句柄
            Object virtualUserHandle = VirtualCore.getInstance().getVirtualUserHandle();
            if (virtualUserHandle != null) {
                Log.d(TAG, "Returning virtual user handle");
                return virtualUserHandle;
            }
            
            // 如果没有虚拟用户句柄，调用原始方法
            Log.d(TAG, "Calling original myUserHandle()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in myUserHandle hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook Process.killProcess()
     * 虚拟化进程终止
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Process",
        targetMethod = "killProcess",
        targetParameterTypes = {"int"},
        priority = 100
    )
    public static void hookKillProcess(int pid) {
        try {
            Log.d(TAG, "Hook Process.killProcess(): " + pid);
            
            // 检查是否是虚拟进程
            if (VirtualCore.getInstance().isVirtualProcess(pid)) {
                // 终止虚拟进程
                VirtualCore.getInstance().killVirtualProcess(pid);
                Log.d(TAG, "Killed virtual process: " + pid);
                return;
            }
            
            // 对于非虚拟进程，调用原始方法
            Log.d(TAG, "Calling original killProcess()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in killProcess hook", e);
        }
    }
    
    /**
     * Hook Process.setThreadPriority()
     * 虚拟化线程优先级设置
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Process",
        targetMethod = "setThreadPriority",
        targetParameterTypes = {"int", "int"},
        priority = 100
    )
    public static void hookSetThreadPriority(int tid, int priority) {
        try {
            Log.d(TAG, "Hook Process.setThreadPriority(): tid=" + tid + ", priority=" + priority);
            
            // 检查是否是虚拟线程
            if (VirtualCore.getInstance().isVirtualThread(tid)) {
                // 设置虚拟线程优先级
                VirtualCore.getInstance().setVirtualThreadPriority(tid, priority);
                Log.d(TAG, "Set virtual thread priority: " + priority);
                return;
            }
            
            // 对于非虚拟线程，调用原始方法
            Log.d(TAG, "Calling original setThreadPriority()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in setThreadPriority hook", e);
        }
    }
    
    /**
     * Hook Process.getThreadPriority()
     * 获取虚拟线程优先级
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Process",
        targetMethod = "getThreadPriority",
        targetParameterTypes = {"int"},
        priority = 100
    )
    public static int hookGetThreadPriority(int tid) {
        try {
            Log.d(TAG, "Hook Process.getThreadPriority(): " + tid);
            
            // 检查是否是虚拟线程
            if (VirtualCore.getInstance().isVirtualThread(tid)) {
                // 获取虚拟线程优先级
                int priority = VirtualCore.getInstance().getVirtualThreadPriority(tid);
                Log.d(TAG, "Returning virtual thread priority: " + priority);
                return priority;
            }
            
            // 对于非虚拟线程，调用原始方法
            Log.d(TAG, "Calling original getThreadPriority()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getThreadPriority hook", e);
        }
        
        return Process.THREAD_PRIORITY_DEFAULT;
    }
} 