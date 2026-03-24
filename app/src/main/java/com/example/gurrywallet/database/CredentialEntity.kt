package com.example.gurrywallet.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TypeOfCredential {
    DNI, DRIVER_LICENSE, HEALTH_CARD, PROFESSIONAL_CARD, YOUTH_CARD
}

@Entity
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val idCredencial: Int = 0,
    val userId: String = "",             // Para saber de qué usuario es
    val tipo: TypeOfCredential? = null,        // Enum (Room lo guarda como String o Int)
    val indexEnC: Int = -1,                 // El '0' o '1' que le pasarás a C++
    val credPicture: String = "",           // URL o nombre de recurso en drawable (ej: "dni_es")
    val country: String = ""
)