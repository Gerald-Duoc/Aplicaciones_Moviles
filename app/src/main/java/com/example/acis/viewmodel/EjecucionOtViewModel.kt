package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.DatosDemo
import com.example.acis.data.OrdenTrabajo
import com.example.acis.data.RegistroEjecucion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EjecucionOtUiState(
    val ot: OrdenTrabajo? = null,
    val paso: Int = 0,
    val mostrarImprevisto: Boolean = false,
    val motivoImprevisto: String = "",
    val errorImprevisto: Boolean = false,
    val errorCierre: String? = null,
    val cerrarCompletado: Boolean = false,
    val navegarAtras: Boolean = false
)

class EjecucionOtViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EjecucionOtUiState())
    val uiState: StateFlow<EjecucionOtUiState> = _uiState.asStateFlow()

    private var otId: String = ""

    fun cargar(id: String) {
        otId = id
        _uiState.update { it.copy(ot = DatosDemo.ordenPorId(id)) }
    }

    fun registro(): RegistroEjecucion = DatosDemo.registro(otId)

    fun refrescarOt() {
        _uiState.update { it.copy(ot = DatosDemo.ordenPorId(otId)) }
    }

    fun setPaso(paso: Int) = _uiState.update { it.copy(paso = paso) }

    fun guardarCheckIn(reg: RegistroEjecucion) {
        val ahora = System.currentTimeMillis()
        reg.checkInMillis = ahora
        reg.checkInTexto = DatosDemo.ahoraTexto()
        DatosDemo.registrarCambioLocal()
        DatosDemo.marcarEnEjecucion(otId)
        refrescarOt()
    }

    fun guardarCheckOut(reg: RegistroEjecucion) {
        reg.checkOutMillis = System.currentTimeMillis()
        reg.checkOutTexto = DatosDemo.ahoraTexto()
        DatosDemo.registrarCambioLocal()
    }

    fun abrirImprevisto() = _uiState.update { it.copy(mostrarImprevisto = true, motivoImprevisto = "", errorImprevisto = false) }
    fun cerrarImprevisto() = _uiState.update { it.copy(mostrarImprevisto = false, motivoImprevisto = "", errorImprevisto = false) }
    fun onMotivoImprevistoChange(valor: String) = _uiState.update { it.copy(motivoImprevisto = valor, errorImprevisto = false) }

    fun confirmarImprevisto() {
        val motivo = _uiState.value.motivoImprevisto.trim()
        if (motivo.isBlank()) {
            _uiState.update { it.copy(errorImprevisto = true) }
            return
        }
        DatosDemo.reportarImprevisto(otId, motivo)
        _uiState.update { it.copy(mostrarImprevisto = false, navegarAtras = true) }
    }

    fun cerrarOt(reg: RegistroEjecucion) {
        val error = when {
            !reg.ubicacionAutorizada -> "Debes autorizar el uso de tu ubicación (paso 1)."
            reg.checkInTexto.isBlank() || reg.checkOutTexto.isBlank() -> "Debes registrar el check-in y el check-out (paso 1)."
            reg.diagnostico.isBlank() -> "El diagnóstico es obligatorio (paso 2)."
            reg.actividades.isEmpty() -> "Agrega al menos una actividad realizada (paso 3)."
            reg.materiales.isEmpty() -> "Agrega al menos un material utilizado (paso 3)."
            reg.fotosAntes.isEmpty() || reg.fotosDespues.isEmpty() -> "Debes agregar al menos una foto antes y una después (paso 3)."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorCierre = error) }
            return
        }
        DatosDemo.cerrar(otId, reg)
        _uiState.update { it.copy(errorCierre = null, cerrarCompletado = true) }
    }

    fun consumirNavegacion() = _uiState.update { it.copy(navegarAtras = false, cerrarCompletado = false) }
}
