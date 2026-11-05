package com.example.acis.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.acis.data.Clima
import com.example.acis.data.DatosDemo
import com.example.acis.data.EstadoOt
import com.example.acis.data.Prioridad

// ---------------------------------------------------------------------
// Barra superior y navegación
// ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

fun cerrarSesion(navController: NavController) {
    navController.navigate("login") {
        popUpTo(navController.graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun BotonCerrarSesion(navController: NavController) {
    IconButton(onClick = { cerrarSesion(navController) }) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
    }
}

/** Barra inferior del planificador: Panel y Catastro. */
@Composable
fun BarraPlanificador(navController: NavController, seleccion: Int) {
    NavigationBar {
        NavigationBarItem(
            selected = seleccion == 0,
            onClick = {
                if (seleccion != 0) navController.popBackStack("panel", false)
            },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Panel de OT") }
        )
        NavigationBarItem(
            selected = seleccion == 1,
            onClick = {
                if (seleccion != 1) navController.navigate("catastro") { launchSingleTop = true }
            },
            icon = { Icon(Icons.Default.Build, contentDescription = null) },
            label = { Text("Catastro") }
        )
    }
}

// ---------------------------------------------------------------------
// Etiquetas (chips de solo lectura)
// ---------------------------------------------------------------------

@Composable
fun Etiqueta(texto: String, fondo: Color, contenido: Color) {
    Surface(color = fondo, contentColor = contenido, shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun EtiquetaEstado(estado: EstadoOt) {
    val (fondo, texto) = when (estado) {
        EstadoOt.CREADA -> Color(0xFFE3E8EE) to Color(0xFF37474F)
        EstadoOt.PENDIENTE_ASIGNACION -> Color(0xFFFFE9B3) to Color(0xFF5C4300)
        EstadoOt.ASIGNADA -> Color(0xFFDCEBF8) to Color(0xFF083B66)
        EstadoOt.ACEPTADA -> Color(0xFFD3F0EC) to Color(0xFF00574D)
        EstadoOt.EN_EJECUCION -> Color(0xFFE2E0FA) to Color(0xFF2E2A7A)
        EstadoOt.PENDIENTE_IMPREVISTO -> Color(0xFFFFDAD6) to Color(0xFF8C1D18)
        EstadoOt.CERRADA -> Color(0xFFD6F0D8) to Color(0xFF1B5E20)
    }
    Etiqueta(estado.etiqueta, fondo, texto)
}

@Composable
fun EtiquetaPrioridad(prioridad: Prioridad) {
    val (fondo, texto) = when (prioridad) {
        Prioridad.ALTA -> Color(0xFFFFDAD6) to Color(0xFF8C1D18)
        Prioridad.MEDIA -> Color(0xFFFFE9B3) to Color(0xFF5C4300)
        Prioridad.BAJA -> Color(0xFFD6F0D8) to Color(0xFF1B5E20)
    }
    Etiqueta("Prioridad ${prioridad.etiqueta}", fondo, texto)
}

@Composable
fun EtiquetaClima(clima: Clima) {
    Etiqueta("${clima.icono} ${clima.etiqueta}", Color(0xFFFFF4D6), Color(0xFF5C4300))
}

// ---------------------------------------------------------------------
// Banners de aviso
// ---------------------------------------------------------------------

enum class TipoBanner(val fondo: Color, val texto: Color) {
    INFO(Color(0xFFDCEBF8), Color(0xFF083B66)),
    AVISO(Color(0xFFFFF4D6), Color(0xFF5C4300)),
    PELIGRO(Color(0xFFFFE0DC), Color(0xFF8C1D18)),
    OK(Color(0xFFDDF2DE), Color(0xFF1B5E20))
}

@Composable
fun BannerAlerta(
    icono: String,
    titulo: String,
    mensaje: String,
    tipo: TipoBanner,
    modifier: Modifier = Modifier,
    acciones: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = tipo.fondo, contentColor = tipo.texto)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Text(icono, fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold)
                Text(mensaje, style = MaterialTheme.typography.bodyMedium)
                if (acciones != null) acciones()
            }
        }
    }
}

/** Aviso del estado de conexión (modo sin conexión y sincronización). */
@Composable
fun AvisoConexion() {
    if (DatosDemo.sinConexion) {
        BannerAlerta(
            icono = "📡",
            titulo = "Sin conexión",
            mensaje = "Tu trabajo se guarda en el equipo (${DatosDemo.pendientesSync} registros pendientes) " +
                "y se enviará solo al recuperar la señal.",
            tipo = TipoBanner.AVISO
        )
    } else if (DatosDemo.mensajeSync.isNotEmpty()) {
        BannerAlerta(
            icono = "✅",
            titulo = "Sincronizado",
            mensaje = DatosDemo.mensajeSync,
            tipo = TipoBanner.OK
        )
    }
}

/** Interruptor para simular que el teléfono pierde la señal. */
@Composable
fun SelectorSinConexion() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Simular sin señal", fontWeight = FontWeight.Bold)
                Text(
                    "Solo para el prototipo: prueba el modo sin conexión.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = DatosDemo.sinConexion,
                onCheckedChange = { DatosDemo.cambiarConexion(it) }
            )
        }
    }
}

// ---------------------------------------------------------------------
// Textos y filas de datos
// ---------------------------------------------------------------------

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
fun FilaDato(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(0.38f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            modifier = Modifier.weight(0.62f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SeparadorVertical(alto: Int = 8) {
    Spacer(Modifier.height(alto.dp))
}

// ---------------------------------------------------------------------
// Listas desplegables
// ---------------------------------------------------------------------

/** Campo de formulario que abre una lista de opciones. */
@Composable
fun CampoDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    error: Boolean = false
) {
    var expandido by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            isError = error,
            label = { Text(etiqueta) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que recibe el toque para abrir la lista
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = habilitado) { expandido = true }
        )
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Filtro en forma de chip que abre una lista de opciones. La primera opción es "Todos". */
@Composable
fun FiltroDesplegable(
    titulo: String,
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = seleccion != opciones.first(),
            onClick = { expandido = true },
            label = { Text("$titulo: $seleccion") }
        )
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Recuadro con un número grande y un título (resúmenes del panel). */
@Composable
fun TarjetaResumen(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
