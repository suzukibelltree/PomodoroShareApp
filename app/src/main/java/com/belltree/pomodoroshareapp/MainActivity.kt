package com.belltree.pomodoroshareapp

// Compose の Surface を使うため android.view.Surface を削除し material3 を使用
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.belltree.pomodoroshareapp.notification.NotificationPermissionManager
import com.belltree.pomodoroshareapp.ui.theme.PomodoroShareAppTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

object AppContextHolder {
    lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationPermissionManager: NotificationPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 通知権限を要求する
        notificationPermissionManager.requestNotificationPermission(this)

        setContent {
            PomodoroShareAppTheme{
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}
