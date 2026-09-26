package com.ahmedalobaedy.timework

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ahmedalobaedy.timework.data.WorkSessionStore
import com.ahmedalobaedy.timework.service.WorkTrackingService
import com.ahmedalobaedy.timework.ui.TimeWorkApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableImmersiveNavigation()

        val store = WorkSessionStore(applicationContext)
        if (store.activeStartMillis != null) {
            WorkTrackingService.start(applicationContext)
        }

        setContent {
            MaterialTheme {
                TimeWorkApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveNavigation()
        }
    }

    private fun enableImmersiveNavigation() {
        WindowCompat.setDecorFitsSystemWindows(window, true)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
