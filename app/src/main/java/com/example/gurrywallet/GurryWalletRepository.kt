package com.example.gurrywallet

import android.util.Log
import com.example.gurrywallet.database.CredentialEntity
import com.example.gurrywallet.database.TypeOfCredential
import com.example.gurrywallet.database.UserEntity
import com.example.gurrywallet.database.WalletDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GurryWalletRepository(
    private val walletDao: WalletDao,
    private val externalScope: CoroutineScope
) {
    private var allUsersList: List<UserEntity> = emptyList()
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        externalScope.launch(Dispatchers.IO) { // Lanzamos en hilo de Background
            Log.e("GURRY_DEBUG", "1. Repositorio iniciado. Comprobando la Base de Datos...")

            // Usamos .first() para leer la base de datos UNA SOLA VEZ como si fuera una foto
            val usuariosActuales = walletDao.getAllUsers().first()

            if (usuariosActuales.isEmpty()) {
                Log.e("GURRY_DEBUG", "2. La BD está Vacia. Inyectando a Raúl y Lucas...")

                // Insertamos a Raúl
                walletDao.insertUser(UserEntity("u_raul"))
                walletDao.insertCredencial(
                    CredentialEntity(userId = "u_raul", tipo = TypeOfCredential.DNI, indexEnC = 0, credPicture = "user1", country = "es")
                )

                // Insertamos a Lucas
                walletDao.insertUser(UserEntity("u_lucas"))
                walletDao.insertCredencial(
                    CredentialEntity(userId = "u_lucas", tipo = TypeOfCredential.DNI, indexEnC = 1, credPicture = "user2", country = "es")
                )
                Log.e("GURRY_DEBUG", "3. ¡Inyección completada con éxito!")
            } else {
                Log.e("GURRY_DEBUG", "2. La BD ya tiene datos (${usuariosActuales.size} usuarios). No inyectamos nada.")
            }

            Log.e("GURRY_DEBUG", "4. Nos suscribimos al Flow para escuchar cambios...")

            // Ahora sí, nos quedamos escuchando permanentemente
            walletDao.getAllUsers().collect { users ->
                Log.e("GURRY_DEBUG", "5. Flow detectó un cambio. Total usuarios: ${users.size}")
                allUsersList = users

                // Si hay usuarios y no hemos seleccionado a ninguno, cogemos el primero
                if (users.isNotEmpty() && _currentUser.value == null) {
                    _currentUser.value = users[0]
                    Log.e("GURRY_DEBUG", "6. Usuario activo asignado automáticamente.")
                }
            }
        }
    }
    fun selectNextUser() {
        val current = _currentUser.value
        if (allUsersList.isNotEmpty() && current != null) {
            val currentIndex = allUsersList.indexOf(current)
            val nextIndex = (currentIndex + 1) % allUsersList.size
            _currentUser.value = allUsersList[nextIndex]
        }
    }

    suspend fun getCredencialesDeUsuario(userId: String): List<CredentialEntity> {
        return walletDao.getCredencialesDeUsuario(userId)
    }

    fun selectPreviousUser() {
        val current = _currentUser.value
        if (allUsersList.isNotEmpty() && current != null) {
            val currentIndex = allUsersList.indexOf(current)
            val prevIndex = (currentIndex - 1 + allUsersList.size) % allUsersList.size
            _currentUser.value = allUsersList[prevIndex]
        }
    }
}