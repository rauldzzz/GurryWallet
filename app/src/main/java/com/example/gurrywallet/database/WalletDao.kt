package com.example.gurrywallet.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WalletDao {
    @Insert
    suspend fun insertUser(user: UserEntity)

    @Insert
    suspend fun insertCredencial(credential: CredentialEntity)

    // Saca todas las credenciales de un usuario concreto
    @Query("SELECT * FROM CredentialEntity WHERE userId = :userId")
    suspend fun getCredencialesDeUsuario(userId: String): List<CredentialEntity>
}