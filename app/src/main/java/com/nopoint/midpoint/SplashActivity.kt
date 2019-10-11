package com.nopoint.midpoint

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.nopoint.midpoint.models.CurrentUser

const val SPLASH_DISPLAY_TIME = 500.toLong()

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler()
        val currentUser = CurrentUser.getCurrentUser(this)
        handler.postDelayed(object : Runnable {
            override fun run() {
                    startActivity(Intent(this@SplashActivity, EntryActivity::class.java))
                finish()
                handler.removeCallbacks(this)
            }
        }, SPLASH_DISPLAY_TIME)
    }
}