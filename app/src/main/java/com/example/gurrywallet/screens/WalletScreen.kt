package com.example.gurrywallet.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gurrywallet.WalletViewModel
import com.example.gurrywallet.ui.theme.GurryWalletTheme

@Composable
fun WalletScreen(walletViewModel: WalletViewModel){
    WalletContent(
        onRunCredential = { index ->
            walletViewModel.generarPruebaEdad(index)
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WalletContent(
    onRunCredential: suspend (Int) -> Boolean
){
    // CoroutineScope nos permite lanzar tareas en segundo plano desde la UI
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    GurryWalletTheme {
        Scaffold(modifier = Modifier.fillMaxSize()){
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

            }
        }
    }
}

@Preview
@Composable
fun WalletScreenPreview(){

    WalletContent({ index ->
        true
    });
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