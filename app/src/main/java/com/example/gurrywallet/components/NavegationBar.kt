package com.example.gurrywallet.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gurrywallet.SettingsViewModel
import com.example.gurrywallet.WalletViewModel
import com.example.gurrywallet.screens.SettingsScreen
import com.example.gurrywallet.screens.WalletScreen
import com.example.gurrywallet.ui.theme.SettingsPage
import com.example.gurrywallet.ui.theme.WalletPage

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavBar(
    settingsViewModel: SettingsViewModel,
    walletViewModel: WalletViewModel
){
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(value = WalletPage.route) }
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.onSurface,  // Color contenido por defecto
                tonalElevation = 8.dp // Opcional: Sombra/Elevació
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary, // Icono seleccionado
                    selectedTextColor = MaterialTheme.colorScheme.secondary, // Texto seleccionado
                    indicatorColor = Color.Transparent, // La "píldora" de fondo al seleccionar
                    unselectedIconColor = MaterialTheme.colorScheme.primary, // Icono no seleccionado
                    unselectedTextColor = MaterialTheme.colorScheme.primary  // Texto no seleccionado
                )
                NavigationBarItem(
                    selected = currentRoute == WalletPage.route,
                    onClick = {
                        currentRoute = WalletPage.route
                        navController.navigate(WalletPage.route)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = currentRoute == SettingsPage.route,
                    onClick = {
                        currentRoute = SettingsPage.route
                        navController.navigate(SettingsPage.route)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    },
                        label = { Text("Settings") },
                        colors = itemColors
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavigationManager(
            innerPadding,
            navController,
            walletViewModel,
            settingsViewModel
        )
    }
}

@Composable
private fun NavigationManager(
    innerPadding: PaddingValues,
    navController: NavHostController,
    walletViewModel: WalletViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = WalletPage.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(WalletPage.route) {
            WalletScreen(walletViewModel)
        }
        composable(SettingsPage.route) {
            SettingsScreen(settingsViewModel)
        }
    }
}