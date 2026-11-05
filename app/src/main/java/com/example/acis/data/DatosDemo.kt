package com.example.acis.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Datos de ejemplo en memoria. No hay base de datos ni servidor: es un prototipo
 * de pantallas. Al cerrar la app los cambios se pierden.
 */
object DatosDemo {

    // ---------------------------------------------------------------
    // Sesión de prueba
    // ---------------------------------------------------------------
    const val tecnicoActual = "Carlos Muñoz"
    const val clienteActual = "Supermercado Cordillera"
    const val localActual = "Sucursal Independencia"

    val usuariosPrueba = listOf(
        UsuarioPrueba("tecnico@acis.cl", "1234", Perfil.TECNICO, "Técnico de terreno"),
        UsuarioPrueba("planificador@acis.cl", "1234", Perfil.PLANIFICADOR, "Planificador"),
        UsuarioPrueba("cliente@acis.cl", "1234", Perfil.CLIENTE, "Cliente")
    )

    fun autenticar(correo: String, clave: String): Perfil? {
        return usuariosPrueba.firstOrNull {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.clave == clave
        }?.perfil
    }

    // ---------------------------------------------------------------
    // Conexión simulada (modo sin conexión)
    // ---------------------------------------------------------------
    var sinConexion by mutableStateOf(false)
        private set
    var pendientesSync by mutableStateOf(0)
        private set
    var mensajeSync by mutableStateOf("")
        private set

    fun cambiarConexion(sinSenal: Boolean) {
        if (sinSenal) {
            mensajeSync = ""
        } else {
            mensajeSync = if (pendientesSync > 0) {
                "Se sincronizaron $pendientesSync registros, sin duplicados."
            } else {
                "Todo está sincronizado."
            }
            pendientesSync = 0
        }
        sinConexion = sinSenal
    }

    /** Cada acción del técnico queda "pendiente de enviar" si no hay señal. */
    fun registrarCambioLocal() {
        if (sinConexion) pendientesSync += 1
    }

    // ---------------------------------------------------------------
    // Agenda y pronóstico
    // ---------------------------------------------------------------
    val dias = listOf(
        DiaAgenda("Mar 27/10", Clima.DESPEJADO),
        DiaAgenda("Mié 28/10", Clima.LLUVIA),
        DiaAgenda("Jue 29/10", Clima.VIENTO_FUERTE),
        DiaAgenda("Vie 30/10", Clima.DESPEJADO),
        DiaAgenda("Lun 02/11", Clima.LLUVIA)
    )

    // ---------------------------------------------------------------
    // Catastro: clientes, locales, equipos y técnicos
    // ---------------------------------------------------------------
    val clientes = mutableStateListOf(
        Cliente(
            nombre = "Supermercado Cordillera",
            contacto = "Ana Pérez · ana.perez@cordillera.cl",
            slaHoras = 4,
            locales = listOf(
                Local(
                    nombre = "Sucursal Independencia",
                    direccion = "Av. Independencia 1250, Independencia",
                    equipos = listOf(
                        Equipo("Condensadora rooftop 1", "Condensadora", "FrioMax", "CX-450", true, "Azotea", "15/07/2026"),
                        Equipo("Cámara frigorífica 1", "Cámara frigorífica", "PolarTec", "CF-220", false, "Bodega", "02/09/2026")
                    )
                ),
                Local(
                    nombre = "Sucursal Maipú",
                    direccion = "Av. Pajaritos 3400, Maipú",
                    equipos = listOf(
                        Equipo("Condensadora patio 1", "Condensadora", "FrioMax", "CX-300", true, "Patio exterior", "20/06/2026"),
                        Equipo("Cámara frigorífica 2", "Cámara frigorífica", "PolarTec", "CF-180", false, "Bodega", "10/08/2026")
                    )
                )
            )
        ),
        Cliente(
            nombre = "Centro Comercial Los Alerces",
            contacto = "Marcelo Díaz · mdiaz@losalerces.cl",
            slaHoras = 6,
            locales = listOf(
                Local(
                    nombre = "Torre Oriente",
                    direccion = "Av. Apoquindo 4500, Las Condes",
                    equipos = listOf(
                        Equipo("Chiller central", "Chiller", "ClimaTec", "CH-900", true, "Patio técnico", "30/07/2026"),
                        Equipo("Split sala técnica", "Split", "ClimaTec", "SP-120", false, "Sala técnica", "12/09/2026")
                    )
                )
            )
        ),
        Cliente(
            nombre = "Retail Moderna",
            contacto = "Paula Fuentes · pfuentes@retailmoderna.cl",
            slaHoras = 3,
            locales = listOf(
                Local(
                    nombre = "Tienda Costanera",
                    direccion = "Av. Andrés Bello 2425, Providencia",
                    equipos = listOf(
                        Equipo("Climatización tienda", "Rooftop", "ClimaTec", "RT-600", true, "Azotea", "05/08/2026"),
                        Equipo("Vitrina refrigerada 3", "Vitrina", "PolarTec", "VR-90", false, "Salón de ventas", "18/09/2026")
                    )
                )
            )
        )
    )

