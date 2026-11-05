package com.example.acis.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.acis.data.Clima
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import com.example.acis.ui.componentes.BannerAlerta
import com.example.acis.ui.componentes.BarraSuperior
import com.example.acis.ui.componentes.EtiquetaClima
import com.example.acis.ui.componentes.EtiquetaEstado
import com.example.acis.ui.componentes.EtiquetaPrioridad
import com.example.acis.ui.componentes.FilaDato
import com.example.acis.ui.componentes.TipoBanner
import com.example.acis.ui.componentes.TituloSeccion
import com.example.acis.viewmodel.DetalleOtViewModel

/**
 * Pantalla 3 - Detalle de la OT.
 * HU-14 ver información completa, HU-15 aceptar, HU-16 rechazar con motivo obligatorio,
 * HU-29 etiqueta de alerta climática y HU-30 recomendación de postergación.
 *
 * MVVM: observa [DetalleOtViewModel.uiState]; acciones y validaciones en el ViewModel.
 */
@Composable
fun DetalleOtScreen(
    navController: NavController,
    otId: String,
    vm: DetalleOtViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(otId) { vm.cargar(otId) }

    LaunchedEffect(state.mensajeOk) {
        val msg = state.mensajeOk ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.consumirMensaje()
    }
    LaunchedEffect(state.navegarAtras) {
        if (state.navegarAtras) {
            vm.consumirNavegacion()
            navController.popBackStack()
        }
    }

    val ot = state.ot
    Scaffold(
        topBar = { BarraSuperior(titulo = otId, onVolver = { navController.popBackStack() }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (ot != null) {
                AccionesTecnico(
                    ot = ot,
                    onAceptar = { vm.aceptar() },
                    onRechazar = { vm.abrirRechazo() },
                    onEjecutar = { navController.navigate("ejecucion/${ot.id}") }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaEstado(ot.estado)
                EtiquetaPrioridad(ot.prioridad)
                EtiquetaClima(ot.clima)
            }

            if (ot.estado == EstadoOt.ASIGNADA) {
                BannerAlerta(
                    icono = "📥",
                    titulo = "OT asignada a ti",
                    mensaje = "Revisa la información y decide si la aceptas o la rechazas con un motivo.",
                    tipo = TipoBanner.INFO
                )
            }
            if (ot.clima != Clima.DESPEJADO && ot.exterior) {
                BannerAlerta(
                    icono = ot.clima.icono,
                    titulo = "Recomendación por clima",
                    mensaje = "Hay ${ot.clima.etiqueta.lowercase()} y el equipo está en el exterior " +
                        "(${ot.ubicacionEquipo}). Si el riesgo es alto, rechaza la OT indicando el motivo.",
                    tipo = TipoBanner.PELIGRO
                )
            }

            TarjetaInfo("Cliente y local") {
                FilaDato("Cliente", ot.cliente)
                FilaDato("Local", ot.local)
                FilaDato("Dirección", ot.direccion)
            }
            TarjetaInfo("Equipo") {
                FilaDato("Equipo", ot.equipo)
                FilaDato("Ubicación", ot.ubicacionEquipo)
                FilaDato("Tipo", if (ot.exterior) "Exterior (sensible al clima)" else "Interior")
                FilaDato("Última mantención", ot.ultimaMantencion)
            }
            TarjetaInfo("Solicitud") {
                FilaDato("Tipo", ot.tipo)
                FilaDato("Fecha programada", ot.fecha)
                FilaDato("SLA", "${ot.slaHoras} horas")
                FilaDato("Descripción", ot.descripcion)
            }
            if (ot.historial.isNotEmpty()) {
                TarjetaInfo("Historial de intervenciones") {
                    ot.historial.forEach { h ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("${h.fecha} · ${h.tipo}", fontWeight = FontWeight.Bold)
                            Text(h.diagnostico, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (state.mostrarRechazo) {
        AlertDialog(
            onDismissRequest = { vm.cerrarRechazo() },
            title = { Text("Rechazar OT") },
            text = {
                Column {
                    Text("Indica el motivo. La OT quedará pendiente para que el planificador la reasigne.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.motivoRechazo,
                        onValueChange = { vm.onMotivoChange(it) },
                        label = { Text("Motivo del rechazo *") },
                        isError = state.errorMotivo,
                        supportingText = { if (state.errorMotivo) Text("El motivo es obligatorio") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.confirmarRechazo() }) { Text("Confirmar rechazo") }
            },
            dismissButton = {
                TextButton(onClick = { vm.cerrarRechazo() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AccionesTecnico(
    ot: OrdenTrabajo,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onEjecutar: () -> Unit
) {
    val (textoBoton, accion) = when (ot.estado) {
        EstadoOt.ASIGNADA -> "Aceptar OT" to onAceptar
        EstadoOt.ACEPTADA -> "Iniciar visita" to onEjecutar
        EstadoOt.EN_EJECUCION, EstadoOt.PENDIENTE_IMPREVISTO -> "Continuar visita" to onEjecutar
        else -> null to null
    }
    if (accion == null) return

    Surface(shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = accion, modifier = Modifier.fillMaxWidth()) {
                Text(textoBoton ?: "")
            }
            if (ot.estado == EstadoOt.ASIGNADA) {
                OutlinedButton(onClick = onRechazar, modifier = Modifier.fillMaxWidth()) {
                    Text("Rechazar OT")
                }
            }
        }
    }
}

@Composable
private fun TarjetaInfo(titulo: String, contenido: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            TituloSeccion(titulo)
            Spacer(Modifier.height(8.dp))
            contenido()
        }
    }
}
