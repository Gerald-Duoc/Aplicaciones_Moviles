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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.acis.data.Cliente
import com.example.acis.data.DatosDemo
import com.example.acis.data.Equipo
import com.example.acis.data.Local
import com.example.acis.data.Tecnico
import com.example.acis.ui.componentes.BarraPlanificador
import com.example.acis.ui.componentes.BarraSuperior
import com.example.acis.ui.componentes.BotonCerrarSesion
import com.example.acis.ui.componentes.CampoDesplegable
import com.example.acis.ui.componentes.Etiqueta
import com.example.acis.viewmodel.CatastroViewModel

private val SECCIONES = listOf("Clientes", "Locales", "Equipos", "Técnicos")
private val TIPOS_EQUIPO = listOf("Condensadora", "Evaporadora", "Chiller", "Rooftop", "Split", "Cámara frigorífica", "Vitrina")

/**
 * Pantalla 7 - Catastro y usuarios (planificador).
 * HU-32 registrar clientes, HU-36 definir el SLA de cada cliente (en horas),
 * HU-33 registrar locales, HU-34 registrar equipos y HU-02 registrar técnicos.
 *
 * MVVM: datos y operaciones en [CatastroViewModel]; formularios con estado/validación local
 * y guardado delegado al ViewModel.
 */
@Composable
fun CatastroScreen(
    navController: NavController,
    vm: CatastroViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.cargar() }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Catastro y usuarios",
                acciones = { BotonCerrarSesion(navController) }
            )
        },
        bottomBar = { BarraPlanificador(navController, 1) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { vm.abrirFormulario() }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when (state.seccion) {
                        0 -> "Nuevo cliente"
                        1 -> "Nuevo local"
                        2 -> "Nuevo equipo"
                        else -> "Nuevo técnico"
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SECCIONES.forEachIndexed { indice, nombre ->
                    FilterChip(
                        selected = state.seccion == indice,
                        onClick = { vm.setSeccion(indice) },
                        label = { Text(nombre) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state.seccion) {
                    0 -> items(state.clientes, key = { it.nombre }) { cliente -> TarjetaCliente(cliente) }
                    1 -> items(state.locales, key = { "${it.first}-${it.second.nombre}" }) { (nombreCliente, local) ->
                        TarjetaLocal(nombreCliente, local)
                    }
                    2 -> items(state.equipos, key = { "${it.first}-${it.second}-${it.third.nombre}" }) { (nombreCliente, nombreLocal, equipo) ->
                        TarjetaEquipo(nombreCliente, nombreLocal, equipo)
                    }
                    else -> items(state.tecnicos, key = { it.correo }) { tecnico ->
                        TarjetaTecnico(tecnico) { vm.cargaTecnico(tecnico.nombre) }
                    }
                }
            }
        }
    }

    if (state.mostrarFormulario) {
        val cerrar = { vm.cerrarFormulario() }
        when (state.seccion) {
            0 -> DialogoCliente(
                onCerrar = cerrar,
                onGuardar = { vm.agregarCliente(it); cerrar() }
            )
            1 -> DialogoLocal(
                clientes = state.clientes,
                onCerrar = cerrar,
                onGuardar = { nc, l -> vm.agregarLocal(nc, l); cerrar() }
            )
            2 -> DialogoEquipo(
                clientes = state.clientes,
                onCerrar = cerrar,
                onGuardar = { nc, nl, e -> vm.agregarEquipo(nc, nl, e); cerrar() }
            )
            else -> DialogoTecnico(
                existeCorreo = { vm.existeCorreoTecnico(it) },
                onCerrar = cerrar,
                onGuardar = { vm.agregarTecnico(it); cerrar() }
            )
        }
    }
}

