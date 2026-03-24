package com.example.gurrywallet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gurrywallet.components.NavBar
import com.example.gurrywallet.ui.theme.GurryWalletTheme
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    private val appViewModelFactory = viewModelFactory {
        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GurryWalletApplication)
            SettingsViewModel(app.repository)
        }
        initializer {
            val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GurryWalletApplication)
            WalletViewModel(app.repository)
        }
    }
    private val settingsViewModel: SettingsViewModel by viewModels { appViewModelFactory }
    private val walletViewModel: WalletViewModel by viewModels { appViewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GurryWalletTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavBar(
                        settingsViewModel,
                        walletViewModel
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GurryWalletTheme {

    }
}