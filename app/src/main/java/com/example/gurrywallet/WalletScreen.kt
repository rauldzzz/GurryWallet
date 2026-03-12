package com.example.gurrywallet

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gurrywallet.ui.theme.GurryWalletTheme
import kotlinx.coroutines.launch

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
                verticalArrangement = Arrangement.Center
            ) {
                // Indicador visual
                if (isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generando prueba Zero-Knowledge...")
                    Text("(La primera vez tarda unos 7-10 segundos)")
                    Spacer(modifier = Modifier.height(32.dp))
                }
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
                Button(onClick = {
                    Log.i("ZkTest", "--- INICIANDO PROVER PARA LUCAS (Índice 1) ---")

                    scope.launch {
                        val exito = onRunCredential(1)
                        isLoading = false
                        if (exito) {
                            Log.i("ZkTest", "✅ ÉXITO. El circuito validó a Raúl.")
                        } else {
                            Log.e("ZkTest", "❌ ERROR inesperado en Raúl.")
                        }
                    }
                }) {
                    Text("Probar Lucas (Menor)")
                }
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