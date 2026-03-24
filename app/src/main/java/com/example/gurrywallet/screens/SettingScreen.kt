package com.example.gurrywallet.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gurrywallet.SettingsViewModel
import com.example.gurrywallet.components.ScreenTitle
import com.example.gurrywallet.database.UserEntity
import com.example.gurrywallet.ui.theme.GurryWalletTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel){
    SettingsContent(
        currentUser = settingsViewModel.currentUser,
        onAction = {
            action -> settingsViewModel.onAction(action)
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsContent(
    onAction: (SettingsViewModel.SettingsAction) -> Unit,
    currentUser: StateFlow<UserEntity?>
) {
    GurryWalletTheme {
        Scaffold(
            topBar = {
                ScreenTitle("Settings")
            },
            modifier = Modifier.fillMaxSize()
        ){ paddingValues ->
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding()
                    )
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview(){
    SettingsContent(
        {},
        currentUser = MutableStateFlow<UserEntity?>(null),
    )
}


