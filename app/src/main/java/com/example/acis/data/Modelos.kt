package com.example.acis.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Perfil { TECNICO, PLANIFICADOR, CLIENTE }

enum class Prioridad(val etiqueta: String) {
    ALTA("Alta"),
    MEDIA("Media"),
    BAJA("Baja")
}

enum class EstadoOt(val etiqueta: String) {
    CREADA("Creada"),
    PENDIENTE_ASIGNACION("Pendiente de asignación"),
    ASIGNADA("Asignada"),
    ACEPTADA("Aceptada"),
    EN_EJECUCION("En ejecución"),
    PENDIENTE_IMPREVISTO("Pendiente por imprevisto"),
    CERRADA("Cerrada")
}

enum class Clima(val etiqueta: String, val icono: String) {
    DESPEJADO("Despejado", "☀️"),
    LLUVIA("Lluvia", "🌧️"),
    VIENTO_FUERTE("Viento fuerte", "💨")
}

enum class Conformidad { SIN_REVISAR, APROBADA, OBSERVADA }

data class UsuarioPrueba(
    val correo: String,
    val clave: String,
    val perfil: Perfil,
    val etiqueta: String
)

data class Intervencion(
    val fecha: String,
    val tipo: String,
    val diagnostico: String
)

data class Equipo(
    val nombre: String,
    val tipo: String,
    val marca: String,
    val modelo: String,
    val exterior: Boolean,
    val ubicacion: String,
    val ultimaMantencion: String
)

data class Local(
    val nombre: String,
    val direccion: String,
    val equipos: List<Equipo>
)

data class Cliente(
    val nombre: String,
    val contacto: String,
    val slaHoras: Int,
    val locales: List<Local>
)

data class Tecnico(
    val nombre: String,
    val correo: String
)

data class DiaAgenda(
    val etiqueta: String,
    val clima: Clima
)

data class OrdenTrabajo(
    val id: String,
    val cliente: String,
    val local: String,
    val direccion: String,
    val equipo: String,
    val exterior: Boolean,
    val ubicacionEquipo: String,
    val ultimaMantencion: String,
    val tipo: String,
    val prioridad: Prioridad,
    val estado: EstadoOt,
    val tecnico: String?,
    val fecha: String,
    val clima: Clima,
    val descripcion: String,
    val slaHoras: Int,
    val tiempoRealMin: Int? = null,
    val diagnostico: String? = null,
    val actividades: List<String> = emptyList(),
    val materiales: List<String> = emptyList(),
    val conformidad: Conformidad = Conformidad.SIN_REVISAR,
    val fechaConformidad: String? = null,
    val comentarioCliente: String? = null,
    val observacion: String? = null,
    val historial: List<Intervencion> = emptyList()
)

/**
 * Registro que va llenando el técnico durante la visita (check-in, diagnóstico,
 * actividades, materiales y fotos). Se guarda en memoria, por eso se mantiene
 * aunque cambies de pantalla o rotes el teléfono.
 */
class RegistroEjecucion {
    var ubicacionAutorizada by mutableStateOf(false)
    var checkInMillis by mutableStateOf<Long?>(null)
    var checkInTexto by mutableStateOf("")
    var checkOutMillis by mutableStateOf<Long?>(null)
    var checkOutTexto by mutableStateOf("")
    var diagnostico by mutableStateOf("")
    val actividades = mutableStateListOf<String>()
    val materiales = mutableStateListOf<String>()
    val fotosAntes = mutableStateListOf<String>()
    val fotosDespues = mutableStateListOf<String>()

    /** Minutos entre check-in y check-out (mínimo 1). */
    val tiempoAtencionMin: Int?
        get() {
            val entrada = checkInMillis
            val salida = checkOutMillis
            if (entrada == null || salida == null) return null
            return maxOf(1, ((salida - entrada) / 60000L).toInt())
        }
}

fun minutosATexto(min: Int): String {
    val horas = min / 60
    val minutos = min % 60
    return when {
        horas == 0 -> "$minutos min"
        minutos == 0 -> "$horas h"
        else -> "$horas h $minutos min"
    }
}
