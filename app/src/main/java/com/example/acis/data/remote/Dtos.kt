package com.example.acis.data.remote

/**
 * DTOs (Data Transfer Objects): representan exactamente el JSON que envía la API
 * Spring Boot. Se declaran con valores por defecto para que un campo ausente no
 * rompa la app.
 */

data class LoginRequest(
    val correo: String,
    val clave: String
)

data class UsuarioDto(
    val id: Long? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val perfil: String = "",
    val clienteId: Long? = null,
    val localId: Long? = null
)

data class LoginResponse(
    val token: String = "",
    val expiraEn: String? = null,
    val usuario: UsuarioDto = UsuarioDto()
)

/** El OrdenDto de la API. Las fechas llegan como texto ISO (2026-11-26). */
data class OrdenDto(
    val id: Long = 0,
    val codigo: String? = null,
    val estado: String = "",
    val prioridad: String = "",
    val tipo: String = "",
    val fecha: String? = null,
    val descripcion: String? = null,
    val observacion: String? = null,
    val slaHoras: Int? = null,
    val tiempoRealMin: Int? = null,
    val clienteNombre: String? = null,
    val localNombre: String? = null,
    val direccion: String? = null,
    val equipoId: Long? = null,
    val equipoNombre: String? = null,
    val equipoExterior: Boolean = false,
    val ubicacionEquipo: String? = null,
    val ultimaMantencion: String? = null,
    val tecnicoId: Long? = null,
    val tecnicoNombre: String? = null,
    val ubicacionAutorizada: Boolean = false,
    val checkIn: String? = null,
    val checkOut: String? = null,
    val diagnostico: String? = null,
    val actividades: List<String>? = null,
    val materiales: List<String>? = null,
    val fotosAntes: Long? = null,
    val fotosDespues: Long? = null,
    val conformidad: String? = null,
    val fechaConformidad: String? = null,
    val comentarioCliente: String? = null
)

data class SaludDto(
    val estado: String = "",
    val hora: String? = null
)
