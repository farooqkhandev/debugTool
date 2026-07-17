package com.quadlogixs.debugtool.core.network

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.chuckerteam.chucker.api.Chucker
import com.quadlogixs.debugtool.api.DebugToolRegistry
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Accelerometer shake listener.
 *
 * By default only invokes [onShakeDetect]. Set [launchChuckerOnShake] to also toggle Chucker;
 * prefer opening Chucker from the Network Trace menu action.
 */
class ShakeDetector(
    private val context: Context,
    private val shakeThreshold: Float = 2.7f,
    private val shakeCooldownMs: Long = 1000L,
    private val launchChuckerOnShake: Boolean = false,
    private val onShakeDetect: () -> Unit,
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShakeTimestamp: Long = 0

    private val shakeEnabled: Boolean
        get() = DebugToolRegistry.isInstalled() && DebugToolRegistry.config.features.shakeEnabled

    fun start() {
        if (!shakeEnabled) return
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: Timber.tag("ShakeDetector").e("Accelerometer sensor not available")
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!shakeEnabled || event == null) return

        val (x, y, z) = event.values
        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        val currentTime = System.currentTimeMillis()
        if (gForce > shakeThreshold && currentTime - lastShakeTimestamp > shakeCooldownMs) {
            lastShakeTimestamp = currentTime
            onShakeDetected()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun onShakeDetected() {
        try {
            onShakeDetect()
            if (!launchChuckerOnShake) return
            if (!DebugToolRegistry.isInstalled() || !DebugToolRegistry.config.features.chuckerEnabled) {
                return
            }
            if (isChuckerRunning()) {
                destroyChuckerIfOpen()
            } else {
                context.startActivity(
                    Chucker.getLaunchIntent(context).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                )
            }
        } catch (e: Exception) {
            Timber.tag("ShakeDetector").e(e, "Error handling shake: ${e.message}")
        }
    }

    private fun isChuckerRunning(): Boolean =
        runCatching {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            activityManager?.appTasks?.any { task ->
                task.taskInfo.topActivity?.className ==
                    "com.chuckerteam.chucker.internal.ui.MainActivity"
            } ?: false
        }.getOrDefault(false)

    private fun destroyChuckerIfOpen() {
        runCatching {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            activityManager?.appTasks?.forEach { task ->
                if (task.taskInfo.topActivity?.className ==
                    "com.chuckerteam.chucker.internal.ui.MainActivity"
                ) {
                    task.finishAndRemoveTask()
                }
            }
        }.onFailure {
            Timber.tag("ShakeDetector").e(it, "Error destroying Chucker")
        }
    }
}
