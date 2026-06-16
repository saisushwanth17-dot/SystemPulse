package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.navigation.MainAppShell
import com.example.presentation.viewmodel.SystemPulseViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the system observer ViewModel passing context.
        val viewModel = SystemPulseViewModel(applicationContext)

        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppShell(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

