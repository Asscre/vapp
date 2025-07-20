package com.virtualspace.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L // 2秒
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        Log.d(TAG, "SplashActivity onCreate")
        
        // 延迟跳转到主界面
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMain()
        }, SPLASH_DELAY)
    }
    
    private fun navigateToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            Log.d(TAG, "Navigated to MainActivity")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to MainActivity", e)
        }
    }
} 