package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.DatosDemo
import com.example.acis.data.OrdenTrabajo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DetalleOtUiState(
    val ot: OrdenTrabajo? = null,
    val mostrarRechazo: Boolean = false,
    val motivoRechazo: String = "",
    val errorMotivo: Boolean = false,
    val mensajeOk: String? = null,
    val navegarAtras: Boolean = false
)

class DetalleOtViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleOtUiState())
    val uiState: StateFlow<DetalleOtUiState> = _uiState.asStateFlow()

    private var otId: String = ""

    fun cargar(id: String) {
        otId = id
        _uiState.update { it.copy(ot = DatosDemo.ordenPorId(id)) }
    }

    fun aceptar() {
        DatosDemo.aceptar(otId)
        _uiState.update { it.copy(ot = DatosDemo.ordenPorId(otId), mensajeOk = "OT aceptada. Puedes iniciar la visita.") }
    }

    fun abrirRechazo() {
        _uiState.update { it.copy(mostrarRechazo = true, motivoRechazo = "", errorMotivo = false) }
    }

    fun cerrarRechazo() {
        _uiState.update { it.copy(mostrarRechazo = false, motivoRechazo = "", errorMotivo = false) }
    }

    fun onMotivoChange(valor: String) {
        _uiState.update { it.copy(motivoRechazo = valor, errorMotivo = false) }
    }

    fun confirmarRechazo() {
        val motivo = _uiState.value.motivoRechazo.trim()
        if (motivo.isBlank()) {
            _uiState.update { it.copy(errorMotivo = true) }
            return
        }
        DatosDemo.rechazar(otId, motivo)
        _uiState.update { it.copy(mostrarRechazo = false, navegarAtras = true) }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensajeOk = null) }
    }

    fun consumirNavegacion() {
        _uiState.update { it.copy(navegarAtras = false) }
    }
}