    val tecnicos = mutableStateListOf(
        Tecnico("Carlos Muñoz", "tecnico@acis.cl"),
        Tecnico("Javiera Rojas", "jrojas@acis.cl"),
        Tecnico("Matías Soto", "msoto@acis.cl")
    )

    fun existeCorreoTecnico(correo: String): Boolean {
        return tecnicos.any { it.correo.equals(correo.trim(), ignoreCase = true) }
    }

    fun cargaTecnico(nombre: String): Int {
        return ordenes.count { it.tecnico == nombre && it.estado != EstadoOt.CERRADA }
    }

    fun agregarCliente(cliente: Cliente) {
        clientes.add(cliente)
    }

    fun agregarLocal(nombreCliente: String, local: Local) {
        val i = clientes.indexOfFirst { it.nombre == nombreCliente }
        if (i >= 0) {
            clientes[i] = clientes[i].copy(locales = clientes[i].locales + local)
        }
    }

    fun agregarEquipo(nombreCliente: String, nombreLocal: String, equipo: Equipo) {
        val i = clientes.indexOfFirst { it.nombre == nombreCliente }
        if (i < 0) return
        val cliente = clientes[i]
        val locales = cliente.locales.map {
            if (it.nombre == nombreLocal) it.copy(equipos = it.equipos + equipo) else it
        }
        clientes[i] = cliente.copy(locales = locales)
    }

    fun agregarTecnico(tecnico: Tecnico) {
        tecnicos.add(tecnico)
    }

    // ---------------------------------------------------------------
    // Órdenes de trabajo
    // ---------------------------------------------------------------
    private var siguienteNumero = 1046

