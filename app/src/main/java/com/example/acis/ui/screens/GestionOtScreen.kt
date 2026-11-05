package com.example.acis.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.acis.data.Clima
import com.example.acis.data.DatosDemo
import com.example.acis.data.Prioridad
import com.example.acis.ui.componentes.BannerAlerta
import com.example.acis.ui.componentes.BarraSuperior
import com.example.acis.ui.componentes.CampoDesplegable
import com.example.acis.ui.componentes.FilaDato
import com.example.acis.ui.componentes.TipoBanner
import com.example.acis.ui.componentes.TituloSeccion
import com.example.acis.viewmodel.GestionOtViewModel
import com.example.acis.viewmodel.textoDia

/**
 * Pantalla 6 - Crear y asignar OT (planificador).
 * Un mismo formulario sirve para 4 modos:
 *  - "nueva": HU-06 crear OT, HU-07 prioridad, HU-08 asignar, HU-31 alerta climática al agendar
 *  - "asignar": HU-08 asignar una OT que aún no tiene técnico
 *  - "reasignar": HU-09 cambiar el técnico de una OT
 *  - "reprogramar": HU-10 cambiar la fecha (con alerta climática, HU-31)
 *
 * MVVM: estado del formulario y guardado en [GestionOtViewModel].
 */
@Composable
fun GestionOtScreen(
    navController: NavController,
    modo: String,
    otId: String,
    vm: GestionOtViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(modo, otId) { vm.inicializar(modo, otId) }

    val ot = state.ot
    val cliente = DatosDemo.clientes.firstOrNull { it.nombre == state.clienteSel }
    val local = cliente?.locales?.firstOrNull { it.nombre == state.localSel }
    val equipo = local?.equipos?.firstOrNull { it.nombre == state.equipoSel }
    val dia = DatosDemo.dias.firstOrNull { textoDia(it) == state.diaSel }

    val equipoExterior = if (state.esNueva) equipo?.exterior == true else ot?.exterior == true
    val hayAlertaClima = dia != null && dia.clima != Clima.DESPEJADO && equipoExterior

    val errorCliente = state.esNueva && cliente == null
    val errorLocal = state.esNueva && local == null
    val errorEquipo = state.esNueva && equipo == null
    val errorDescripcion = state.esNueva && state.descripcion.isBlank()
    val errorFecha = state.mostrarFecha && dia == null
    val errorTecnico = !state.esNueva && state.mostrarTecnico && state.tecnicoSel.isBlank()
    val hayErrores = errorCliente || errorLocal || errorEquipo || errorDescripcion || errorFecha || errorTecnico

    Scaffold(
        topBar = { BarraSuperior(titulo = state.titulo, onVolver = { navController.popBackStack() }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (ot != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TituloSeccion(ot.id)
                        Spacer(Modifier.height(6.dp))
                        FilaDato("Cliente", "${ot.cliente} · ${ot.local}")
                        FilaDato("Equipo", ot.equipo)
                        FilaDato("Prioridad", ot.prioridad.etiqueta)
                        FilaDato("Técnico actual", ot.tecnico ?: "Sin asignar")
                        FilaDato("Fecha actual", ot.fecha)
                    }
                }
            }

            if (state.esNueva) {
                TituloSeccion("¿Dónde y qué equipo?")
                CampoDesplegable(
                    etiqueta = "Cliente *",
                    opciones = DatosDemo.clientes.map { it.nombre },
                    seleccion = state.clienteSel,
                    onSeleccion = { vm.onCliente(it) },
                    error = state.intentoGuardar && errorCliente
                )
                CampoDesplegable(
                    etiqueta = "Local *",
                    opciones = cliente?.locales?.map { it.nombre } ?: emptyList(),
                    seleccion = state.localSel,
                    onSeleccion = { vm.onLocal(it) },
                    habilitado = cliente != null,
                    error = state.intentoGuardar && errorLocal
                )
                CampoDesplegable(
                    etiqueta = "Equipo *",
                    opciones = local?.equipos?.map { it.nombre } ?: emptyList(),
                    seleccion = state.equipoSel,
                    onSeleccion = { vm.onEquipo(it) },
                    habilitado = local != null,
                    error = state.intentoGuardar && errorEquipo
                )

                TituloSeccion("Tipo y prioridad")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Preventiva", "Correctiva").forEach { tipo ->
                        FilterChip(
                            selected = state.tipoSel == tipo,
                            onClick = { vm.onTipo(tipo) },
                            label = { Text(tipo) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Prioridad.entries.forEach { prioridad ->
                        FilterChip(
                            selected = state.prioridadSel == prioridad.etiqueta,
                            onClick = { vm.onPrioridad(prioridad.etiqueta) },
                            label = { Text("Prioridad ${prioridad.etiqueta}") }
                        )
                    }
                }
                OutlinedTextField(
                    value = state.descripcion,
                    onValueChange = { vm.onDescripcion(it) },
                    label = { Text("Descripción del problema *") },
                    minLines = 3,
                    isError = state.intentoGuardar && errorDescripcion,
                    supportingText = { if (state.intentoGuardar && errorDescripcion) Text("La descripción es obligatoria") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.mostrarFecha) {
                TituloSeccion("Fecha de la visita")
                CampoDesplegable(
                    etiqueta = "Fecha y pronóstico *",
                    opciones = DatosDemo.dias.map { textoDia(it) },
                    seleccion = state.diaSel,
                    onSeleccion = { vm.onDia(it) },
                    error = state.intentoGuardar && errorFecha
                )
                if (hayAlertaClima && dia != null) {
                    BannerAlerta(
                        icono = dia.clima.icono,
                        titulo = "Alerta climática",
                        mensaje = "Se pronostica ${dia.clima.etiqueta.lowercase()} para el ${dia.etiqueta} y el " +
                            "equipo está en el exterior. Considera elegir otra fecha para postergar la mantención.",
                        tipo = TipoBanner.PELIGRO
                    )
                }
            }

            if (state.mostrarTecnico) {
                TituloSeccion(if (state.esNueva) "Asignación (opcional)" else "Técnico")
                CampoDesplegable(
                    etiqueta = if (state.esNueva) "Técnico disponible" else "Técnico *",
                    opciones = vm.opcionesTecnico(),
                    seleccion = state.tecnicoSel,
                    onSeleccion = { vm.onTecnico(it) },
                    error = state.intentoGuardar && errorTecnico
                )
                if (state.modo == "reasignar" && ot?.tecnico != null) {
                    Text(
                        "${ot.tecnico} dejará de ver esta OT y el nuevo técnico la recibirá en su aplicación.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (state.intentoGuardar && hayErrores) {
                Text(
                    "Revisa los campos marcados en rojo.",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(onClick = { vm.guardar() }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    when (state.modo) {
                        "nueva" -> "Crear OT"
                        "asignar" -> "Asignar OT"
                        "reasignar" -> "Reasignar OT"
                        else -> "Guardar nueva fecha"
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (state.confirmacion != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Listo") },
            text = { Text(state.confirmacion ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    vm.consumirConfirmacion()
                    navController.popBackStack()
                }) { Text("Volver al panel") }
            }
        )
    }
}
