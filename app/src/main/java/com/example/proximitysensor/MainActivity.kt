@file:Suppress("DEPRECATION")

package com.example.proximitysensor

import android.app.Activity
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the initial background color
        window.decorView.setBackgroundColor(Color.WHITE)

        // Initialize the sensor manager and proximity sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        // Initialize the PowerManager and WakeLock
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "ProximitySensor::WakeLock"
        )

        if (proximitySensor == null) {
            Log.e("ProximitySensor", "No proximity sensor found on this device.")
        }
    }

    override fun onResume() {
        super.onResume()
        proximitySensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_PROXIMITY) {
                val distance = it.values[0]
                val maxRange = proximitySensor?.maximumRange ?: 0f

                Log.d("ProximitySensor", "Distance: $distance, Max Range: $maxRange")

                if (distance < maxRange) {
                    turnOffScreen()
                } else {
                    turnOnScreen()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    private fun turnOffScreen() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
        window.decorView.setBackgroundColor(Color.BLACK)
    }

    private fun turnOnScreen() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        window.decorView.setBackgroundColor(Color.WHITE)
    }
}
