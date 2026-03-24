package com.example.gurrywallet.screens

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val user by currentUser.collectAsState()
    GurryWalletTheme {
        Scaffold(
            topBar = {
                ScreenTitle("Settings")
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding()
                    )
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), // Altura de la carta
                    shape = RoundedCornerShape(16.dp), // Bordes muy redondeados
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp), // Padding interno para las flechas
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // Separa los elementos a los extremos
                    ) {
                        // Flecha Izquierda
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Last User",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    onAction(SettingsViewModel.SettingsAction.onPreviousUserClick)
                                },
                            tint = MaterialTheme.colorScheme.tertiary
                        )

                        // Nombre del Usuario Actual
                        Text(
                            text = user?.id ?: "Loading...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        // Flecha Derecha
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next User",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    onAction(SettingsViewModel.SettingsAction.onNextUserClick)
                                },
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)// Altura de la carta
                        .clickable {
                            /*TODO Add tutorial*/
                        },
                    shape = RoundedCornerShape(16.dp), // Bordes muy redondeados
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "How to use",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
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


