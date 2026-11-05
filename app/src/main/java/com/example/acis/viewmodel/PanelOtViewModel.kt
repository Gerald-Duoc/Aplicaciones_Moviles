package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.DatosDemo
import com.example.acis.data.DiaAgenda
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PanelOtUiState(
    val verPorTecnico: Boolean = false,
    val filtroEstado: String = "Todos",
    val filtroTecnico: String = "Todos",
    val filtroFecha: String = "Todos",
    val otAReprogramar: String? = null,
    val mensaje: String? = null,
    val ordenes: List<OrdenTrabajo> = emptyList()
) {
    val filtradas: List<OrdenTrabajo>
        get() = ordenes.filter { ot ->
            (filtroEstado == "Todos" || ot.estado.etiqueta == filtroEstado) &&
                (filtroTecnico == "Todos" || ot.tecnico == filtroTecnico) &&
                (filtroFecha == "Todos" || ot.fecha == filtroFecha)
        }

    val asignadas: Int get() = ordenes.count { it.estado == EstadoOt.ASIGNADA || it.estado == EstadoOt.ACEPTADA }
    val enEjecucion: Int get() = ordenes.count { it.estado == EstadoOt.EN_EJECUCION }
    val cerradas: Int get() = ordenes.count { it.estado == EstadoOt.CERRADA }
    val pendientes: Int get() = ordenes.count { it.estado == EstadoOt.PENDIENTE_ASIGNACION || it.estado == EstadoOt.CREADA }
}

class PanelOtViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PanelOtUiState())
    val uiState: StateFlow<PanelOtUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.update { it.copy(ordenes = DatosDemo.ordenes.toList()) }
    }

    fun setVerPorTecnico(valor: Boolean) = _uiState.update { it.copy(verPorTecnico = valor) }
    fun setFiltroEstado(valor: String) = _uiState.update { it.copy(filtroEstado = valor) }
    fun setFiltroTecnico(valor: String) = _uiState.update { it.copy(filtroTecnico = valor) }
    fun setFiltroFecha(valor: String) = _uiState.update { it.copy(filtroFecha = valor) }

    fun pedirReprogramar(id: String) = _uiState.update { it.copy(otAReprogramar = id) }
    fun cancelarReprogramar() = _uiState.update { it.copy(otAReprogramar = null) }

    fun reprogramar(id: String, dia: DiaAgenda) {
        DatosDemo.reprogramar(id, dia)
        _uiState.update { it.copy(otAReprogramar = null, mensaje = "OT reprogramada al ${dia.etiqueta}.") }
        cargar()
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
