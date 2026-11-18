package com.ocore.nextgen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.io.File
import kotlin.math.abs

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val statusFile = File("/data/local/tmp/OCoreNextGen/status.json")

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        setupFloatingView()
        startTelemetryPolling()
    }

    private fun setupFloatingView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = TextView(this).apply {
            text = "FPS: -- | OCore"
            setTextColor(Color.GREEN)
            setBackgroundColor(Color.argb(200, 0, 0, 0))
            setPadding(30, 15, 30, 15)
            textSize = 14f
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        // Jika bergeser lebih dari 10 pixel, anggap ini drag, bukan tap
                        if (abs(dx) > 10 || abs(dy) > 10) {
                            isClick = false
                        }
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Jika ini adalah Tap (tidak banyak bergeser), buka Control Panel
                        if (isClick) {
                            val intent = Intent(this@FloatingService, ControlPanelActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
    }

    private fun startTelemetryPolling() {
        handler.post(object : Runnable {
            override fun run() {
                try {
                    if (statusFile.exists()) {
                        val json = JSONObject(statusFile.readText())
                        val engine = json.optString("engine_state", "--")
                        val activeGame = json.optString("active_game", "None")
                        floatingView.text = "OCore: $engine\nGame: $activeGame"
                    }
                } catch (e: Exception) {}
                handler.postDelayed(this, 2000)
            }
        })
    }

    private fun createNotification(): android.app.Notification {
        val channelId = "ocore_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "OCore Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("OCore NextGen Aktif")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatingView)
        handler.removeCallbacksAndMessages(null)
    }
}
