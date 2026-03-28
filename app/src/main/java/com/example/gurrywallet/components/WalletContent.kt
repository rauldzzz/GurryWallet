package com.example.gurrywallet.components

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurrywallet.R
import com.example.gurrywallet.database.CredentialEntity
import com.example.gurrywallet.database.TypeOfCredential
import com.example.gurrywallet.ui.theme.GurryWalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialCard(
    entity: CredentialEntity,
    getCredentialTitle: (TypeOfCredential) -> String,
    getCredentialIconId: (TypeOfCredential) -> ImageVector,
    getCountryFlagId: (String) -> Int,
    onCardClick: () -> Unit // Clic en toda la card
) {
    val portait = getDrawableIdByName(context = LocalContext.current, entity.credPicture)
    Log.i("Debug", "Portait: $portait ${entity.credPicture}")
    // Usamos Card de Material3 para tener sombra y bordes redondeados
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary // Un color beige claro similar al diseño
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // Centramos todo verticalmente
        ) {
            // Columna 1: Foto de perfil (izquierda)
            val portait = getDrawableIdByName(context = LocalContext.current, entity.credPicture)

            if (portait != 0) {
                // Si encontró la imagen correcta en la base de datos, la pinta
                Image(
                    painter = painterResource(id = portait),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(40.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Si no hay foto en la BD (o devuelve 0), pintamos un icono genérico para que NO explote
                Icon(
                    imageVector = Icons.Filled.AccountBox, // O el icono de Icons que prefieras
                    contentDescription = "Profile Picture Placeholder",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(40.dp)),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            // Columna 2: Título e Icono principal (centro)
            Column(
                modifier = Modifier.weight(1f), // Toma el espacio disponible en el centro
                horizontalAlignment = Alignment.Start
            ) {
                // Fila de arriba: Título del tipo
                // Usamos una valor por defecto si el tipo es null
                val type = entity.tipo ?: TypeOfCredential.DNI
                Text(
                    text = getCredentialTitle(type),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1, // <--- AÑADIDO: Fuerza a que sea 1 sola línea
                    overflow = TextOverflow.Ellipsis
                )

                // Fila de abajo: Icono de la credencial
                Spacer(modifier = Modifier.height(5.dp))
                Icon(
                    imageVector = getCredentialIconId(type),
                    contentDescription = "Credential Icon",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(60.dp)

                )
            }
                // Espaciado para asegurar que esté al final verticalmente si la card es muy alta
                // Spacer(modifier = Modifier.weight(1f)) // Comentado por ahora, Row se encarga si no hay más elementos arriba
                Image(
                    painter = painterResource(id = getCountryFlagId(entity.country)),
                    contentDescription = "Country Flag",
                    modifier = Modifier
                        .size(45.dp)// Tamaño de la bandera
                        .align(Alignment.Bottom),
                    contentScale = ContentScale.Fit
                )
            }
    }
}

@DrawableRes
fun getDrawableIdByName(context: Context, drawableName: String): Int {
    return context.resources.getIdentifier(
        drawableName,
        "drawable",
        context.packageName
    )
}

@Preview
@Composable
fun CredentialCardPreview() {
    GurryWalletTheme {
        // 1. Creamos una entidad de prueba con todos los datos necesarios
        val dummyEntity = CredentialEntity(
            idCredencial = 1,
            userId = "u_raul",
            tipo = TypeOfCredential.DNI,
            indexEnC = 0,
            credPicture = "",
            country = "ES"
        )

        // 2. Le pasamos un poco de padding para que no se pegue a los bordes en el preview
        Column(modifier = Modifier.padding(16.dp)) {
            CredentialCard(
                entity = dummyEntity,
                // 3. Simulamos lo que devolvería tu ViewModel
                getCredentialTitle = { "Personal ID" },
                getCredentialIconId = { Icons.Filled.Badge }, // Simula el ID del icono
                getCountryFlagId = { R.drawable.es },        // Simula el ID de la bandera
                onCardClick = { /* No hace nada en el preview */ }
            )
        }
    }
}