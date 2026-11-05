package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.Conformidad
import com.example.acis.data.DatosDemo
import com.example.acis.data.Equipo
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ClienteUiState(
    val clienteNombre: String = DatosDemo.clienteActual,
    val localNombre: String = DatosDemo.localActual,
    val misOt: List<OrdenTrabajo> = emptyList(),
    val expandidaId: String? = null,
    val mostrarSolicitud: Boolean = false,
    val otAObservar: String? = null,
    val mensaje: String? = null
) {
    val porRevisar: OrdenTrabajo?
        get() = misOt.firstOrNull { it.estado == EstadoOt.CERRADA && it.conformidad == Conformidad.SIN_REVISAR }

    val equiposDisponibles: List<Equipo>
        get() = DatosDemo.clientes
            .firstOrNull { it.nombre == DatosDemo.clienteActual }
            ?.locales?.firstOrNull { it.nombre == DatosDemo.localActual }
            ?.equipos ?: emptyList()
}

class ClienteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    fun cargar() {
        val lista = DatosDemo.ordenes
            .filter { it.cliente == DatosDemo.clienteActual && it.local == DatosDemo.localActual }
            .sortedWith(
                compareByDescending<OrdenTrabajo> {
                    it.estado == EstadoOt.CERRADA && it.conformidad == Conformidad.SIN_REVISAR
                }.thenBy { it.estado == EstadoOt.CERRADA }
            )
        _uiState.update { it.copy(clienteNombre = DatosDemo.clienteActual, localNombre = DatosDemo.localActual, misOt = lista) }
    }

    fun alternarExpandida(id: String) {
        _uiState.update { it.copy(expandidaId = if (it.expandidaId == id) null else id) }
    }

    fun expandir(id: String) = _uiState.update { it.copy(expandidaId = id) }

    fun abrirSolicitud() = _uiState.update { it.copy(mostrarSolicitud = true) }
    fun cerrarSolicitud() = _uiState.update { it.copy(mostrarSolicitud = false) }

    fun enviarSolicitud(equipo: Equipo, descripcion: String) {
        DatosDemo.solicitarMantencion(equipo, descripcion.trim())
        _uiState.update { it.copy(mostrarSolicitud = false, mensaje = "Solicitud enviada al planificador.") }
        cargar()
    }

    fun aprobar(id: String) {
        DatosDemo.aprobarTrabajo(id)
        _uiState.update { it.copy(mensaje = "Gracias, quedó registrada tu conformidad.") }
        cargar()
    }

    fun pedirObservar(id: String) = _uiState.update { it.copy(otAObservar = id) }
    fun cancelarObservar() = _uiState.update { it.copy(otAObservar = null) }

    fun enviarObservacion(id: String, comentario: String) {
        DatosDemo.observarTrabajo(id, comentario.trim())
        _uiState.update { it.copy(otAObservar = null, mensaje = "Comentario enviado al planificador.") }
        cargar()
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
