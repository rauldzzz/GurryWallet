package com.example.gurrywallet.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, CredentialEntity::class], version = 5)

abstract class GurryWalletDatabase : RoomDatabase() {
    abstract val walletDao: WalletDao

    companion object {
        @Volatile
        private var INSTANCE: GurryWalletDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GurryWalletDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GurryWalletDatabase::class.java,
                    "gurry_wallet_db"
                )
                    // ¡AQUÍ ESTÁ EL TRUCO! Enganchamos el Callback
                    .fallbackToDestructiveMigration() // <--- 1. AÑADE ESTA LÍNEA AQUÍ
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

