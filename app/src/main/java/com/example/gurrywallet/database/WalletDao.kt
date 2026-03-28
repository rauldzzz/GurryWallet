package com.example.gurrywallet.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Insert
    suspend fun insertUser(user: UserEntity)

    @Insert
    suspend fun insertCredencial(credential: CredentialEntity)

    // Saca todas las credenciales de un usuario concreto
    @Query("SELECT * FROM CredentialEntity WHERE userId = :userId")
    suspend fun getCredencialesDeUsuario(userId: String): List<CredentialEntity>

    //Obtain all Users
    @Query("SELECT * FROM UserEntity")
    fun getAllUsers(): Flow<List<UserEntity>>
}