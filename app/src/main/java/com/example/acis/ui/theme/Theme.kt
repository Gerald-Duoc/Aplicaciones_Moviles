package com.example.acis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Solo tema claro, para que la app se vea igual en todos los dispositivos (y en el video).
private val EsquemaClaro = lightColorScheme(
    primary = AzulAcis,
    onPrimary = Color.White,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulOscuro,
    secondary = Turquesa,
    onSecondary = Color.White,
    background = FondoClaro,
    surface = Color.White,
    surfaceVariant = Color(0xFFE6ECF2),
    error = RojoAlerta
)

@Composable
fun AcisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        content = content
    )
}
