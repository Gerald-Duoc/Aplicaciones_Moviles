package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.DatosDemo
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado/estructura de la pantalla "Mis OT del día" (técnico).
 * El ViewModel guarda este estado; la UI solo lo observa.
 */
data class MisOtUiState(
    val tecnicoNombre: String = DatosDemo.tecnicoActual,
    val misOt: List<OrdenTrabajo> = emptyList(),
    val avisoNuevaOtVisible: Boolean = true
) {
    val pendientes: Int get() = misOt.count { it.estado != EstadoOt.CERRADA }
    val nuevaOt: OrdenTrabajo? get() = misOt.firstOrNull { it.estado == EstadoOt.ASIGNADA }
}

class MisOtViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MisOtUiState())
    val uiState: StateFlow<MisOtUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    /** Carga/refresca las OT del técnico desde la fuente de datos. */
    fun cargar() {
        val lista = DatosDemo.ordenes
            .filter { it.tecnico == DatosDemo.tecnicoActual }
            .sortedBy { it.estado == EstadoOt.CERRADA }
        _uiState.update { it.copy(tecnicoNombre = DatosDemo.tecnicoActual, misOt = lista) }
    }

    fun descartarAvisoNuevaOt() {
        _uiState.update { it.copy(avisoNuevaOtVisible = false) }
    }
}
