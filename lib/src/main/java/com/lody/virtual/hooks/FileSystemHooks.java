package com.lody.virtual.hooks;

import android.util.Log;
import com.lody.virtual.HookWrapper;
import com.lody.virtual.VEnvironment;

import java.io.File;

/**
 * 文件系统Hook类
 * 实现文件路径重定向和虚拟化
 */
public class FileSystemHooks {
    
    private static final String TAG = "FileSystemHooks";
    
    /**
     * Hook File构造函数
     * 重定向文件路径到虚拟环境
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "<init>",
        targetParameterTypes = {"java.lang.String"},
        priority = 100
    )
    public static void hookFileConstructor(String path) {
        try {
            Log.d(TAG, "Hook File constructor: " + path);
            
            // 检查是否需要重定向
            String redirectedPath = VEnvironment.getRedirectedPath(path);
            if (!redirectedPath.equals(path)) {
                Log.d(TAG, "Redirecting path: " + path + " -> " + redirectedPath);
                // 调用原始构造函数，但使用重定向后的路径
                // 这里需要通过反射调用原始方法
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File constructor hook", e);
        }
    }
    
    /**
     * Hook File.exists()
     * 检查虚拟环境中的文件是否存在
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "exists",
        priority = 100
    )
    public static boolean hookFileExists(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.exists(): " + path);
            
            // 检查虚拟环境中的文件
            String virtualPath = VEnvironment.getVirtualPath(path);
            File virtualFile = new File(virtualPath);
            
            boolean exists = virtualFile.exists();
            Log.d(TAG, "File exists in virtual environment: " + exists);
            
            return exists;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.exists() hook", e);
            return false;
        }
    }
    
    /**
     * Hook File.getAbsolutePath()
     * 返回虚拟环境中的绝对路径
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "getAbsolutePath",
        priority = 100
    )
    public static String hookFileGetAbsolutePath(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.getAbsolutePath(): " + path);
            
            // 返回虚拟环境中的绝对路径
            String virtualPath = VEnvironment.getVirtualPath(path);
            String absolutePath = new File(virtualPath).getAbsolutePath();
            
            Log.d(TAG, "Returning virtual absolute path: " + absolutePath);
            return absolutePath;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.getAbsolutePath() hook", e);
            return "";
        }
    }
    
    /**
     * Hook File.getCanonicalPath()
     * 返回虚拟环境中的规范路径
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "getCanonicalPath",
        priority = 100
    )
    public static String hookFileGetCanonicalPath(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.getCanonicalPath(): " + path);
            
            // 返回虚拟环境中的规范路径
            String virtualPath = VEnvironment.getVirtualPath(path);
            String canonicalPath = new File(virtualPath).getCanonicalPath();
            
            Log.d(TAG, "Returning virtual canonical path: " + canonicalPath);
            return canonicalPath;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.getCanonicalPath() hook", e);
            return "";
        }
    }
    
    /**
     * Hook File.list()
     * 返回虚拟环境中的文件列表
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "list",
        priority = 100
    )
    public static String[] hookFileList(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.list(): " + path);
            
            // 返回虚拟环境中的文件列表
            String virtualPath = VEnvironment.getVirtualPath(path);
            File virtualFile = new File(virtualPath);
            
            String[] list = virtualFile.list();
            Log.d(TAG, "Returning virtual file list, count: " + (list != null ? list.length : 0));
            
            return list;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.list() hook", e);
            return new String[0];
        }
    }
    
    /**
     * Hook File.listFiles()
     * 返回虚拟环境中的文件对象列表
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "listFiles",
        priority = 100
    )
    public static File[] hookFileListFiles(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.listFiles(): " + path);
            
            // 返回虚拟环境中的文件对象列表
            String virtualPath = VEnvironment.getVirtualPath(path);
            File virtualFile = new File(virtualPath);
            
            File[] files = virtualFile.listFiles();
            Log.d(TAG, "Returning virtual file objects, count: " + (files != null ? files.length : 0));
            
            return files;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.listFiles() hook", e);
            return new File[0];
        }
    }
    
    /**
     * Hook File.mkdir()
     * 在虚拟环境中创建目录
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "mkdir",
        priority = 100
    )
    public static boolean hookFileMkdir(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.mkdir(): " + path);
            
            // 在虚拟环境中创建目录
            String virtualPath = VEnvironment.getVirtualPath(path);
            File virtualFile = new File(virtualPath);
            
            boolean result = virtualFile.mkdir();
            Log.d(TAG, "Created directory in virtual environment: " + result);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.mkdir() hook", e);
            return false;
        }
    }
    
    /**
     * Hook File.mkdirs()
     * 在虚拟环境中创建多级目录
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "mkdirs",
        priority = 100
    )
    public static boolean hookFileMkdirs(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.mkdirs(): " + path);
            
            // 在虚拟环境中创建多级目录
            String virtualPath = VEnvironment.getVirtualPath(path);
            File virtualFile = new File(virtualPath);
            
            boolean result = virtualFile.mkdirs();
            Log.d(TAG, "Created directories in virtual environment: " + result);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.mkdirs() hook", e);
            return false;
        }
    }
    
    /**
     * Hook File.delete()
     * 在虚拟环境中删除文件
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "delete",
        priority = 100
    )
    public static boolean hookFileDelete(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.delete(): " + path);
            
            // 在虚拟环境中删除文件
            String virtualPath = VEnvironment.getVirtualPath(path);
            File virtualFile = new File(virtualPath);
            
            boolean result = virtualFile.delete();
            Log.d(TAG, "Deleted file in virtual environment: " + result);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.delete() hook", e);
            return false;
        }
    }
    
    /**
     * Hook File.renameTo()
     * 在虚拟环境中重命名文件
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "renameTo",
        targetParameterTypes = {"java.io.File"},
        priority = 100
    )
    public static boolean hookFileRenameTo(Object file, File dest) {
        try {
            String srcPath = getFilePath(file);
            String destPath = dest.getPath();
            Log.d(TAG, "Hook File.renameTo(): " + srcPath + " -> " + destPath);
            
            // 在虚拟环境中重命名文件
            String virtualSrcPath = VEnvironment.getVirtualPath(srcPath);
            String virtualDestPath = VEnvironment.getVirtualPath(destPath);
            
            File virtualSrcFile = new File(virtualSrcPath);
            File virtualDestFile = new File(virtualDestPath);
            
            boolean result = virtualSrcFile.renameTo(virtualDestFile);
            Log.d(TAG, "Renamed file in virtual environment: " + result);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.renameTo() hook", e);
            return false;
        }
    }
    
    /**
     * Hook File.createNewFile()
     * 在虚拟环境中创建新文件
     */
    @HookWrapper.Hook(
        targetClass = "java.io.File",
        targetMethod = "createNewFile",
        priority = 100
    )
    public static boolean hookFileCreateNewFile(Object file) {
        try {
            String path = getFilePath(file);
            Log.d(TAG, "Hook File.createNewFile(): " + path);
            
            // 在虚拟环境中创建新文件
            String virtualPath = VEnvironment.getVirtualPath(path);
            File virtualFile = new File(virtualPath);
            
            boolean result = virtualFile.createNewFile();
            Log.d(TAG, "Created new file in virtual environment: " + result);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in File.createNewFile() hook", e);
            return false;
        }
    }
    
    /**
     * 获取文件路径的辅助方法
     */
    private static String getFilePath(Object file) {
        try {
            // 通过反射获取文件路径
            java.lang.reflect.Method getPathMethod = file.getClass().getMethod("getPath");
            return (String) getPathMethod.invoke(file);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get file path", e);
            return "";
        }
    }
} 