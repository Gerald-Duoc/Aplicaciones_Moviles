package com.example.acis.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.acis.model.OtEntity

/**
 * Base de datos Room (persistencia local). Punto único de acceso a la BD.
 * Aún NO está conectada a las pantallas: es la base para cumplir el requisito
 * de almacenamiento local de la rúbrica cuando decidan integrarla.
 */
@Database(entities = [OtEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun otDao(): OtDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "acis.db"
                ).build().also { instancia = it }
            }
    }
}
