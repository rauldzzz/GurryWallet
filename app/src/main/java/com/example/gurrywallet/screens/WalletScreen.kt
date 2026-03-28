package com.example.gurrywallet.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gurrywallet.R
import com.example.gurrywallet.WalletViewModel
import com.example.gurrywallet.components.CredentialCard
import com.example.gurrywallet.components.ScreenTitle
import com.example.gurrywallet.database.CredentialEntity
import com.example.gurrywallet.database.TypeOfCredential
import com.example.gurrywallet.ui.theme.GurryWalletTheme
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(walletViewModel: WalletViewModel){
    val credentialState = walletViewModel.credenciales.collectAsState()

    WalletContent(
        credentials = credentialState.value,
        onRunCredential = { index ->
            walletViewModel.generarPruebaEdad(index)
        },
        getCredentialTitle = { walletViewModel.getCredentialTitle(it)},
        getCredentialIconId = { walletViewModel.getCredentialIconId(it) },
        getCountryFlagId = { walletViewModel.getCountryFlagId(it) },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WalletContent(
    onRunCredential: suspend (Int) -> ByteArray?,
    credentials: List<CredentialEntity>,
    getCredentialTitle: (TypeOfCredential) -> String,
    getCredentialIconId: (TypeOfCredential) -> ImageVector,
    getCountryFlagId: (String) -> Int
) {
    // CoroutineScope nos permite lanzar tareas en segundo plano desde la UI
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    GurryWalletTheme {
        Scaffold(
            topBar = {
                ScreenTitle("GurryWallet")
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding()
                    )
                    .fillMaxSize(),
            ) {
                LazyColumn( // Usamos LazyColumn para que sea eficiente si hay muchas
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(credentials) { credencial ->
                        // Para cada credencial de la lista, llamamos a CredentialCard
                        CredentialCard(
                            entity = credencial,
                            getCredentialTitle = getCredentialTitle,
                            getCredentialIconId = getCredentialIconId,
                            getCountryFlagId = getCountryFlagId,
                            onCardClick = {
                                // Aquí puedes manejar el clic si quieres
                                Log.i(
                                    "ZkTest",
                                    "--- INICIANDO PROVER PARA ${getCredentialTitle(credencial.tipo ?: TypeOfCredential.DNI)} ---"
                                )
                                scope.launch {
                                    isLoading = true
                                    val result = onRunCredential(credencial.indexEnC)
                                    isLoading = false
                                    if (result != null) {
                                        Log.i(
                                            "ZkTest",
                                            "✅ ÉXITO. El circuito validó la credencial."
                                        )
                                    } else {
                                        Log.e("ZkTest", "❌ ERROR inesperado.")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun WalletScreenPreview() {
    // 1. Creamos una lista ficticia con un par de credenciales para ver cómo funciona el LazyColumn
    val dummyCredentials = listOf(
        CredentialEntity(
            idCredencial = 1,
            userId = "u_raul",
            tipo = TypeOfCredential.DNI,
            indexEnC = 0,
            credPicture = "user1",
            country = "ES"
        ),
        CredentialEntity(
            idCredencial = 2,
            userId = "u_raul",
            tipo = TypeOfCredential.HEALTH_CARD,
            indexEnC = 1,
            credPicture = "user2",
            country = "EU"
        )
    )

    // 2. Renderizamos el Content pasando datos simulados
    GurryWalletTheme {
        WalletContent(
            credentials = dummyCredentials,
            // Simulamos la corrutina devolviendo null (como si fallara la prueba, da igual para el diseño)
            onRunCredential = { _ -> null },

            // Mapeo falso para los títulos
            getCredentialTitle = { type ->
                when (type) {
                    TypeOfCredential.DNI -> "Personal ID"
                    TypeOfCredential.DRIVER_LICENSE -> "Driver Card"
                    TypeOfCredential.HEALTH_CARD -> "European Health Insurance Card"
                    TypeOfCredential.PROFESSIONAL_CARD -> "European Professional Card"
                    TypeOfCredential.YOUTH_CARD -> "Youth Card"
                }
            },

            // ⚠️ IMPORTANTE: Uso iconos nativos de Android para asegurar que NO devuelva 0 y reviente (error 0x0).
            // Puedes cambiarlos por tus R.drawable.pasaporte o R.drawable.seguro si quieres que se vea más real.
            getCredentialIconId = { type ->
                when (type) {
                TypeOfCredential.DNI -> Icons.Filled.Badge // Pon aquí tu icono de pasaporte
                TypeOfCredential.DRIVER_LICENSE -> Icons.Filled.DirectionsCarFilled // Pon aquí tu icono de coche
                TypeOfCredential.HEALTH_CARD -> Icons.Filled.HealthAndSafety // Pon aquí tu icono de escudo/salud
                TypeOfCredential.PROFESSIONAL_CARD -> Icons.Filled.Work // Pon aquí tu icono de profesional
                TypeOfCredential.YOUTH_CARD -> Icons.Filled.ChildCare // Pon aquí tu icono de juventud
            } },
            getCountryFlagId = { countryCode ->
                when (countryCode.uppercase()) {
                    "ES" -> R.drawable.es // Tu bandera de España
                    "EU" -> R.drawable.ue // Tu bandera de la UE
                    else -> R.drawable.ic_launcher_background // Bandera por defecto o icono de error
                }
            }
        )
    }
}

/*
Button(onClick = {
                    // Llamamos a la función que nos han pasado por parámetro
                    Log.i("ZkTest", "--- INICIANDO PROVER PARA RAÚL (Índice 0) ---")
                    scope.launch {
                        val exito = onRunCredential(0)
                        isLoading = false
                        if (exito) {
                            Log.i("ZkTest", "✅ ÉXITO. El circuito validó a Raúl.")
                        } else {
                            Log.e("ZkTest", "❌ ERROR inesperado en Raúl.")
                        }
                    }
                }) {
                    Text("Probar Raúl (Mayor)")
                }
* */