@Composable
private fun TarjetaCliente(cliente: Cliente) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(cliente.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Contacto: ${cliente.contacto}", style = MaterialTheme.typography.bodyMedium)
            Text("SLA pactado: ${cliente.slaHoras} horas", style = MaterialTheme.typography.bodyMedium)
            Text("${cliente.locales.size} locales", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TarjetaLocal(nombreCliente: String, local: Local) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(local.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Cliente: $nombreCliente", style = MaterialTheme.typography.bodyMedium)
            Text(local.direccion, style = MaterialTheme.typography.bodyMedium)
            Text("${local.equipos.size} equipos", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TarjetaEquipo(nombreCliente: String, nombreLocal: String, equipo: Equipo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(equipo.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("${equipo.tipo} · ${equipo.marca} ${equipo.modelo}", style = MaterialTheme.typography.bodyMedium)
            Text("$nombreCliente · $nombreLocal", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (equipo.exterior) {
                    Etiqueta("Exterior (sensible al clima)", Color(0xFFFFF4D6), Color(0xFF5C4300))
                } else {
                    Etiqueta("Interior", Color(0xFFE3E8EE), Color(0xFF37474F))
                }
            }
        }
    }
}

@Composable
private fun TarjetaTecnico(tecnico: Tecnico, carga: () -> Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(tecnico.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(tecnico.correo, style = MaterialTheme.typography.bodyMedium)
            Text("Perfil: Técnico de terreno", style = MaterialTheme.typography.bodySmall)
            Text("OT activas: ${carga()}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DialogoCliente(onCerrar: () -> Unit, onGuardar: (Cliente) -> Unit) {
    var razonSocial by rememberSaveable { mutableStateOf("") }
    var contacto by rememberSaveable { mutableStateOf("") }
    var sla by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }

    val horas = sla.trim().toIntOrNull()
    val errorRazon = razonSocial.isBlank()
    val errorContacto = contacto.isBlank()
    val errorSla = horas == null || horas <= 0

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo cliente") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = razonSocial,
                    onValueChange = { razonSocial = it },
                    label = { Text("Razón social *") },
                    isError = intento && errorRazon,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contacto,
                    onValueChange = { contacto = it },
                    label = { Text("Contacto (nombre y correo) *") },
                    isError = intento && errorContacto,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sla,
                    onValueChange = { sla = it },
                    label = { Text("SLA pactado (horas) *") },
                    isError = intento && errorSla,
                    supportingText = { Text("Plazo de referencia para medir la atención") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (intento && (errorRazon || errorContacto || errorSla)) {
                    Text("Completa los campos obligatorios.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                intento = true
                if (!errorRazon && !errorContacto && !errorSla && horas != null) {
                    onGuardar(Cliente(razonSocial.trim(), contacto.trim(), horas, emptyList()))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoLocal(
    clientes: List<Cliente>,
    onCerrar: () -> Unit,
    onGuardar: (String, Local) -> Unit
) {
    var clienteSel by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }

    val errorCliente = clienteSel.isBlank()
    val errorNombre = nombre.isBlank()
    val errorDireccion = direccion.isBlank()

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo local") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoDesplegable(
                    etiqueta = "Cliente *",
                    opciones = clientes.map { it.nombre },
                    seleccion = clienteSel,
                    onSeleccion = { clienteSel = it },
                    error = intento && errorCliente
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del local *") },
                    isError = intento && errorNombre,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección *") },
                    isError = intento && errorDireccion,
                    modifier = Modifier.fillMaxWidth()
                )
                if (intento && (errorCliente || errorNombre || errorDireccion)) {
                    Text("Completa los campos obligatorios.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                intento = true
                if (!errorCliente && !errorNombre && !errorDireccion) {
                    onGuardar(clienteSel, Local(nombre.trim(), direccion.trim(), emptyList()))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoEquipo(
    clientes: List<Cliente>,
    onCerrar: () -> Unit,
    onGuardar: (String, String, Equipo) -> Unit
) {
    var localSel by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("") }
    var marca by rememberSaveable { mutableStateOf("") }
    var modelo by rememberSaveable { mutableStateOf("") }
    var exterior by rememberSaveable { mutableStateOf(false) }
    var intento by rememberSaveable { mutableStateOf(false) }

    val opcionesLocal = clientes.flatMap { c -> c.locales.map { l -> "${c.nombre} — ${l.nombre}" } }

    val errorLocal = localSel.isBlank()
    val errorTipo = tipo.isBlank()
    val errorMarca = marca.isBlank()
    val errorModelo = modelo.isBlank()

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo equipo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoDesplegable(
                    etiqueta = "Local *",
                    opciones = opcionesLocal,
                    seleccion = localSel,
                    onSeleccion = { localSel = it },
                    error = intento && errorLocal
                )
                CampoDesplegable(
                    etiqueta = "Tipo de equipo *",
                    opciones = TIPOS_EQUIPO,
                    seleccion = tipo,
                    onSeleccion = { tipo = it },
                    error = intento && errorTipo
                )
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca *") },
                    isError = intento && errorMarca,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = { Text("Modelo *") },
                    isError = intento && errorModelo,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Unidad exterior (azotea o patio)", modifier = Modifier.weight(1f))
                    Switch(checked = exterior, onCheckedChange = { exterior = it })
                }
                if (intento && (errorLocal || errorTipo || errorMarca || errorModelo)) {
                    Text("Completa los campos obligatorios.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                intento = true
                if (!errorLocal && !errorTipo && !errorMarca && !errorModelo) {
                    val nombreCliente = localSel.substringBefore(" — ")
                    val nombreLocal = localSel.substringAfter(" — ")
                    onGuardar(
                        nombreCliente,
                        nombreLocal,
                        Equipo(
                            nombre = "$tipo ${modelo.trim()}",
                            tipo = tipo,
                            marca = marca.trim(),
                            modelo = modelo.trim(),
                            exterior = exterior,
                            ubicacion = if (exterior) "Exterior" else "Interior",
                            ultimaMantencion = "Sin registro"
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoTecnico(
    existeCorreo: (String) -> Boolean,
    onCerrar: () -> Unit,
    onGuardar: (Tecnico) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }

    val errorNombre = nombre.isBlank()
    val errorCorreoVacio = !correo.contains("@")
    val errorCorreoDuplicado = !errorCorreoVacio && existeCorreo(correo)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo técnico") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo *") },
                    isError = intento && errorNombre,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo *") },
                    isError = intento && (errorCorreoVacio || errorCorreoDuplicado),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Perfil: Técnico de terreno", style = MaterialTheme.typography.bodyMedium)
                if (intento && errorCorreoDuplicado) {
                    Text("Ya existe un técnico con ese correo.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else if (intento && (errorNombre || errorCorreoVacio)) {
                    Text("Ingresa el nombre y un correo válido.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                intento = true
                if (!errorNombre && !errorCorreoVacio && !errorCorreoDuplicado) {
                    onGuardar(Tecnico(nombre.trim(), correo.trim()))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
