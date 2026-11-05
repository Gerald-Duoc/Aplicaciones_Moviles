package com.example.acis.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.acis.data.DatosDemo
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import com.example.acis.data.RegistroEjecucion
import com.example.acis.data.minutosATexto
import com.example.acis.ui.componentes.AvisoConexion
import com.example.acis.ui.componentes.BannerAlerta
import com.example.acis.ui.componentes.BarraSuperior
import com.example.acis.ui.componentes.FilaDato
import com.example.acis.ui.componentes.SelectorSinConexion
import com.example.acis.ui.componentes.TipoBanner
import com.example.acis.ui.componentes.TituloSeccion
import com.example.acis.viewmodel.EjecucionOtViewModel

private val PASOS = listOf("Check-in", "Diagnóstico", "Evidencia", "Cierre")

/**
 * Pantalla 4 - Ejecución de la OT (registro de la visita en terreno).
 * HU-17 check-in con autorización de ubicación, HU-18 check-out y tiempo real,
 * HU-19 diagnóstico, HU-20 actividades, HU-21 materiales, HU-22 fotos antes/después,
 * HU-23 cerrar con datos obligatorios, HU-24 no cerrar si falta información,
 * HU-25 reportar imprevisto, HU-03/04 explicación de ubicación y control.
 *
 * MVVM: orquestación/validaciones en [EjecucionOtViewModel]; [RegistroEjecucion] mantiene
 * estado observable (mutableStateOf/list) para los campos del formulario.
 */
