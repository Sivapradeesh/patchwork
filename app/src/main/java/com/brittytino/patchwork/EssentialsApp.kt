package com.brittytino.patchwork

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.brittytino.patchwork.services.ScreenOffReceiver
import com.brittytino.patchwork.utils.ShizukuUtils

class EssentialsApp : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    private val screenOffReceiver = ScreenOffReceiver()

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        
        try {
            resources?.configuration
        } catch (e: Exception) {
            
        }

        ShizukuUtils.initialize()
        com.brittytino.patchwork.utils.LogManager.init(this)

        // Init Automation
        com.brittytino.patchwork.domain.diy.DIYRepository.init(this)
        com.brittytino.patchwork.services.automation.AutomationManager.init(this)
        com.brittytino.patchwork.services.CalendarSyncManager.init(this)

        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOffReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(screenOffReceiver, intentFilter)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(screenOffReceiver)
    }
}

