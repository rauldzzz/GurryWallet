package com.example.gurrywallet.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, CredentialEntity::class], version = 1)

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
                    .addCallback(WalletDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // ====================================================================
    // CALLBACK: SE EJECUTA LA PRIMERA VEZ QUE SE CREA LA BD
    // ====================================================================
    private class WalletDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    popularBaseDeDatos(database.walletDao)
                }
            }
        }

        // Metemos a mano los datos harcodeados para tu demo
        suspend fun popularBaseDeDatos(dao: WalletDao) {

            // 1. Creamos al usuario Raúl (Mayor de edad)
            dao.insertUser(UserEntity("u_raul"))

            // Le asignamos el DNI (que apunta al índice 0 en tu my_mock.h)
            dao.insertCredencial(
                CredentialEntity(
                    userId = "u_raul",
                    tipo = TypeOfCredential.DNI,
                    indexEnC = 0, // <--- Este es el enlace con tu C++
                    credPicture = "dni_spain_front",
                    country = "ES"
                )
            )

            // 2. Creamos al usuario Lucas (Menor de edad)
            dao.insertUser(UserEntity("u_lucas"))

            // Le asignamos el DNI (apunta al índice 1 en tu my_mock.h)
            dao.insertCredencial(
                CredentialEntity(
                    userId = "u_lucas",
                    tipo = TypeOfCredential.DNI,
                    indexEnC = 1, // <--- Este es el enlace con tu C++
                    credPicture = "dni_spain_front",
                    country = "ES"
                )
            )
        }
    }
}
