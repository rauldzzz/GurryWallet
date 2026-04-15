package com.example.gurrywallet.screens

import android.annotation.SuppressLint
import android.graphics.Color.alpha
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
fun WalletScreen(
    walletViewModel: WalletViewModel,
    onProofGenerated: (Pair<ByteArray, ByteArray>?) -> Unit){
    val credentialState = walletViewModel.credenciales.collectAsState()

    WalletContent(
        credentials = credentialState.value,
        onRunCredential = { index ->
            walletViewModel.generarPruebaEdad(index)
        },
        getCredentialTitle = { walletViewModel.getCredentialTitle(it)},
        getCredentialIconId = { walletViewModel.getCredentialIconId(it) },
        getCountryFlagId = { walletViewModel.getCountryFlagId(it) },
        onProofGenerated = onProofGenerated
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WalletContent(
    onRunCredential: suspend (Int) -> Pair<ByteArray, ByteArray>?,
    credentials: List<CredentialEntity>,
    getCredentialTitle: (TypeOfCredential) -> String,
    getCredentialIconId: (TypeOfCredential) -> ImageVector,
    getCountryFlagId: (String) -> Int,
    onProofGenerated: (Pair<ByteArray, ByteArray>?) -> Unit
) {
    // CoroutineScope nos permite lanzar tareas en segundo plano desde la UI
    val scope = rememberCoroutineScope()
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
                                    Log.i(
                                        "ZkTest",
                                        "--- INICIANDO PROVER PARA ${getCredentialTitle(credencial.tipo ?: TypeOfCredential.DNI)} ---"
                                    )
                                    scope.launch {
                                        val result = onRunCredential(credencial.indexEnC)
                                        onProofGenerated(result)
                                    }
                                }
                            )
                        }
                    }
                }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
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
            // Simulamos la corrutina devolviendo null
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

            // Iconos nativos de Android / Material para que no reviente el preview
            getCredentialIconId = { type ->
                when (type) {
                    TypeOfCredential.DNI -> Icons.Filled.Badge
                    TypeOfCredential.DRIVER_LICENSE -> Icons.Filled.DirectionsCarFilled
                    TypeOfCredential.HEALTH_CARD -> Icons.Filled.HealthAndSafety
                    TypeOfCredential.PROFESSIONAL_CARD -> Icons.Filled.Work
                    TypeOfCredential.YOUTH_CARD -> Icons.Filled.ChildCare
                }
            },

            // Bandera del país (Ahora está correctamente dentro de los paréntesis)
            getCountryFlagId = { countryCode ->
                when (countryCode.uppercase()) {
                    "ES" -> R.drawable.es
                    "EU" -> R.drawable.ue
                    else -> R.drawable.ic_launcher_background
                }
            },

            // ⚠️ NUEVO: El callback que añadimos para la integración de intents
            onProofGenerated = { _ ->
                // En el preview no hacemos nada con la prueba generada
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