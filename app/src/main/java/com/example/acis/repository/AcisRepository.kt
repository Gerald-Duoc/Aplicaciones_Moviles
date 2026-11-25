package com.example.acis.repository

import com.example.acis.data.DatosDemo
import com.example.acis.data.OrdenTrabajo
import com.example.acis.data.Perfil
import com.example.acis.data.SessionStore
import com.example.acis.data.Clima
import com.example.acis.data.Conformidad
import com.example.acis.data.EstadoOt
import com.example.acis.data.Prioridad
import com.example.acis.data.remote.ApiClient
import com.example.acis.data.remote.LoginRequest
import com.example.acis.data.remote.OrdenDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository (MVVM): capa que obtiene y entrega datos a los ViewModels.
 *
 * Ahora tiene DOS fuentes:
 *  1) La API Spring Boot (real,guarda en MySQL).
 *  2) DatosDemo (respaldo si la API no está disponible).
 *
 * La UI no sabe de dónde vienen los datos: solo recibe OrdenTrabajo y una
 * etiqueta "fuente" que muestra en pantalla.
 */
class AcisRepository {

    /** HU-01: login contra la API. Si falla la red, usa el modo demostración. */
    suspend fun login(correo: String, clave: String): Result<Perfil> = withContext(Dispatchers.IO) {
        try {
            val respuesta = ApiClient.api().login(LoginRequest(correo.trim(), clave))
            val perfil = mapPerfil(respuesta.usuario.perfil)
            SessionStore.guardarSesion(respuesta.token, perfil)
            Result.success(perfil)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Result.failure(Exception("Correo o clave incorrectos. Inténtalo nuevamente."))
            } else {
                SessionStore.ultimoError = "HTTP ${e.code()}: ${e.message()}"
                modoDemo(correo, clave)
            }
        } catch (e: Exception) {
            SessionStore.ultimoError = "${e.javaClass.simpleName}: ${e.message}"
            modoDemo(correo, clave)
        }
    }

    /**
     * HU-13: obtiene las OT del técnico desde la API.
     * Devuelve también un texto con la fuente para demostrar en la UI si los
     * datos vinieron del servidor o del respaldo local.
     */
    suspend fun ordenesDelTecnico(nombre: String): Result<Pair<List<OrdenTrabajo>, String>> =
        withContext(Dispatchers.IO) {
            val autorizacion = SessionStore.authorization()
            if (SessionStore.modoApi && autorizacion != null) {
                try {
                    val lista = ApiClient.api().ordenes(autorizacion)
                        .map(::mapOrden)
                        .filter { it.tecnico == nombre }
                        .sortedBy { it.estado == EstadoOt.CERRADA }
                    return@withContext Result.success(
                        lista to "Conectado a la API · ${SessionStore.baseUrl}"
                    )
                } catch (e: Exception) {
                    // Si la API se cae, seguimos con los datos de ejemplo.
                    SessionStore.ultimoError = "${e.javaClass.simpleName}: ${e.message}"
                    SessionStore.modoApi = false
                }
            }
            val demo = DatosDemo.ordenes
                .filter { it.tecnico == nombre }
                .sortedBy { it.estado == EstadoOt.CERRADA }
            val detalle = SessionStore.ultimoError?.let { " · $it" } ?: ""
            Result.success(demo to "Modo demostración (API no disponible)$detalle")
        }

    /** Compatibilidad con el código anterior: búsqueda local por id. */
    fun ordenPorId(id: String): OrdenTrabajo? = DatosDemo.ordenPorId(id)

    fun todasLasOrdenes(): List<OrdenTrabajo> = DatosDemo.ordenes.toList()

    private fun modoDemo(correo: String, clave: String): Result<Perfil> {
        val perfil = DatosDemo.autenticar(correo, clave)
            ?: return Result.failure(
                Exception("No se pudo conectar con la API y las credenciales tampoco son válidas.")
            )
        SessionStore.guardarModoDemo(perfil)
        return Result.success(perfil)
    }

    private fun mapPerfil(valor: String?): Perfil =
        runCatching { Perfil.valueOf(valor?.trim()?.uppercase() ?: "") }
            .getOrDefault(Perfil.TECNICO)

    /** Convierte el OrdenDto de la API al modelo que ya usa la app. */
    private fun mapOrden(d: OrdenDto): OrdenTrabajo = OrdenTrabajo(
        id = d.codigo?.takeIf { it.isNotBlank() } ?: "OT-${d.id}",
        cliente = d.clienteNombre ?: "",
        local = d.localNombre ?: "",
        direccion = d.direccion ?: "",
        equipo = d.equipoNombre ?: "",
        exterior = d.equipoExterior,
        ubicacionEquipo = d.ubicacionEquipo ?: "",
        ultimaMantencion = d.ultimaMantencion ?: "",
        tipo = d.tipo,
        prioridad = runCatching { Prioridad.valueOf(d.prioridad.uppercase()) }
            .getOrDefault(Prioridad.MEDIA),
        estado = runCatching { EstadoOt.valueOf(d.estado.uppercase()) }
            .getOrDefault(EstadoOt.CREADA),
        tecnico = d.tecnicoNombre,
        fecha = d.fecha ?: "",
        clima = Clima.DESPEJADO,
        descripcion = d.descripcion ?: "",
        slaHoras = d.slaHoras ?: 0,
        tiempoRealMin = d.tiempoRealMin,
        diagnostico = d.diagnostico,
        actividades = d.actividades ?: emptyList(),
        materiales = d.materiales ?: emptyList(),
        conformidad = runCatching {
            Conformidad.valueOf((d.conformidad ?: "SIN_REVISAR").uppercase())
        }.getOrDefault(Conformidad.SIN_REVISAR),
        fechaConformidad = d.fechaConformidad,
        comentarioCliente = d.comentarioCliente,
        observacion = d.observacion,
        historial = emptyList()
    )
}