    val ordenes = mutableStateListOf(
        OrdenTrabajo(
            id = "OT-1041",
            cliente = "Supermercado Cordillera",
            local = "Sucursal Independencia",
            direccion = "Av. Independencia 1250, Independencia",
            equipo = "Condensadora rooftop 1",
            exterior = true,
            ubicacionEquipo = "Azotea",
            ultimaMantencion = "15/07/2026",
            tipo = "Correctiva",
            prioridad = Prioridad.ALTA,
            estado = EstadoOt.ASIGNADA,
            tecnico = "Carlos Muñoz",
            fecha = "Hoy lun 26/10",
            clima = Clima.LLUVIA,
            descripcion = "La unidad no enfría y el compresor hace un ruido anormal.",
            slaHoras = 4,
            historial = listOf(
                Intervencion("15/07/2026", "Preventiva", "Limpieza de serpentín y revisión de presiones. Sin fallas."),
                Intervencion("03/03/2026", "Correctiva", "Reemplazo del condensador de arranque del ventilador.")
            )
        ),
        OrdenTrabajo(
            id = "OT-1042",
            cliente = "Retail Moderna",
            local = "Tienda Costanera",
            direccion = "Av. Andrés Bello 2425, Providencia",
            equipo = "Vitrina refrigerada 3",
            exterior = false,
            ubicacionEquipo = "Salón de ventas",
            ultimaMantencion = "18/09/2026",
            tipo = "Preventiva",
            prioridad = Prioridad.MEDIA,
            estado = EstadoOt.ACEPTADA,
            tecnico = "Carlos Muñoz",
            fecha = "Hoy lun 26/10",
            clima = Clima.DESPEJADO,
            descripcion = "Mantención preventiva trimestral: limpieza y revisión de temperatura.",
            slaHoras = 3,
            historial = listOf(
                Intervencion("18/09/2026", "Preventiva", "Filtros saturados, se reemplazaron.")
            )
        ),
        OrdenTrabajo(
            id = "OT-1043",
            cliente = "Centro Comercial Los Alerces",
            local = "Torre Oriente",
            direccion = "Av. Apoquindo 4500, Las Condes",
            equipo = "Chiller central",
            exterior = true,
            ubicacionEquipo = "Patio técnico",
            ultimaMantencion = "30/07/2026",
            tipo = "Preventiva",
            prioridad = Prioridad.MEDIA,
            estado = EstadoOt.ASIGNADA,
            tecnico = "Carlos Muñoz",
            fecha = "Hoy lun 26/10",
            clima = Clima.VIENTO_FUERTE,
            descripcion = "Revisión general del chiller y toma de presiones.",
            slaHoras = 6,
            historial = listOf(
                Intervencion("30/07/2026", "Preventiva", "Nivel de refrigerante bajo; se repuso la carga.")
            )
        ),
        OrdenTrabajo(
            id = "OT-1040",
            cliente = "Retail Moderna",
            local = "Tienda Costanera",
            direccion = "Av. Andrés Bello 2425, Providencia",
            equipo = "Climatización tienda",
            exterior = true,
            ubicacionEquipo = "Azotea",
            ultimaMantencion = "05/08/2026",
            tipo = "Correctiva",
            prioridad = Prioridad.ALTA,
            estado = EstadoOt.EN_EJECUCION,
            tecnico = "Javiera Rojas",
            fecha = "Hoy lun 26/10",
            clima = Clima.DESPEJADO,
            descripcion = "La tienda no alcanza la temperatura de confort.",
            slaHoras = 3
        ),
        OrdenTrabajo(
            id = "OT-1044",
            cliente = "Supermercado Cordillera",
            local = "Sucursal Maipú",
            direccion = "Av. Pajaritos 3400, Maipú",
            equipo = "Condensadora patio 1",
            exterior = true,
            ubicacionEquipo = "Patio exterior",
            ultimaMantencion = "20/06/2026",
            tipo = "Correctiva",
            prioridad = Prioridad.ALTA,
            estado = EstadoOt.CREADA,
            tecnico = null,
            fecha = "Mié 28/10",
            clima = Clima.LLUVIA,
            descripcion = "Ruido anormal en el ventilador de la condensadora.",
            slaHoras = 4
        ),
        OrdenTrabajo(
            id = "OT-1045",
            cliente = "Centro Comercial Los Alerces",
            local = "Torre Oriente",
            direccion = "Av. Apoquindo 4500, Las Condes",
            equipo = "Split sala técnica",
            exterior = false,
            ubicacionEquipo = "Sala técnica",
            ultimaMantencion = "12/09/2026",
            tipo = "Preventiva",
            prioridad = Prioridad.BAJA,
            estado = EstadoOt.PENDIENTE_ASIGNACION,
            tecnico = null,
            fecha = "Vie 30/10",
            clima = Clima.DESPEJADO,
            descripcion = "Mantención preventiva trimestral del split.",
            slaHoras = 6,
            observacion = "Rechazada por Matías Soto: conflicto de agenda."
        ),
        OrdenTrabajo(
            id = "OT-1038",
            cliente = "Supermercado Cordillera",
            local = "Sucursal Independencia",
            direccion = "Av. Independencia 1250, Independencia",
            equipo = "Cámara frigorífica 1",
            exterior = false,
            ubicacionEquipo = "Bodega",
            ultimaMantencion = "02/09/2026",
            tipo = "Correctiva",
            prioridad = Prioridad.ALTA,
            estado = EstadoOt.CERRADA,
            tecnico = "Javiera Rojas",
            fecha = "Vie 23/10",
            clima = Clima.DESPEJADO,
            descripcion = "La cámara no mantiene la temperatura.",
            slaHoras = 4,
            tiempoRealMin = 150,
            diagnostico = "Falla en el termostato de la cámara; la temperatura estaba sobre el rango.",
            actividades = listOf(
                "Reemplazo del termostato digital",
                "Calibración de la temperatura a 2 °C",
                "Prueba de funcionamiento por 30 minutos"
            ),
            materiales = listOf("Termostato digital × 1", "Cinta aislante × 1")
        ),
        OrdenTrabajo(
            id = "OT-1030",
            cliente = "Supermercado Cordillera",
            local = "Sucursal Independencia",
            direccion = "Av. Independencia 1250, Independencia",
            equipo = "Condensadora rooftop 1",
            exterior = true,
            ubicacionEquipo = "Azotea",
            ultimaMantencion = "15/07/2026",
            tipo = "Preventiva",
            prioridad = Prioridad.MEDIA,
            estado = EstadoOt.CERRADA,
            tecnico = "Matías Soto",
            fecha = "Lun 12/10",
            clima = Clima.DESPEJADO,
            descripcion = "Mantención preventiva semestral.",
            slaHoras = 4,
            tiempoRealMin = 95,
            diagnostico = "Equipo en buen estado; serpentín con suciedad leve.",
            actividades = listOf("Limpieza de serpentín", "Revisión de presiones"),
            materiales = listOf("Desengrasante × 1 L"),
            conformidad = Conformidad.APROBADA,
            fechaConformidad = "12/10/2026 17:20"
        ),
        OrdenTrabajo(
            id = "OT-1036",
            cliente = "Centro Comercial Los Alerces",
            local = "Torre Oriente",
            direccion = "Av. Apoquindo 4500, Las Condes",
            equipo = "Chiller central",
            exterior = true,
            ubicacionEquipo = "Patio técnico",
            ultimaMantencion = "30/07/2026",
            tipo = "Correctiva",
            prioridad = Prioridad.ALTA,
            estado = EstadoOt.CERRADA,
            tecnico = "Matías Soto",
            fecha = "Jue 22/10",
            clima = Clima.DESPEJADO,
            descripcion = "El chiller se detiene por alta presión.",
            slaHoras = 6,
            tiempoRealMin = 410,
            diagnostico = "Válvula de expansión obstruida; alta presión en el circuito.",
            actividades = listOf("Limpieza de la válvula de expansión", "Recarga de refrigerante", "Prueba de carga"),
            materiales = listOf("Refrigerante R-410A × 3 kg"),
            conformidad = Conformidad.APROBADA,
            fechaConformidad = "22/10/2026 19:05"
        ),
        OrdenTrabajo(
            id = "OT-1035",
            cliente = "Retail Moderna",
            local = "Tienda Costanera",
            direccion = "Av. Andrés Bello 2425, Providencia",
            equipo = "Climatización tienda",
            exterior = true,
            ubicacionEquipo = "Azotea",
            ultimaMantencion = "05/08/2026",
            tipo = "Preventiva",
            prioridad = Prioridad.MEDIA,
            estado = EstadoOt.CERRADA,
            tecnico = "Javiera Rojas",
            fecha = "Mié 21/10",
            clima = Clima.DESPEJADO,
            descripcion = "Mantención preventiva mensual.",
            slaHoras = 3,
            tiempoRealMin = 130,
            diagnostico = "Sin fallas; filtros con desgaste normal.",
            actividades = listOf("Cambio de filtros", "Revisión eléctrica"),
            materiales = listOf("Filtro G4 × 4"),
            conformidad = Conformidad.APROBADA,
            fechaConformidad = "21/10/2026 12:40"
        )
    )

