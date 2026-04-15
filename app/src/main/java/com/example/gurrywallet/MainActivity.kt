package com.example.gurrywallet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            WalletViewModel(app.repository, app.cacheDir.absolutePath)
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
                        walletViewModel,
                        onProofReady = { resultPair ->
                            if (resultPair != null){
                                Toast.makeText(this@MainActivity, "The proof has been sent correctly", Toast.LENGTH_SHORT).show()
                                val resultIntent = Intent().apply {
                                    putExtra("zk_proof", resultPair.first)
                                    putExtra("zk_transcript", resultPair.second)
                                }
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            } else{
                                Toast.makeText(this@MainActivity, "Error generating proof", Toast.LENGTH_SHORT).show()
                            }

                        }
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