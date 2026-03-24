package com.example.gurrywallet

import android.app.Application
import com.example.gurrywallet.database.GurryWalletDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class GurryWalletApplication: Application(){
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { GurryWalletDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { GurryWalletRepository(database.walletDao, applicationScope) }
}