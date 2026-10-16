package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.sentinel.ui.SentinelMainScreen
import com.example.sentinel.ui.SentinelViewModel
import com.example.ui.theme.SentinelTheme

class MainActivity : ComponentActivity() {

    private val sentinelViewModel: SentinelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SentinelTheme(darkTheme = true) {
                SentinelMainScreen(viewModel = sentinelViewModel)
            }
        }
    }
}
