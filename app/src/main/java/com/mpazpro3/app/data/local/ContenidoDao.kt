package com.mpazpro3.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContenidoDao{

    @Query("SELECT * FROM contenidos ORDER BY id")
    fun obtenerTodos(): Flow<List<ContenidoEntity>>

    @Query("UPDATE contenidos SET estado = :estado, observacionUtp = :observacion WHERE id = :id")
    suspend fun actualizarEstado(id: String, estado: String, observacion: String)

    @Query("SELECT COUNT(*) FROM contenidos WHERE asignatura = :asignatura AND estado = 'APROBADO' AND id != :idExcluido")
    suspend fun contarAprobadosPorAsignatura(asignatura: String, idExcluido: String): Int

    @Query("SELECT COUNT(*) FROM contenidos")
    suspend fun contarTodos(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(contenidos: List<ContenidoEntity>)
}