package com.example.gurrywallet

import androidx.lifecycle.ViewModel
import com.example.gurrywallet.database.WalletDao

class SettingsViewModel(
    private val gurryWalletRepository: GurryWalletRepository
): ViewModel() {

    val currentUser = gurryWalletRepository.currentUser

    sealed interface SettingsAction {
        object onNextUserClick : SettingsAction
        object onPreviousUserClick : SettingsAction
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.onNextUserClick -> gurryWalletRepository.selectNextUser()
            is SettingsAction.onPreviousUserClick -> gurryWalletRepository.selectPreviousUser()
        }
    }

}