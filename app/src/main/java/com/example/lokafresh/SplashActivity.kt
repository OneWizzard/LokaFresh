package com.example.lokafresh

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay splash screen selama 2 detik (2000 ms)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Login::class.java) // Ganti MainActivity ke activity utama kamu
            startActivity(intent)
            finish() // supaya splash screen tidak bisa kembali
        }, 2000)
    }
}
