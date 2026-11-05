package com.example.acis.repository

import com.example.acis.data.DatosDemo
import com.example.acis.data.OrdenTrabajo

/**
 * Repository (MVVM, opcional según Guía 7): capa que obtiene y entrega datos.
 * La UI/ViewModel NO accede directo a la fuente de datos; pasa por aquí.
 *
 * Hoy usa [DatosDemo] (en memoria). Cuando conectes Room, este repositorio
 * leerá/escribirá desde la base de datos local en vez de (o además de) DatosDemo.
 */
class AcisRepository {
    fun ordenesDelTecnico(nombre: String): List<OrdenTrabajo> =
        DatosDemo.ordenes.filter { it.tecnico == nombre }

    fun ordenPorId(id: String): OrdenTrabajo? = DatosDemo.ordenPorId(id)

    fun todasLasOrdenes(): List<OrdenTrabajo> = DatosDemo.ordenes.toList()
}
