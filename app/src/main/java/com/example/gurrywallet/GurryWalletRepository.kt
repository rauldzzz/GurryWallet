package com.example.gurrywallet

import com.example.gurrywallet.database.UserEntity
import com.example.gurrywallet.database.WalletDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GurryWalletRepository(
    private val walletDao: WalletDao,
    private val externalScope: CoroutineScope
) {
    private var allUsersList: List<UserEntity> = emptyList()
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        externalScope.launch {
            allUsersList = walletDao.getAllUsers()
            if (allUsersList.isNotEmpty()) {
                _currentUser.value = allUsersList[0]
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

    fun selectPreviousUser() {
        val current = _currentUser.value
        if (allUsersList.isNotEmpty() && current != null) {
            val currentIndex = allUsersList.indexOf(current)
            val prevIndex = (currentIndex - 1 + allUsersList.size) % allUsersList.size
            _currentUser.value = allUsersList[prevIndex]
        }
    }
}