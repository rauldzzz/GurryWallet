package com.example.gurrywallet

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gurrywallet.database.CredentialEntity
import com.example.gurrywallet.database.TypeOfCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletViewModel(
    repository: GurryWalletRepository,
    private val cacheDir: String) : ViewModel()
{

    val currentUser = repository.currentUser
    private val _credencials = MutableStateFlow<List<CredentialEntity>>(emptyList())
    val credenciales: StateFlow<List<CredentialEntity>> = _credencials.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        System.loadLibrary("gurry_native")

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    // Si hay un usuario seleccionado, buscamos sus credenciales en la BD
                    _credencials.value = repository.getCredencialesDeUsuario(user.id)
                } else {
                    // Si por algún motivo no hay usuario, vaciamos la lista
                    _credencials.value = emptyList()
                }
            }
        }
    }

    fun getCredentialTitle(type: TypeOfCredential): String {
        return when (type) {
            TypeOfCredential.DNI -> "Personal ID"
            TypeOfCredential.DRIVER_LICENSE -> "Driver Card"
            TypeOfCredential.HEALTH_CARD -> "European Health Insurance Card"
            TypeOfCredential.PROFESSIONAL_CARD -> "European Professional Card"
            TypeOfCredential.YOUTH_CARD -> "Youth Card"
        }
    }


    fun getCredentialIconId(type: TypeOfCredential): ImageVector {
        return when (type) {
            TypeOfCredential.DNI -> Icons.Filled.Badge // Pon aquí tu icono de pasaporte
            TypeOfCredential.DRIVER_LICENSE -> Icons.Filled.DirectionsCarFilled // Pon aquí tu icono de coche
            TypeOfCredential.HEALTH_CARD -> Icons.Filled.HealthAndSafety // Pon aquí tu icono de escudo/salud
            TypeOfCredential.PROFESSIONAL_CARD -> Icons.Filled.Work // Pon aquí tu icono de profesional
            TypeOfCredential.YOUTH_CARD -> Icons.Filled.ChildCare // Pon aquí tu icono de juventud
        }
    }

    @DrawableRes
    fun getCountryFlagId(countryCode: String): Int {
        return when (countryCode.uppercase()) {
            "ES" -> R.drawable.es // Tu bandera de España
            "EU" -> R.drawable.ue // Tu bandera de la UE
            else -> R.drawable.ic_launcher_background // Bandera por defecto o icono de error
        }
    }

    // La función nativa JNI sigue siendo idéntica (external = native en Kotlin)
    private external fun ejecutarPruebaZkNativa(indiceCredencial: Int, cacheDir: String): Array<ByteArray>?

    // "suspend" significa que puede pausarse y ejecutarse en segundo plano
    suspend fun generarPruebaEdad(indice: Int): Pair<ByteArray, ByteArray>? {
        _isLoading.value = true
        return try {
            withContext(Dispatchers.IO) {
                val result = ejecutarPruebaZkNativa(indice, cacheDir)
                if (result != null && result.size == 2) {
                    Pair(result[0], result[1])
                } else {
                    null
                }
            }
        } finally {
            _isLoading.value = false
        }
    }
}