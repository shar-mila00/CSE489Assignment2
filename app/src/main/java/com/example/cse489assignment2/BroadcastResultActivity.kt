package com.example.cse489assignment2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.example.cse489assignment2.databinding.ActivityBroadcastResultBinding

/**
 * Third activity of the Broadcast Receiver flow.
 *
 * 1. Custom selection: registers a dynamically created custom broadcast
 *    receiver and receives the text message that was entered in the
 *    second activity.
 * 2. Battery selection: receives the system battery broadcast and shows the
 *    live battery percentage and Charging / Not Charging status.
 */
class BroadcastResultActivity : AppCompatActivity() {

    companion object {
        // Action of the custom broadcast sent by this flow.
        const val ACTION_CUSTOM_BROADCAST = "com.example.cse489assignment2.ACTION_CUSTOM_BROADCAST"
        const val EXTRA_TEXT = "extra_text"
    }

    private lateinit var binding: ActivityBroadcastResultBinding
    private var message: String? = null
    private var registered = false
    private var isBattery = false
    private var batteryRegistered = false

    /** System battery receiver (used when the battery option was selected). */
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showBattery(intent)
        }
    }

    private fun showBattery(intent: Intent?) {
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        if (level < 0 || scale <= 0) return
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        binding.tvReceived.text = getString(R.string.battery_level, level * 100 / scale) +
                "\n" + getString(
            if (isCharging) R.string.battery_status_charging
            else R.string.battery_status_not_charging
        )
    }

    /** Custom broadcast receiver created in this activity. */
    private val customReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val received = intent?.getStringExtra(EXTRA_TEXT) ?: return
            binding.tvReceived.text = getString(R.string.received_message, received)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBroadcastResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back arrow in the action bar + a Back button on screen.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnBack.setOnClickListener { finish() }

        isBattery = intent.getStringExtra(BroadcastReceiverActivity.EXTRA_TYPE) ==
                BroadcastReceiverActivity.TYPE_BATTERY
        message = intent.getStringExtra(EXTRA_TEXT)
        if (isBattery) binding.tvReceived.text = getString(R.string.battery_loading)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStart() {
        super.onStart()

        if (isBattery) {
            // Sticky broadcast: the current value is returned immediately,
            // then the receiver keeps updating the screen on every change.
            val sticky = ContextCompat.registerReceiver(
                this,
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                ContextCompat.RECEIVER_EXPORTED
            )
            batteryRegistered = true
            showBattery(sticky)
            return
        }

        message ?: return

        // Register the custom receiver first, then send the custom broadcast
        // carrying the text message given in the second activity.
        // Android 14+ requires an explicit export flag for non-system broadcasts.
        ContextCompat.registerReceiver(
            this,
            customReceiver,
            IntentFilter(ACTION_CUSTOM_BROADCAST),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        registered = true
        sendBroadcast(
            Intent(ACTION_CUSTOM_BROADCAST)
                .setPackage(packageName)
                .putExtra(EXTRA_TEXT, message)
        )
    }

    override fun onStop() {
        super.onStop()
        if (batteryRegistered) {
            unregisterReceiver(batteryReceiver)
            batteryRegistered = false
        }
        if (registered) {
            unregisterReceiver(customReceiver)
            registered = false
        }
    }
}
