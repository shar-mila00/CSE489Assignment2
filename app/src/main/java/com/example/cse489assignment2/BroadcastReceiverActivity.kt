package com.example.cse489assignment2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.example.cse489assignment2.databinding.ActivityBroadcastReceiverBinding

/**
 * Second activity of the Broadcast Receiver flow. Its behaviour depends on
 * the selection made in the first activity:
 *
 * 1. Custom broadcast receiver -> takes plain text input from the user,
 *    which is passed to the next activity for the custom broadcast receiver.
 * 2. System battery notification receiver -> receives the battery percentage
 *    broadcast (ACTION_BATTERY_CHANGED) and displays it.
 */
class BroadcastReceiverActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_CUSTOM = "custom"
        const val TYPE_BATTERY = "battery"
        const val EXTRA_TEXT = "extra_text"
    }

    private lateinit var binding: ActivityBroadcastReceiverBinding
    private var selection: String = TYPE_CUSTOM

    private var batteryRegistered = false

    /** System battery notification receiver (sticky broadcast, re-delivered on every change). */
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showBattery(intent)
        }
    }

    private fun showBattery(intent: Intent?) {
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) {
            val percentage = level * 100 / scale
            binding.tvBattery.text = getString(R.string.battery_level, percentage)
        }

        // Charging / Not Charging
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        binding.tvCharging.text = getString(
            if (isCharging) R.string.battery_status_charging
            else R.string.battery_status_not_charging
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBroadcastReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back arrow in the action bar + a Back button on screen.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnBack.setOnClickListener { finish() }

        selection = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_CUSTOM

        if (selection == TYPE_CUSTOM) {
            // Option 1: take plain text input from the user.
            binding.layoutCustom.visibility = View.VISIBLE
            binding.btnSend.setOnClickListener {
                val message = binding.editTextMessage.text.toString().trim()
                if (message.isEmpty()) {
                    binding.editTextMessage.error = getString(R.string.error_empty_message)
                    return@setOnClickListener
                }
                startActivity(
                    Intent(this, BroadcastResultActivity::class.java)
                        .putExtra(EXTRA_TEXT, message)
                )
            }
        } else {
            // Option 2: battery percentage broadcast is shown in this activity.
            binding.layoutBattery.visibility = View.VISIBLE
            binding.btnNextBattery.setOnClickListener {
                startActivity(
                    Intent(this, BroadcastResultActivity::class.java)
                        .putExtra(EXTRA_TYPE, TYPE_BATTERY)
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStart() {
        super.onStart()
        if (selection == TYPE_BATTERY) {
            // ACTION_BATTERY_CHANGED is sticky: registering returns the latest value
            // immediately, so show it right away and keep listening for updates.
            // (Android 14+ requires an explicit export flag when registering at runtime.)
            val sticky = ContextCompat.registerReceiver(
                this,
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                ContextCompat.RECEIVER_EXPORTED
            )
            batteryRegistered = true
            showBattery(sticky)
        }
    }

    override fun onStop() {
        super.onStop()
        if (batteryRegistered) {
            unregisterReceiver(batteryReceiver)
            batteryRegistered = false
        }
    }
}
