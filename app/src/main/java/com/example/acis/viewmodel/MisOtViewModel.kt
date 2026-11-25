package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acis.data.DatosDemo
import com.example.acis.data.EstadoOt
import com.example.acis.data.OrdenTrabajo
import com.example.acis.repository.AcisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado/estructura de la pantalla "Mis OT del día" (técnico).
 * El ViewModel guarda este estado; la UI solo lo observa.
 */
data class MisOtUiState(
    val tecnicoNombre: String = DatosDemo.tecnicoActual,
    val misOt: List<OrdenTrabajo> = emptyList(),
    val avisoNuevaOtVisible: Boolean = true,
    val fuente: String = "Cargando…",
    val cargando: Boolean = false
) {
    val pendientes: Int get() = misOt.count { it.estado != EstadoOt.CERRADA }
    val nuevaOt: OrdenTrabajo? get() = misOt.firstOrNull { it.estado == EstadoOt.ASIGNADA }
}

class MisOtViewModel : ViewModel() {
    private val repositorio = AcisRepository()
    private val _uiState = MutableStateFlow(MisOtUiState())
    val uiState: StateFlow<MisOtUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    /** Carga/refresca las OT del técnico desde la API (o el respaldo local). */
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            repositorio.ordenesDelTecnico(DatosDemo.tecnicoActual)
                .onSuccess { (lista, fuente) ->
                    _uiState.update {
                        it.copy(
                            tecnicoNombre = DatosDemo.tecnicoActual,
                            misOt = lista,
                            fuente = fuente,
                            cargando = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(fuente = "Error al cargar las OT", cargando = false)
                    }
                }
        }
    }

    fun descartarAvisoNuevaOt() {
        _uiState.update { it.copy(avisoNuevaOtVisible = false) }
    }
}
