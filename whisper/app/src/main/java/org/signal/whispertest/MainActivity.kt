package org.signal.whispertest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import org.signal.whispertest.ui.main.MainScreen
import org.signal.whispertest.ui.main.MainScreenViewModel
import org.signal.whispertest.ui.theme.WhisperTestTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainScreenViewModel by viewModels { MainScreenViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
          WhisperTestTheme {
                MainScreen(viewModel)
            }
        }
    }
}