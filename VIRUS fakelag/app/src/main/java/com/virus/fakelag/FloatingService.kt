package com.virus.fakelag

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import kotlin.concurrent.timer

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatView: View
    private var timerTask: Timer? = null
    private var isRunning = false
    private var delay = 500L

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        createFloatingButton()
        startForeground(1, createNotification())
    }

    private fun createFloatingButton() {
        floatView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatView, params)

        val btn = floatView.findViewById<Button>(R.id.floatingBtn)
        btn.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                btn.text = "Stop"
                startFakeLag()
            } else {
                isRunning = false
                btn.text = "Start"
                timerTask?.cancel()
            }
        }

        floatView.setOnTouchListener(object : View.OnTouchListener {
            var lastX = 0f
            var lastY = 0f
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX
                        lastY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - lastX).toInt()
                        val dy = (event.rawY - lastY).toInt()
                        params.x += dx
                        params.y += dy
                        windowManager.updateViewLayout(floatView, params)
                        lastX = event.rawX
                        lastY = event.rawY
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun startFakeLag() {
        timerTask = timer(period = delay * 2) {
            FakeVPN.start(this@FloatingService)
            Handler(Looper.getMainLooper()).postDelayed({
                FakeVPN.stop(this@FloatingService)
            }, delay)
        }
    }

    private fun createNotification(): Notification {
        val channelId = "fakelag_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "FakeLag", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("VIRUS fakelag فعال است")
            .setSmallIcon(R.drawable.ic_wifi)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerTask?.cancel()
        windowManager.removeView(floatView)
    }
}