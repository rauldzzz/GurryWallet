package com.example.gurrywallet.ui.theme

interface Routable {
    val route: String
}

object WalletPage: Routable {
    override val route: String = "main/walletpage"
}

object SettingsPage: Routable {
    override val route: String = "main/settingspage"
}