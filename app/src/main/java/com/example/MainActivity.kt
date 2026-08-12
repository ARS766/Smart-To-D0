package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ui.DashboardScreen
import com.example.ui.TaskViewModel
import com.example.ui.theme.SmartReminderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate TaskViewModel with our application context factory
        val viewModel = ViewModelProvider(
            this,
            TaskViewModel.Factory(application)
        )[TaskViewModel::class.java]

        setContent {
            val currentPalette by viewModel.currentPalette.collectAsState()
            val isDark by viewModel.isDarkMode.collectAsState()
            val isOled by viewModel.isOledMode.collectAsState()

            SmartReminderTheme(
                palette = currentPalette,
                isDark = isDark,
                isOled = isOled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
