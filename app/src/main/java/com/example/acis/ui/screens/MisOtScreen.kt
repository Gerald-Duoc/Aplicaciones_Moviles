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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import com.example.acis.ui.componentes.AvisoConexion
import com.example.acis.ui.componentes.BannerAlerta
import com.example.acis.ui.componentes.BarraSuperior
import com.example.acis.ui.componentes.BotonCerrarSesion
import com.example.acis.ui.componentes.EtiquetaClima
import com.example.acis.ui.componentes.EtiquetaEstado
import com.example.acis.ui.componentes.EtiquetaPrioridad
import com.example.acis.ui.componentes.SelectorSinConexion
import com.example.acis.ui.componentes.TipoBanner
import com.example.acis.viewmodel.MisOtViewModel

/**
 * Pantalla 2 - Mis OT del día (técnico de terreno).
 * HU-13 listado del día, HU-26/27/28 modo sin conexión y sincronización,
 * HU-44 aviso de nueva OT, HU-29/30 etiqueta de alerta climática.
 *
 * MVVM: la pantalla (View) observa [MisOtViewModel.uiState]; el estado/estructura
 * y la carga de datos los guarda el ViewModel.
 */
@Composable
fun MisOtScreen(
    navController: NavController,
    vm: MisOtViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    // Refresca al entrar/volver a la pantalla para reflejar cambios hechos en otras vistas.
    LaunchedEffect(Unit) { vm.cargar() }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mis OT de hoy",
                acciones = { BotonCerrarSesion(navController) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        "Hola, ${state.tecnicoNombre}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tienes ${state.pendientes} OT por atender hoy.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        state.fuente,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item { SelectorSinConexion() }
            item { AvisoConexion() }
            val nuevaOt = state.nuevaOt
            item {
                AnimatedVisibility(visible = state.avisoNuevaOtVisible && nuevaOt != null) {
                    if (nuevaOt != null) {
                        BannerAlerta(
                            icono = "🔔",
                            titulo = "Nueva OT asignada",
                            mensaje = "${nuevaOt.id} · ${nuevaOt.cliente} · Prioridad ${nuevaOt.prioridad.etiqueta}",
                            tipo = TipoBanner.INFO,
                            acciones = {
                                Row {
                                    TextButton(onClick = { navController.navigate("detalle/${nuevaOt.id}") }) {
                                        Text("Ver OT")
                                    }
                                    TextButton(onClick = { vm.descartarAvisoNuevaOt() }) {
                                        Text("Descartar")
                                    }
                                }
                            }
                        )
                    }
                }
            }
            items(state.misOt, key = { it.id }) { ot ->
                TarjetaOtTecnico(ot) { navController.navigate("detalle/${ot.id}") }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun TarjetaOtTecnico(ot: OrdenTrabajo, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ot.id, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                EtiquetaPrioridad(ot.prioridad)
            }
            Text("${ot.cliente} · ${ot.local}", fontWeight = FontWeight.Medium)
            Text(ot.direccion, style = MaterialTheme.typography.bodySmall)
            Text("${ot.tipo} · ${ot.equipo}", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaEstado(ot.estado)
                if (ot.exterior && ot.clima != Clima.DESPEJADO && ot.estado != EstadoOt.CERRADA) {
                    EtiquetaClima(ot.clima)
                }
            }
        }
    }
}
