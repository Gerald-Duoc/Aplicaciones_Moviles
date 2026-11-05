package com.example.acis.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * EJEMPLO de entidad Room (persistencia local).
 * Una @Entity se convierte en una tabla de la base de datos.
 * Adáptala a tus modelos reales (p. ej. OrdenTrabajo) cuando integres Room.
 */
@Entity(tableName = "ot_local")
data class OtEntity(
    @PrimaryKey val id: String,
    val cliente: String,
    val estado: String,
    val tecnico: String?
)
