package com.brittytino.patchwork.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.brittytino.patchwork.viewmodels.StatusBarIconViewModel

class StatusBarAutomationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_POWER_CONNECTED || action == Intent.ACTION_POWER_DISCONNECTED || action == Intent.ACTION_BOOT_COMPLETED) {
            updateBatteryPercentage(context, action)
        }
    }

    private fun updateBatteryPercentage(context: Context, action: String) {
        val prefs = context.getSharedPreferences("patchwork_prefs", Context.MODE_PRIVATE)
        val mode = prefs.getInt(StatusBarIconViewModel.PREF_BATTERY_PERCENT_MODE, 0)
        val isCharging = action == Intent.ACTION_POWER_CONNECTED

        if (mode == 2) { // Charging only
            val value = if (isCharging) 1 else 0
            updateSystemSetting(context, value)
        } else if (action == Intent.ACTION_BOOT_COMPLETED) {
            val value = if (mode == 1) 1 else 0
            updateSystemSetting(context, value)
        }
    }

    private fun updateSystemSetting(context: Context, value: Int) {
        val key = "status_bar_show_battery_percent"
        var success = false
        try {
            success = Settings.System.putInt(context.contentResolver, key, value)
        } catch (e: Exception) {
            try {
                success = Settings.Secure.putInt(context.contentResolver, key, value)
            } catch (e2: Exception) {
            }
        }

        // Background Shizuku/Root fallback
        if (!success || !Settings.System.getInt(context.contentResolver, key, -1)
                .let { it == value }
        ) {
            if (com.brittytino.patchwork.utils.ShizukuUtils.hasPermission()) {
                com.brittytino.patchwork.utils.ShizukuUtils.runCommand("settings put system $key $value")
                com.brittytino.patchwork.utils.ShizukuUtils.runCommand("settings put secure $key $value")
            } else if (com.brittytino.patchwork.utils.RootUtils.isRootPermissionGranted()) {
                com.brittytino.patchwork.utils.RootUtils.runCommand("settings put system $key $value")
                com.brittytino.patchwork.utils.RootUtils.runCommand("settings put secure $key $value")
            }
        }
    }
}
