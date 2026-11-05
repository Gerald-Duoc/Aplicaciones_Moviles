package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.Cliente
import com.example.acis.data.DatosDemo
import com.example.acis.data.DiaAgenda
import com.example.acis.data.Equipo
import com.example.acis.data.Local
import com.example.acis.data.OrdenTrabajo
import com.example.acis.data.Prioridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val SIN_TECNICO = "Sin asignar por ahora"

data class GestionOtUiState(
    val modo: String = "nueva",
    val otId: String = "",
    val ot: OrdenTrabajo? = null,
    val clienteSel: String = "",
    val localSel: String = "",
    val equipoSel: String = "",
    val tipoSel: String = "Correctiva",
    val prioridadSel: String = Prioridad.MEDIA.etiqueta,
    val diaSel: String = "",
    val tecnicoSel: String = "",
    val descripcion: String = "",
    val intentoGuardar: Boolean = false,
    val confirmacion: String? = null
) {
    val esNueva: Boolean get() = modo == "nueva"
    val titulo: String
        get() = when (modo) {
            "nueva" -> "Nueva orden de trabajo"
            "asignar" -> "Asignar OT"
            "reasignar" -> "Reasignar OT"
            else -> "Reprogramar OT"
        }
    val mostrarFecha: Boolean get() = esNueva || modo == "reprogramar"
    val mostrarTecnico: Boolean get() = modo != "reprogramar"
}

class GestionOtViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GestionOtUiState())
    val uiState: StateFlow<GestionOtUiState> = _uiState.asStateFlow()

    fun inicializar(modo: String, otId: String) {
        val ot = if (modo == "nueva") null else DatosDemo.ordenPorId(otId)
        _uiState.update { GestionOtUiState(modo = modo, otId = otId, ot = ot) }
    }

    fun refrescar() {
        val s = _uiState.value
        if (!s.esNueva) _uiState.update { it.copy(ot = DatosDemo.ordenPorId(s.otId)) }
    }

    fun onCliente(valor: String) = _uiState.update { it.copy(clienteSel = valor, localSel = "", equipoSel = "") }
    fun onLocal(valor: String) = _uiState.update { it.copy(localSel = valor, equipoSel = "") }
    fun onEquipo(valor: String) = _uiState.update { it.copy(equipoSel = valor) }
    fun onTipo(valor: String) = _uiState.update { it.copy(tipoSel = valor) }
    fun onPrioridad(valor: String) = _uiState.update { it.copy(prioridadSel = valor) }
    fun onDia(valor: String) = _uiState.update { it.copy(diaSel = valor) }
    fun onTecnico(valor: String) = _uiState.update { it.copy(tecnicoSel = valor) }
    fun onDescripcion(valor: String) = _uiState.update { it.copy(descripcion = valor) }

    private fun textoTecnico(nombre: String): String = "$nombre (${DatosDemo.cargaTecnico(nombre)} OT activas)"

    fun opcionesTecnico(): List<String> {
        val s = _uiState.value
        return (if (s.esNueva) listOf(SIN_TECNICO) else emptyList()) +
            DatosDemo.tecnicos.map { textoTecnico(it.nombre) }
    }

    fun nombreTecnicoDesdeSeleccion(sel: String): String? {
        if (sel.isBlank() || sel == SIN_TECNICO) return null
        return sel.substringBefore(" (")
    }

    fun guardar() {
        val s = _uiState.value
        val cliente = DatosDemo.clientes.firstOrNull { it.nombre == s.clienteSel }
        val local = cliente?.locales?.firstOrNull { it.nombre == s.localSel }
        val equipo = local?.equipos?.firstOrNull { it.nombre == s.equipoSel }
        val dia = DatosDemo.dias.firstOrNull { textoDia(it) == s.diaSel }

        val errorCliente = s.esNueva && cliente == null
        val errorLocal = s.esNueva && local == null
        val errorEquipo = s.esNueva && equipo == null
        val errorDescripcion = s.esNueva && s.descripcion.isBlank()
        val errorFecha = s.mostrarFecha && dia == null
        val errorTecnico = !s.esNueva && s.mostrarTecnico && s.tecnicoSel.isBlank()
        val hayErrores = errorCliente || errorLocal || errorEquipo || errorDescripcion || errorFecha || errorTecnico

        if (hayErrores) {
            _uiState.update { it.copy(intentoGuardar = true) }
            return
        }

        val nombreTecnico = nombreTecnicoDesdeSeleccion(s.tecnicoSel)
        val confirmacion = when (s.modo) {
            "nueva" -> {
                val prioridad = Prioridad.entries.firstOrNull { it.etiqueta == s.prioridadSel } ?: Prioridad.MEDIA
                val nueva = DatosDemo.crearOrden(
                    cliente = cliente!!,
                    local = local!!,
                    equipo = equipo!!,
                    tipo = s.tipoSel,
                    prioridad = prioridad,
                    dia = dia!!,
                    tecnico = nombreTecnico,
                    descripcion = s.descripcion.trim()
                )
                if (nombreTecnico != null) {
                    "${nueva.id} creada y asignada a $nombreTecnico. El técnico la recibe en su aplicación."
                } else {
                    "${nueva.id} creada en estado \"Creada\". Falta asignarla a un técnico."
                }
            }
            "reprogramar" -> {
                DatosDemo.reprogramar(s.otId, dia!!)
                "Nueva fecha guardada: ${dia.etiqueta}. El técnico asignado será notificado."
            }
            else -> {
                if (nombreTecnico != null) {
                    DatosDemo.asignar(s.otId, nombreTecnico)
                    if (s.modo == "reasignar") {
                        "OT reasignada a $nombreTecnico. El técnico anterior dejó de verla en su listado."
                    } else {
                        "OT asignada a $nombreTecnico. La recibe en su aplicación."
                    }
                } else ""
            }
        }
        _uiState.update { it.copy(intentoGuardar = true, confirmacion = confirmacion) }
    }

    fun consumirConfirmacion() = _uiState.update { it.copy(confirmacion = null) }
}

fun textoDia(dia: DiaAgenda): String = "${dia.etiqueta} · ${dia.clima.icono} ${dia.clima.etiqueta}"
