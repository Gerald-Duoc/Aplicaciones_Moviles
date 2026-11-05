package com.example.acis.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.acis.data.DatosDemo
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import com.example.acis.data.minutosATexto
import com.example.acis.ui.componentes.BarraPlanificador
import com.example.acis.ui.componentes.BarraSuperior
import com.example.acis.ui.componentes.BotonCerrarSesion
import com.example.acis.ui.componentes.EtiquetaEstado
import com.example.acis.ui.componentes.EtiquetaPrioridad
import com.example.acis.ui.componentes.FiltroDesplegable
import com.example.acis.ui.componentes.TarjetaResumen
import com.example.acis.viewmodel.PanelOtViewModel

/**
 * Pantalla 5 - Panel de operación (planificador/supervisor).
 * HU-11 ver el estado de todas las OT, HU-12 filtrar por fecha, estado y técnico,
 * HU-08 asignar, HU-09 reasignar, HU-10 reprogramar y HU-37 monitorear tiempos (SLA).
 *
 * MVVM: filtros, datos derivados y acciones en [PanelOtViewModel].
 */
@Composable
fun PanelOtScreen(
    navController: NavController,
    vm: PanelOtViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.cargar() }

    LaunchedEffect(state.mensaje) {
        val msg = state.mensaje ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.consumirMensaje()
    }

    val filtradas = state.filtradas

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Panel de operación",
                acciones = { BotonCerrarSesion(navController) }
            )
        },
        bottomBar = { BarraPlanificador(navController, 0) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { navController.navigate("gestionar/nueva/0") }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nueva OT")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaResumen("Asignadas", state.asignadas.toString(), Modifier.weight(1f))
                TarjetaResumen("En ejecución", state.enEjecucion.toString(), Modifier.weight(1f))
                TarjetaResumen("Cerradas", state.cerradas.toString(), Modifier.weight(1f))
                TarjetaResumen("Pendientes", state.pendientes.toString(), Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.verPorTecnico,
                    onClick = { vm.setVerPorTecnico(false) },
                    label = { Text("Todas las OT") }
                )
                FilterChip(
                    selected = state.verPorTecnico,
                    onClick = { vm.setVerPorTecnico(true) },
                    label = { Text("Por técnico") }
                )
            }

            if (state.verPorTecnico) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DatosDemo.tecnicos, key = { it.nombre }) { tecnico ->
                        val otTecnico = filtradas.filter { it.tecnico == tecnico.nombre }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "${tecnico.nombre}  ·  ${otTecnico.size} OT",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                if (otTecnico.isEmpty()) {
                                    Text("Sin OT con estos filtros.", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    otTecnico.forEach { ot ->
                                        Text(
                                            "${ot.id} · ${ot.local} · ${ot.estado.etiqueta}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroDesplegable(
                        "Estado",
                        listOf("Todos") + EstadoOt.entries.map { it.etiqueta },
                        state.filtroEstado
                    ) { vm.setFiltroEstado(it) }
                    FiltroDesplegable(
                        "Técnico",
                        listOf("Todos") + DatosDemo.tecnicos.map { it.nombre },
                        state.filtroTecnico
                    ) { vm.setFiltroTecnico(it) }
                    FiltroDesplegable(
                        "Fecha",
                        listOf("Todos") + DatosDemo.ordenes.map { it.fecha }.distinct(),
                        state.filtroFecha
                    ) { vm.setFiltroFecha(it) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filtradas.isEmpty()) {
                        item { Text("No hay OT con los filtros seleccionados.") }
                    }
                    items(filtradas, key = { it.id }) { ot ->
                        TarjetaOtPanel(
                            ot = ot,
                            onAsignar = { navController.navigate("gestionar/asignar/${ot.id}") },
                            onReasignar = { navController.navigate("gestionar/reasignar/${ot.id}") },
                            onReprogramar = { vm.pedirReprogramar(ot.id) }
                        )
                    }
                }
            }
        }
    }

    val idReprogramar = state.otAReprogramar
    if (idReprogramar != null) {
        val ot = DatosDemo.ordenPorId(idReprogramar)
        AlertDialog(
            onDismissRequest = { vm.cancelarReprogramar() },
            title = { Text("Reprogramar $idReprogramar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Fecha actual: ${ot?.fecha ?: "-"}")
                    Text("Elige la nueva fecha. El técnico asignado será notificado.")
                    Spacer(Modifier.height(4.dp))
                    DatosDemo.dias.forEach { dia ->
                        OutlinedButton(
                            onClick = { vm.reprogramar(idReprogramar, dia) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(dia.etiqueta, fontWeight = FontWeight.Medium)
                                Text(
                                    "${dia.clima.icono} ${dia.clima.etiqueta}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { vm.cancelarReprogramar() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaOtPanel(
    ot: OrdenTrabajo,
    onAsignar: () -> Unit,
    onReasignar: () -> Unit,
    onReprogramar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ot.id, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                EtiquetaEstado(ot.estado)
            }
            Text("${ot.cliente} · ${ot.local}", fontWeight = FontWeight.Medium)
            Text("Equipo: ${ot.equipo}", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaPrioridad(ot.prioridad)
            }
            Text(
                "Técnico: ${ot.tecnico ?: "Sin asignar"}  ·  Fecha: ${ot.fecha}",
                style = MaterialTheme.typography.bodySmall
            )
            Text("SLA pactado: ${ot.slaHoras} h", style = MaterialTheme.typography.bodySmall)

            if (ot.estado == EstadoOt.CERRADA) {
                val real = ot.tiempoRealMin ?: 0
                val cumple = DatosDemo.cumpleSla(ot) == true
                Text(
                    "Tiempo real: ${minutosATexto(real)}  ·  ${if (cumple) "Cumplió SLA" else "Excedió SLA"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (cumple) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            if (ot.observacion != null) {
                Text(
                    ot.observacion ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (ot.estado != EstadoOt.CERRADA) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ot.tecnico == null) {
                        Button(onClick = onAsignar) { Text("Asignar") }
                    } else {
                        OutlinedButton(onClick = onReasignar) { Text("Reasignar") }
                        OutlinedButton(onClick = onReprogramar) { Text("Reprogramar") }
                    }
                }
            }
        }
    }
}
