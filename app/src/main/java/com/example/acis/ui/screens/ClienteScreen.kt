package com.example.acis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.acis.data.Conformidad
import com.example.acis.data.Equipo
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import com.example.acis.data.minutosATexto
import com.example.acis.ui.componentes.BannerAlerta
import com.example.acis.ui.componentes.BarraSuperior
import com.example.acis.ui.componentes.BotonCerrarSesion
import com.example.acis.ui.componentes.CampoDesplegable
import com.example.acis.ui.componentes.EtiquetaEstado
import com.example.acis.ui.componentes.FilaDato
import com.example.acis.ui.componentes.TipoBanner
import com.example.acis.ui.componentes.TituloSeccion
import com.example.acis.viewmodel.ClienteViewModel

/**
 * Pantalla 8 - Mis mantenciones (cliente).
 * HU-40 solicitar mantención, HU-41 consultar el estado de mis OT (solo las de mi local),
 * HU-42 aprobar el trabajo, HU-43 rechazarlo con comentario obligatorio,
 * HU-45 aviso de cierre con acceso directo al detalle.
 *
 * MVVM: datos y acciones en [ClienteViewModel]; formularios con validación local + submit al VM.
 */
@Composable
fun ClienteScreen(
    navController: NavController,
    vm: ClienteViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.cargar() }

    LaunchedEffect(state.mensaje) {
        val msg = state.mensaje ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.consumirMensaje()
    }

    val porRevisar = state.porRevisar

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mis mantenciones",
                acciones = { BotonCerrarSesion(navController) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { vm.abrirSolicitud() }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Solicitar mantención")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        state.clienteNombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(state.localNombre, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                AnimatedVisibility(visible = porRevisar != null) {
                    if (porRevisar != null) {
                        BannerAlerta(
                            icono = "🔔",
                            titulo = "Tu OT ${porRevisar.id} fue cerrada",
                            mensaje = "Revisa el trabajo realizado y deja tu conformidad.",
                            tipo = TipoBanner.INFO,
                            acciones = {
                                TextButton(onClick = { vm.expandir(porRevisar.id) }) {
                                    Text("Ver detalle")
                                }
                            }
                        )
                    }
                }
            }
            items(state.misOt, key = { it.id }) { ot ->
                TarjetaOtCliente(
                    ot = ot,
                    expandida = state.expandidaId == ot.id,
                    onAlternar = { vm.alternarExpandida(ot.id) },
                    onAprobar = { vm.aprobar(ot.id) },
                    onRechazar = { vm.pedirObservar(ot.id) }
                )
            }
        }
    }

    if (state.mostrarSolicitud) {
        DialogoSolicitud(
            equipos = state.equiposDisponibles,
            onCancelar = { vm.cerrarSolicitud() },
            onEnviar = { equipo, descripcion -> vm.enviarSolicitud(equipo, descripcion) }
        )
    }

    val idObservar = state.otAObservar
    if (idObservar != null) {
        DialogoRechazo(
            onCancelar = { vm.cancelarObservar() },
            onEnviar = { comentario -> vm.enviarObservacion(idObservar, comentario) }
        )
    }
}

private fun avance(estado: EstadoOt): Float = when (estado) {
    EstadoOt.CREADA, EstadoOt.PENDIENTE_ASIGNACION -> 0.15f
    EstadoOt.ASIGNADA -> 0.35f
    EstadoOt.ACEPTADA -> 0.5f
    EstadoOt.PENDIENTE_IMPREVISTO -> 0.6f
    EstadoOt.EN_EJECUCION -> 0.75f
    EstadoOt.CERRADA -> 1f
}

@Composable
private fun TarjetaOtCliente(
    ot: OrdenTrabajo,
    expandida: Boolean,
    onAlternar: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(onClick = onAlternar, modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ot.id, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                EtiquetaEstado(ot.estado)
            }
            Text("${ot.tipo} · ${ot.equipo}", fontWeight = FontWeight.Medium)
            Text(
                "Fecha: ${ot.fecha}  ·  Técnico: ${ot.tecnico ?: "por asignar"}",
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = { avance(ot.estado) },
                modifier = Modifier.fillMaxWidth()
            )

            if (expandida) {
                Spacer(Modifier.height(4.dp))
                TituloSeccion("Detalle")
                FilaDato("Problema", ot.descripcion)
                FilaDato("Ubicación", ot.ubicacionEquipo)

                if (ot.estado == EstadoOt.CERRADA) {
                    FilaDato("Tiempo de atención", minutosATexto(ot.tiempoRealMin ?: 0))
                    FilaDato("Diagnóstico", ot.diagnostico ?: "-")
                    if (ot.actividades.isNotEmpty()) {
                        Text("Actividades realizadas", fontWeight = FontWeight.Bold)
                        ot.actividades.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                    if (ot.materiales.isNotEmpty()) {
                        Text("Materiales utilizados", fontWeight = FontWeight.Bold)
                        ot.materiales.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                    }

                    when (ot.conformidad) {
                        Conformidad.SIN_REVISAR -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f)) {
                                    Text("Rechazar")
                                }
                                Button(onClick = onAprobar, modifier = Modifier.weight(1f)) {
                                    Text("Aprobar trabajo")
                                }
                            }
                        }
                        Conformidad.APROBADA -> {
                            BannerAlerta(
                                icono = "✅",
                                titulo = "Trabajo aprobado",
                                mensaje = "Conformidad registrada el ${ot.fechaConformidad ?: "-"}.",
                                tipo = TipoBanner.OK
                            )
                        }
                        Conformidad.OBSERVADA -> {
                            BannerAlerta(
                                icono = "💬",
                                titulo = "Trabajo observado",
                                mensaje = "Tu comentario: ${ot.comentarioCliente ?: "-"}",
                                tipo = TipoBanner.AVISO
                            )
                        }
                    }
                }
            } else {
                Text("Toca para ver el detalle", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DialogoSolicitud(
    equipos: List<Equipo>,
    onCancelar: () -> Unit,
    onEnviar: (Equipo, String) -> Unit
) {
    var equipoSel by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }

    val errorEquipo = equipoSel.isBlank()
    val errorDescripcion = descripcion.isBlank()

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Solicitar mantención") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cuéntanos qué falla tiene tu equipo. La solicitud llegará al planificador.")
                CampoDesplegable(
                    etiqueta = "Equipo *",
                    opciones = equipos.map { it.nombre },
                    seleccion = equipoSel,
                    onSeleccion = { equipoSel = it },
                    error = intento && errorEquipo
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción de la falla *") },
                    minLines = 3,
                    isError = intento && errorDescripcion,
                    supportingText = { if (intento && errorDescripcion) Text("La descripción es obligatoria") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                intento = true
                val equipo = equipos.firstOrNull { it.nombre == equipoSel }
                if (equipo != null && !errorDescripcion) {
                    onEnviar(equipo, descripcion.trim())
                }
            }) { Text("Enviar solicitud") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoRechazo(onCancelar: () -> Unit, onEnviar: (String) -> Unit) {
    var comentario by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Rechazar el trabajo") },
        text = {
            Column {
                Text("Cuéntanos qué quedó pendiente. El planificador verá tu comentario en la OT.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = {
                        comentario = it
                        error = false
                    },
                    label = { Text("Comentario *") },
                    isError = error,
                    supportingText = { if (error) Text("El comentario es obligatorio") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (comentario.isBlank()) error = true else onEnviar(comentario.trim())
            }) { Text("Enviar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