    fun ordenPorId(id: String): OrdenTrabajo? = ordenes.firstOrNull { it.id == id }

    private fun actualizar(id: String, cambio: (OrdenTrabajo) -> OrdenTrabajo) {
        val i = ordenes.indexOfFirst { it.id == id }
        if (i >= 0) ordenes[i] = cambio(ordenes[i])
    }

    /** true si el tiempo real cumplió el SLA; null si la OT aún no tiene tiempo medido. */
    fun cumpleSla(ot: OrdenTrabajo): Boolean? {
        val real = ot.tiempoRealMin ?: return null
        return real <= ot.slaHoras * 60
    }

    // --- Acciones del técnico -----------------------------------------
    fun aceptar(id: String) {
        registrarCambioLocal()
        actualizar(id) { it.copy(estado = EstadoOt.ACEPTADA) }
    }

    fun rechazar(id: String, motivo: String) {
        registrarCambioLocal()
        actualizar(id) {
            it.copy(
                estado = EstadoOt.PENDIENTE_ASIGNACION,
                tecnico = null,
                observacion = "Rechazada por ${it.tecnico ?: "el técnico"}: $motivo"
            )
        }
    }

    fun marcarEnEjecucion(id: String) {
        actualizar(id) { it.copy(estado = EstadoOt.EN_EJECUCION) }
    }

