package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.Cliente
import com.example.acis.data.DatosDemo
import com.example.acis.data.Equipo
import com.example.acis.data.Local
import com.example.acis.data.Tecnico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CatastroUiState(
    val seccion: Int = 0,
    val mostrarFormulario: Boolean = false,
    val clientes: List<Cliente> = emptyList(),
    val tecnicos: List<Tecnico> = emptyList()
) {
    val locales: List<Pair<String, Local>>
        get() = clientes.flatMap { c -> c.locales.map { l -> c.nombre to l } }
    val equipos: List<Triple<String, String, Equipo>>
        get() = clientes.flatMap { c -> c.locales.flatMap { l -> l.equipos.map { e -> Triple(c.nombre, l.nombre, e) } } }
}

class CatastroViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CatastroUiState())
    val uiState: StateFlow<CatastroUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.update { it.copy(clientes = DatosDemo.clientes.toList(), tecnicos = DatosDemo.tecnicos.toList()) }
    }

    fun setSeccion(seccion: Int) = _uiState.update { it.copy(seccion = seccion) }
    fun abrirFormulario() = _uiState.update { it.copy(mostrarFormulario = true) }
    fun cerrarFormulario() = _uiState.update { it.copy(mostrarFormulario = false) }

    fun existeCorreoTecnico(correo: String): Boolean = DatosDemo.existeCorreoTecnico(correo)
    fun cargaTecnico(nombre: String): Int = DatosDemo.cargaTecnico(nombre)

    fun agregarCliente(cliente: Cliente) {
        DatosDemo.agregarCliente(cliente)
        cargar()
    }

    fun agregarLocal(nombreCliente: String, local: Local) {
        DatosDemo.agregarLocal(nombreCliente, local)
        cargar()
    }

    fun agregarEquipo(nombreCliente: String, nombreLocal: String, equipo: Equipo) {
        DatosDemo.agregarEquipo(nombreCliente, nombreLocal, equipo)
        cargar()
    }

    fun agregarTecnico(tecnico: Tecnico) {
        DatosDemo.agregarTecnico(tecnico)
        cargar()
    }
}
