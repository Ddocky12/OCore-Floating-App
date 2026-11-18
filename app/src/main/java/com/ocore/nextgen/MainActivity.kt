package com.ocore.nextgen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set layout sederhana via kode agar tidak perlu XML rumit
        val btnStart = Button(this).apply {
            text = "Mulai OCore Floating Pill"
            setOnClickListener { checkOverlayPermissionAndStart() }
        }
        setContentView(btnStart)
    }

    private fun checkOverlayPermissionAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 1001)
            Toast.makeText(this, "Izinkan Tampil di Atas Aplikasi Lain!", Toast.LENGTH_LONG).show()
        } else {
            startFloatingService()
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        finish() // Tutup dashboard setelah pill muncul
    }
}
