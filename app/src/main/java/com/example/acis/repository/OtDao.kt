package com.example.acis.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acis.model.OtEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): define las operaciones sobre la tabla.
 * Room genera la implementación en compilación (por eso usamos KSP).
 */
@Dao
interface OtDao {
    @Query("SELECT * FROM ot_local ORDER BY id DESC")
    fun observarTodas(): Flow<List<OtEntity>>

    @Query("SELECT * FROM ot_local WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): OtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(todas: List<OtEntity>)
}