    fun reportarImprevisto(id: String, motivo: String) {
        registrarCambioLocal()
        actualizar(id) {
            it.copy(estado = EstadoOt.PENDIENTE_IMPREVISTO, observacion = "Imprevisto: $motivo")
        }
    }

    fun cerrar(id: String, registro: RegistroEjecucion) {
        registrarCambioLocal()
        actualizar(id) {
            it.copy(
                estado = EstadoOt.CERRADA,
                tiempoRealMin = registro.tiempoAtencionMin,
                diagnostico = registro.diagnostico,
                actividades = registro.actividades.toList(),
                materiales = registro.materiales.toList(),
                conformidad = Conformidad.SIN_REVISAR,
                observacion = null
            )
        }
    }

    // --- Registro de la visita (uno por OT) ----------------------------
    private val registros = mutableMapOf<String, RegistroEjecucion>()

    fun registro(id: String): RegistroEjecucion = registros.getOrPut(id) { RegistroEjecucion() }

    // --- Acciones del planificador -------------------------------------
    fun crearOrden(
        cliente: Cliente,
        local: Local,
        equipo: Equipo,
        tipo: String,
        prioridad: Prioridad,
        dia: DiaAgenda,
        tecnico: String?,
        descripcion: String
    ): OrdenTrabajo {
        val ot = OrdenTrabajo(
            id = "OT-${siguienteNumero++}",
            cliente = cliente.nombre,
            local = local.nombre,
            direccion = local.direccion,
            equipo = equipo.nombre,
            exterior = equipo.exterior,
            ubicacionEquipo = equipo.ubicacion,
            ultimaMantencion = equipo.ultimaMantencion,
            tipo = tipo,
            prioridad = prioridad,
            estado = if (tecnico != null) EstadoOt.ASIGNADA else EstadoOt.CREADA,
            tecnico = tecnico,
            fecha = dia.etiqueta,
            clima = dia.clima,
            descripcion = descripcion,
            slaHoras = cliente.slaHoras
        )
        ordenes.add(0, ot)
        return ot
    }

    fun asignar(id: String, tecnico: String) {
        actualizar(id) { it.copy(tecnico = tecnico, estado = EstadoOt.ASIGNADA, observacion = null) }
    }

    fun reprogramar(id: String, dia: DiaAgenda) {
        actualizar(id) { it.copy(fecha = dia.etiqueta, clima = dia.clima) }
    }

    // --- Acciones del cliente -------------------------------------------
    fun solicitarMantencion(equipo: Equipo, descripcion: String): OrdenTrabajo {
        val cliente = clientes.firstOrNull { it.nombre == clienteActual }
        val local = cliente?.locales?.firstOrNull { it.nombre == localActual }
        val ot = OrdenTrabajo(
            id = "OT-${siguienteNumero++}",
            cliente = clienteActual,
            local = localActual,
            direccion = local?.direccion ?: "",
            equipo = equipo.nombre,
            exterior = equipo.exterior,
            ubicacionEquipo = equipo.ubicacion,
            ultimaMantencion = equipo.ultimaMantencion,
            tipo = "Correctiva",
            prioridad = Prioridad.MEDIA,
            estado = EstadoOt.CREADA,
            tecnico = null,
            fecha = "Por definir",
            clima = Clima.DESPEJADO,
            descripcion = descripcion,
            slaHoras = cliente?.slaHoras ?: 4,
            observacion = "Solicitada por el cliente"
        )
        ordenes.add(0, ot)
        return ot
    }

    fun aprobarTrabajo(id: String) {
        actualizar(id) {
            it.copy(conformidad = Conformidad.APROBADA, fechaConformidad = ahoraTexto())
        }
    }

    fun observarTrabajo(id: String, comentario: String) {
        actualizar(id) {
            it.copy(
                conformidad = Conformidad.OBSERVADA,
                fechaConformidad = ahoraTexto(),
                comentarioCliente = comentario
            )
        }
    }

    // --- Utilidades de fecha y hora --------------------------------------
    fun ahoraTexto(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    fun horaTexto(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    }
}