@Composable
fun EjecucionOtScreen(
    navController: NavController,
    otId: String,
    vm: EjecucionOtViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val reg = vm.registro()

    LaunchedEffect(otId) { vm.cargar(otId) }

    LaunchedEffect(state.navegarAtras) {
        if (state.navegarAtras) {
            vm.consumirNavegacion()
            navController.popBackStack()
        }
    }
    LaunchedEffect(state.cerrarCompletado) {
        if (state.cerrarCompletado) {
            vm.consumirNavegacion()
            navController.popBackStack("mis_ot", false)
        }
    }

    val ot = state.ot
    Scaffold(
        topBar = { BarraSuperior(titulo = "Visita $otId", onVolver = { navController.popBackStack() }) },
        bottomBar = {
            if (ot != null && ot.estado != EstadoOt.CERRADA) {
                BarraAccionesEjecucion(
                    onImprevisto = { vm.abrirImprevisto() },
                    onCerrar = { vm.cerrarOt(reg) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (ot == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontró la OT $otId.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IndicadorPasos(state.paso) { vm.setPaso(it) }
            SelectorSinConexion()
            AvisoConexion()
            if (ot.estado == EstadoOt.CERRADA) {
                BannerAlerta(
                    icono = "✅",
                    titulo = "OT cerrada",
                    mensaje = "El registro quedó guardado. Tiempo de atención: ${minutosATexto(ot.tiempoRealMin ?: 0)}.",
                    tipo = TipoBanner.OK
                )
            }
            when (state.paso) {
                0 -> PasoCheckIn(ot, reg, onGuardarCheckIn = { vm.guardarCheckIn(reg) }, onGuardarCheckOut = { vm.guardarCheckOut(reg) })
                1 -> PasoDiagnostico(ot, reg)
                2 -> PasoEvidencia(reg)
                3 -> PasoCierre(ot, reg, state.errorCierre)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (state.mostrarImprevisto) {
        AlertDialog(
            onDismissRequest = { vm.cerrarImprevisto() },
            title = { Text("Reportar imprevisto") },
            text = {
                Column {
                    Text("La OT quedará como \"Pendiente por imprevisto\" para que el planificador la gestione.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.motivoImprevisto,
                        onValueChange = { vm.onMotivoImprevistoChange(it) },
                        label = { Text("Motivo del imprevisto *") },
                        isError = state.errorImprevisto,
                        supportingText = { if (state.errorImprevisto) Text("El motivo es obligatorio") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.confirmarImprevisto() }) { Text("Reportar") }
            },
            dismissButton = {
                TextButton(onClick = { vm.cerrarImprevisto() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun IndicadorPasos(actual: Int, onSeleccion: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PASOS.forEachIndexed { indice, nombre ->
            val seleccionado = indice == actual
            val fondo = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val texto = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(fondo)
                    .clickable { onSeleccion(indice) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (seleccionado) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${indice + 1}", color = texto, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(6.dp))
                Text(nombre, color = texto, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ---------------------------------------------------------------------
// Paso 1: check-in y check-out
// ---------------------------------------------------------------------
@Composable
private fun PasoCheckIn(
    ot: OrdenTrabajo,
    reg: RegistroEjecucion,
    onGuardarCheckIn: () -> Unit,
    onGuardarCheckOut: () -> Unit
) {
    val context = LocalContext.current
    val permisoUbicacion = Manifest.permission.ACCESS_FINE_LOCATION
    val launcherUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        reg.ubicacionAutorizada = concedido
        if (!concedido) {
            Toast.makeText(context, "Permiso de ubicación denegado. Puedes autorizarlo en Ajustes.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Ubicación autorizada para el check-in.", Toast.LENGTH_SHORT).show()
        }
    }

    fun solicitarOSincronizarUbicacion(activar: Boolean) {
        if (!activar) {
            reg.ubicacionAutorizada = false
            return
        }
        val yaConcedido = ContextCompat.checkSelfPermission(context, permisoUbicacion) == PackageManager.PERMISSION_GRANTED
        if (yaConcedido) {
            reg.ubicacionAutorizada = true
        } else {
            launcherUbicacion.launch(permisoUbicacion)
        }
    }

    Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TituloSeccion("1. Registro de la visita")
            FilaDato("Cliente", "${ot.cliente} · ${ot.local}")
            FilaDato("Equipo", ot.equipo)

            BannerAlerta(
                icono = "📍",
                titulo = "Uso de tu ubicación",
                mensaje = "Al iniciar la visita, la app solicita permiso de ubicación para confirmar que estás en el lugar del servicio. " +
                    "Solo se usa en ese momento, no se rastrea tu recorrido (HU-03/04).",
                tipo = TipoBanner.INFO
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Autorizo el uso de mi ubicación al hacer check-in",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = reg.ubicacionAutorizada,
                    onCheckedChange = { solicitarOSincronizarUbicacion(it) }
                )
            }

            if (reg.checkInTexto.isBlank()) {
                Button(
                    onClick = {
                        val concedido = ContextCompat.checkSelfPermission(context, permisoUbicacion) == PackageManager.PERMISSION_GRANTED
                        if (concedido) {
                            reg.ubicacionAutorizada = true
                            onGuardarCheckIn()
                        } else {
                            launcherUbicacion.launch(permisoUbicacion)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Marcar check-in (llegada)") }
                if (!reg.ubicacionAutorizada) {
                    Text(
                        "Se solicitará el permiso de ubicación al marcar el check-in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                FilaDato("Check-in", reg.checkInTexto)
                FilaDato("Ubicación registrada", "Autorizada en el check-in (permiso en tiempo de ejecución)")
            }

            if (reg.checkInTexto.isNotBlank() && reg.checkOutTexto.isBlank()) {
                OutlinedButton(
                    onClick = onGuardarCheckOut,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Marcar check-out (salida)") }
            }
            if (reg.checkOutTexto.isNotBlank()) {
                FilaDato("Check-out", reg.checkOutTexto)
                FilaDato("Tiempo de atención", minutosATexto(reg.tiempoAtencionMin ?: 0))
            }
        }
    }
}

// ---------------------------------------------------------------------
// Paso 2: diagnóstico
// ---------------------------------------------------------------------
@Composable
private fun PasoDiagnostico(ot: OrdenTrabajo, reg: RegistroEjecucion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TituloSeccion("2. Diagnóstico técnico")
            Text(
                "Describe lo que encontraste en el equipo. Este diagnóstico es obligatorio para cerrar la OT.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = reg.diagnostico,
                onValueChange = { reg.diagnostico = it },
                label = { Text("Diagnóstico *") },
                placeholder = { Text("Ej.: ${ot.descripcion}") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------------
// Paso 3: actividades, materiales y fotos
// ---------------------------------------------------------------------
@Composable
private fun PasoEvidencia(reg: RegistroEjecucion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TituloSeccion("3. Actividades, materiales y fotos")

            ListaEditable(
                titulo = "Actividades realizadas *",
                pista = "Ej.: Reemplazo del termostato",
                elementos = reg.actividades,
                vacio = "Agrega al menos una actividad."
            )
            ListaEditable(
                titulo = "Materiales utilizados *",
                pista = "Ej.: Termostato digital x 1",
                elementos = reg.materiales,
                vacio = "Agrega al menos un material."
            )

            TituloSeccion("Fotos de evidencia")
            Text(
                "En este prototipo se simulan las fotos con una marca. En la app real se usaría la cámara.",
                style = MaterialTheme.typography.bodySmall
            )
            FotosSimuladas("Antes *", reg.fotosAntes)
            FotosSimuladas("Después *", reg.fotosDespues)
        }
    }
}

@Composable
private fun ListaEditable(
    titulo: String,
    pista: String,
    elementos: MutableList<String>,
    vacio: String
) {
    var texto by rememberSaveable { mutableStateOf("") }
    TituloSeccion(titulo)
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text(pista) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(8.dp))
        Button(
            onClick = {
                if (texto.isNotBlank()) {
                    elementos.add(texto.trim())
                    texto = ""
                    DatosDemo.registrarCambioLocal()
                }
            }
        ) { Text("Agregar") }
    }
    if (elementos.isEmpty()) {
        Text(vacio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    } else {
        elementos.forEachIndexed { indice, item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("• $item", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { elementos.removeAt(indice) }) { Text("Quitar") }
            }
        }
    }
}

@Composable
private fun FotosSimuladas(titulo: String, fotos: MutableList<String>) {
    val context = LocalContext.current
    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotos.add("foto_camara_${fotos.size + 1}")
            DatosDemo.registrarCambioLocal()
        }
    }
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            launcherCamara.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado. Puedes agregar evidencia simulada.", Toast.LENGTH_LONG).show()
        }
    }

    fun tomarFoto() {
        val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (concedido) launcherCamara.launch(null) else launcherPermisoCamara.launch(Manifest.permission.CAMERA)
    }

    TituloSeccion(titulo)
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        fotos.forEach { _ ->
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", textAlign = TextAlign.Center)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = { tomarFoto() }) { Text("Tomar foto") }
            OutlinedButton(onClick = {
                fotos.add("foto_${fotos.size + 1}")
                DatosDemo.registrarCambioLocal()
            }) { Text("+ Simulada") }
        }
    }
    Text(
        "Usa la cámara (permiso en tiempo de ejecución) o agrega una evidencia simulada.",
        style = MaterialTheme.typography.bodySmall
    )
    if (fotos.isEmpty()) {
        Text(
            "Agrega al menos una foto.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// ---------------------------------------------------------------------
// Paso 4: cierre
// ---------------------------------------------------------------------
@Composable
private fun PasoCierre(ot: OrdenTrabajo, reg: RegistroEjecucion, errorCierre: String?) {
    val incompleto = !reg.ubicacionAutorizada ||
        reg.checkInTexto.isBlank() || reg.checkOutTexto.isBlank() ||
        reg.diagnostico.isBlank() || reg.actividades.isEmpty() ||
        reg.materiales.isEmpty() || reg.fotosAntes.isEmpty() || reg.fotosDespues.isEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (incompleto) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TituloSeccion("4. Cierre de la OT")
            FilaDato("Check-in", if (reg.checkInTexto.isBlank()) "Falta" else reg.checkInTexto)
            FilaDato("Check-out", if (reg.checkOutTexto.isBlank()) "Falta" else reg.checkOutTexto)
            FilaDato("Tiempo de atención", reg.tiempoAtencionMin?.let { minutosATexto(it) } ?: "Falta check-out")
            FilaDato("Diagnóstico", if (reg.diagnostico.isBlank()) "Falta" else "Completo")
            FilaDato("Actividades", if (reg.actividades.isEmpty()) "Falta" else "${reg.actividades.size} registradas")
            FilaDato("Materiales", if (reg.materiales.isEmpty()) "Falta" else "${reg.materiales.size} registrados")
            FilaDato("Fotos antes / después", "${reg.fotosAntes.size} / ${reg.fotosDespues.size}")

            if (incompleto) {
                BannerAlerta(
                    icono = "⚠️",
                    titulo = "Faltan datos obligatorios",
                    mensaje = "Completa los pasos anteriores antes de cerrar la OT.",
                    tipo = TipoBanner.AVISO
                )
            } else {
                BannerAlerta(
                    icono = "✅",
                    titulo = "Listo para cerrar",
                    mensaje = "Al cerrar, el cliente podrá revisar el trabajo y dejar su conformidad.",
                    tipo = TipoBanner.OK
                )
            }
            if (errorCierre != null) {
                Text(errorCierre, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
            Text("SLA pactado: ${ot.slaHoras} horas", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BarraAccionesEjecucion(onImprevisto: () -> Unit, onCerrar: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onImprevisto, modifier = Modifier.weight(1f)) {
                Text("Imprevisto")
            }
            Button(onClick = onCerrar, modifier = Modifier.weight(1f)) {
                Text("Cerrar OT")
            }
        }
    }
}
