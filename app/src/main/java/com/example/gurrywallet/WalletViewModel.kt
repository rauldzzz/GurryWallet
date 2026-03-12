package com.example.gurrywallet

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletViewModel : ViewModel() {

    init {
        System.loadLibrary("gurry_native")
    }

    // La función nativa JNI sigue siendo idéntica (external = native en Kotlin)
    private external fun ejecutarPruebaZkNativa(indiceCredencial: Int): Int

    // "suspend" significa que puede pausarse y ejecutarse en segundo plano
    suspend fun generarPruebaEdad(indice: Int): Boolean {
        // Enviar la carga de trabajo a los hilos IO (Background)
        return withContext(Dispatchers.IO) {
            val resultado = ejecutarPruebaZkNativa(indice)
            resultado == 1
        }
    }
}