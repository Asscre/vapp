package com.lody.virtual.security;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.lody.virtual.VirtualCore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 数据加密管理器
 * 负责虚拟应用数据的加密、解密和密钥管理
 */
public class DataEncryptionManager {
    
    private static final String TAG = "DataEncryptionManager";
    private static DataEncryptionManager sInstance;
    
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, EncryptionConfig> mEncryptionConfigs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, SecretKey> mAppKeys = new ConcurrentHashMap<>();
    
    private Context mContext;
    private VirtualCore mVirtualCore;
    private KeyStore mKeyStore;
    private SecureRandom mSecureRandom;
    
    // 加密算法常量
    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_AES_GCM = "AES/GCM/NoPadding";
    public static final String ALGORITHM_AES_CBC = "AES/CBC/PKCS5Padding";
    
    // 密钥长度常量
    public static final int KEY_SIZE_128 = 128;
    public static final int KEY_SIZE_256 = 256;
    
    // GCM参数长度
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    
    private DataEncryptionManager() {
        // 私有构造函数，实现单例模式
    }
    
    public static DataEncryptionManager getInstance() {
        if (sInstance == null) {
            synchronized (DataEncryptionManager.class) {
                if (sInstance == null) {
                    sInstance = new DataEncryptionManager();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化数据加密管理器
     * @param context 上下文
     * @param virtualCore 虚拟核心实例
     * @return 初始化是否成功
     */
    public boolean initialize(Context context, VirtualCore virtualCore) {
        if (mIsInitialized.get()) {
            Log.w(TAG, "DataEncryptionManager already initialized");
            return true;
        }
        
        if (context == null || virtualCore == null) {
            Log.e(TAG, "Context or VirtualCore is null");
            return false;
        }
        
        try {
            Log.d(TAG, "Initializing DataEncryptionManager...");
            
            mContext = context.getApplicationContext();
            mVirtualCore = virtualCore;
            
            // 初始化KeyStore
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
            
            // 初始化SecureRandom
            mSecureRandom = new SecureRandom();
            
            // 清理配置
            mEncryptionConfigs.clear();
            mAppKeys.clear();
            
            mIsInitialized.set(true);
            Log.d(TAG, "DataEncryptionManager initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize DataEncryptionManager", e);
            return false;
        }
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return mIsInitialized.get();
    }
    
    /**
     * 为虚拟应用创建加密配置
     * @param packageName 包名
     * @param config 加密配置
     * @return 创建是否成功
     */
    public boolean createEncryptionConfig(String packageName, EncryptionConfig config) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "DataEncryptionManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Creating encryption config for: " + packageName);
            
            // 生成应用密钥
            SecretKey appKey = generateAppKey(packageName, config.keySize);
            if (appKey == null) {
                Log.e(TAG, "Failed to generate app key for: " + packageName);
                return false;
            }
            
            // 保存密钥到KeyStore
            if (!saveKeyToKeyStore(packageName, appKey)) {
                Log.e(TAG, "Failed to save key to KeyStore for: " + packageName);
                return false;
            }
            
            // 注册配置和密钥
            mEncryptionConfigs.put(packageName, config);
            mAppKeys.put(packageName, appKey);
            
            Log.d(TAG, "Encryption config created successfully for: " + packageName);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create encryption config", e);
            return false;
        }
    }
    
    /**
     * 获取虚拟应用的加密配置
     * @param packageName 包名
     * @return 加密配置
     */
    public EncryptionConfig getEncryptionConfig(String packageName) {
        if (!mIsInitialized.get()) {
            return null;
        }
        
        return mEncryptionConfigs.get(packageName);
    }
    
    /**
     * 加密数据
     * @param packageName 包名
     * @param data 原始数据
     * @return 加密后的数据
     */
    public byte[] encryptData(String packageName, byte[] data) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "DataEncryptionManager not initialized");
            return data;
        }
        
        try {
            EncryptionConfig config = mEncryptionConfigs.get(packageName);
            SecretKey key = mAppKeys.get(packageName);
            
            if (config == null || key == null) {
                Log.w(TAG, "No encryption config or key found for: " + packageName);
                return data;
            }
            
            if (!config.enabled) {
                return data; // 加密未启用，返回原始数据
            }
            
            Cipher cipher = Cipher.getInstance(config.algorithm);
            
            if (config.algorithm.equals(ALGORITHM_AES_GCM)) {
                // GCM模式需要IV
                byte[] iv = generateIV(GCM_IV_LENGTH);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
                
                // 加密数据
                byte[] encryptedData = cipher.doFinal(data);
                
                // 组合IV和加密数据
                byte[] result = new byte[iv.length + encryptedData.length];
                System.arraycopy(iv, 0, result, 0, iv.length);
                System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);
                
                return result;
                
            } else {
                // CBC模式
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return cipher.doFinal(data);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt data", e);
            return data; // 加密失败，返回原始数据
        }
    }
    
    /**
     * 解密数据
     * @param packageName 包名
     * @param encryptedData 加密数据
     * @return 解密后的数据
     */
    public byte[] decryptData(String packageName, byte[] encryptedData) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "DataEncryptionManager not initialized");
            return encryptedData;
        }
        
        try {
            EncryptionConfig config = mEncryptionConfigs.get(packageName);
            SecretKey key = mAppKeys.get(packageName);
            
            if (config == null || key == null) {
                Log.w(TAG, "No encryption config or key found for: " + packageName);
                return encryptedData;
            }
            
            if (!config.enabled) {
                return encryptedData; // 加密未启用，返回原始数据
            }
            
            Cipher cipher = Cipher.getInstance(config.algorithm);
            
            if (config.algorithm.equals(ALGORITHM_AES_GCM)) {
                // GCM模式需要分离IV
                if (encryptedData.length < GCM_IV_LENGTH) {
                    Log.e(TAG, "Invalid encrypted data length for GCM");
                    return encryptedData;
                }
                
                byte[] iv = new byte[GCM_IV_LENGTH];
                byte[] data = new byte[encryptedData.length - GCM_IV_LENGTH];
                
                System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
                System.arraycopy(encryptedData, GCM_IV_LENGTH, data, 0, data.length);
                
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
                
                return cipher.doFinal(data);
                
            } else {
                // CBC模式
                cipher.init(Cipher.DECRYPT_MODE, key);
                return cipher.doFinal(encryptedData);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt data", e);
            return encryptedData; // 解密失败，返回原始数据
        }
    }
    
    /**
     * 加密文件
     * @param packageName 包名
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @return 是否成功
     */
    public boolean encryptFile(String packageName, File sourceFile, File targetFile) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "DataEncryptionManager not initialized");
            return false;
        }
        
        try {
            EncryptionConfig config = mEncryptionConfigs.get(packageName);
            SecretKey key = mAppKeys.get(packageName);
            
            if (config == null || key == null) {
                Log.w(TAG, "No encryption config or key found for: " + packageName);
                return false;
            }
            
            if (!config.enabled) {
                // 加密未启用，直接复制文件
                return copyFile(sourceFile, targetFile);
            }
            
            Cipher cipher = Cipher.getInstance(config.algorithm);
            
            if (config.algorithm.equals(ALGORITHM_AES_GCM)) {
                // GCM模式
                byte[] iv = generateIV(GCM_IV_LENGTH);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
                
                try (FileInputStream fis = new FileInputStream(sourceFile);
                     FileOutputStream fos = new FileOutputStream(targetFile);
                     CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                    
                    // 写入IV
                    fos.write(iv);
                    
                    // 加密数据
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                    }
                }
                
            } else {
                // CBC模式
                cipher.init(Cipher.ENCRYPT_MODE, key);
                
                try (FileInputStream fis = new FileInputStream(sourceFile);
                     CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(targetFile), cipher)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt file", e);
            return false;
        }
    }
    
    /**
     * 解密文件
     * @param packageName 包名
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @return 是否成功
     */
    public boolean decryptFile(String packageName, File sourceFile, File targetFile) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "DataEncryptionManager not initialized");
            return false;
        }
        
        try {
            EncryptionConfig config = mEncryptionConfigs.get(packageName);
            SecretKey key = mAppKeys.get(packageName);
            
            if (config == null || key == null) {
                Log.w(TAG, "No encryption config or key found for: " + packageName);
                return false;
            }
            
            if (!config.enabled) {
                // 加密未启用，直接复制文件
                return copyFile(sourceFile, targetFile);
            }
            
            Cipher cipher = Cipher.getInstance(config.algorithm);
            
            if (config.algorithm.equals(ALGORITHM_AES_GCM)) {
                // GCM模式
                try (FileInputStream fis = new FileInputStream(sourceFile)) {
                    // 读取IV
                    byte[] iv = new byte[GCM_IV_LENGTH];
                    if (fis.read(iv) != GCM_IV_LENGTH) {
                        Log.e(TAG, "Failed to read IV from file");
                        return false;
                    }
                    
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                    cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
                    
                    try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                         FileOutputStream fos = new FileOutputStream(targetFile)) {
                        
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = cis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                
            } else {
                // CBC模式
                cipher.init(Cipher.DECRYPT_MODE, key);
                
                try (CipherInputStream cis = new CipherInputStream(new FileInputStream(sourceFile), cipher);
                     FileOutputStream fos = new FileOutputStream(targetFile)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = cis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt file", e);
            return false;
        }
    }
    
    /**
     * 生成数据哈希
     * @param data 数据
     * @param algorithm 哈希算法
     * @return 哈希值
     */
    public String generateHash(byte[] data, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(data);
            return Base64.encodeToString(hash, Base64.NO_WRAP);
            
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to generate hash", e);
            return null;
        }
    }
    
    /**
     * 验证数据完整性
     * @param data 数据
     * @param expectedHash 期望的哈希值
     * @param algorithm 哈希算法
     * @return 是否完整
     */
    public boolean verifyDataIntegrity(byte[] data, String expectedHash, String algorithm) {
        try {
            String actualHash = generateHash(data, algorithm);
            return expectedHash != null && expectedHash.equals(actualHash);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to verify data integrity", e);
            return false;
        }
    }
    
    /**
     * 删除虚拟应用的加密配置
     * @param packageName 包名
     * @return 删除是否成功
     */
    public boolean deleteEncryptionConfig(String packageName) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "DataEncryptionManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Deleting encryption config for: " + packageName);
            
            // 从KeyStore删除密钥
            if (mKeyStore.containsAlias(packageName)) {
                mKeyStore.deleteEntry(packageName);
            }
            
            // 从内存中移除
            mEncryptionConfigs.remove(packageName);
            mAppKeys.remove(packageName);
            
            Log.d(TAG, "Encryption config deleted successfully for: " + packageName);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete encryption config", e);
            return false;
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (!mIsInitialized.get()) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up DataEncryptionManager...");
            
            mEncryptionConfigs.clear();
            mAppKeys.clear();
            
            mIsInitialized.set(false);
            Log.d(TAG, "DataEncryptionManager cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup DataEncryptionManager", e);
        }
    }
    
    /**
     * 生成应用密钥
     */
    private SecretKey generateAppKey(String packageName, int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES);
            keyGenerator.init(keySize, mSecureRandom);
            return keyGenerator.generateKey();
            
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to generate app key", e);
            return null;
        }
    }
    
    /**
     * 保存密钥到KeyStore
     */
    private boolean saveKeyToKeyStore(String packageName, SecretKey key) {
        try {
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(key);
            KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(packageName.toCharArray());
            mKeyStore.setEntry(packageName, entry, protection);
            return true;
            
        } catch (KeyStoreException e) {
            Log.e(TAG, "Failed to save key to KeyStore", e);
            return false;
        }
    }
    
    /**
     * 从KeyStore加载密钥
     */
    private SecretKey loadKeyFromKeyStore(String packageName) {
        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) mKeyStore.getEntry(packageName, 
                new KeyStore.PasswordProtection(packageName.toCharArray()));
            return entry.getSecretKey();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load key from KeyStore", e);
            return null;
        }
    }
    
    /**
     * 生成IV
     */
    private byte[] generateIV(int length) {
        byte[] iv = new byte[length];
        mSecureRandom.nextBytes(iv);
        return iv;
    }
    
    /**
     * 复制文件
     */
    private boolean copyFile(File sourceFile, File targetFile) {
        try {
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(targetFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy file", e);
            return false;
        }
    }
    
    /**
     * 加密配置类
     */
    public static class EncryptionConfig {
        public boolean enabled;
        public String algorithm;
        public int keySize;
        public boolean encryptFiles;
        public boolean encryptDatabase;
        public boolean encryptSharedPreferences;
        public String hashAlgorithm;
        
        public EncryptionConfig() {
            this.enabled = true;
            this.algorithm = ALGORITHM_AES_GCM;
            this.keySize = KEY_SIZE_256;
            this.encryptFiles = true;
            this.encryptDatabase = true;
            this.encryptSharedPreferences = true;
            this.hashAlgorithm = "SHA-256";
        }
        
        public EncryptionConfig(boolean enabled, String algorithm, int keySize) {
            this();
            this.enabled = enabled;
            this.algorithm = algorithm;
            this.keySize = keySize;
        }
    }
} 