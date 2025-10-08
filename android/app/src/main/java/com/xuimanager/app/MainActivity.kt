package com.xuimanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import com.xuimanager.app.ui.XuiManagerApp
import com.xuimanager.app.ui.XuiViewModel
import com.xuimanager.app.ui.XuiViewModelFactory
import com.xuimanager.app.data.SessionPreferences

class MainActivity : ComponentActivity() {
    private val sessionPreferences by lazy { SessionPreferences(applicationContext) }
    private val viewModel: XuiViewModel by viewModels {
        XuiViewModelFactory(sessionPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    XuiManagerApp(viewModel = viewModel)
                }
            }
        }
    }
}
