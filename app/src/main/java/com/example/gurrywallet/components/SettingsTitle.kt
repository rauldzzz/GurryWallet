package com.example.gurrywallet.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ScreenTitle(title: String){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Margin
        horizontalAlignment = Alignment.CenterHorizontally, // Separa izquierda y derecha al máximo
    ) {
        Text(
            title,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}

