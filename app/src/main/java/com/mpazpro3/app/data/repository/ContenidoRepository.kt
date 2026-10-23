package com.mpazpro3.app.data.repository

import com.mpazpro3.app.data.local.ContenidoDao
import com.mpazpro3.app.data.local.aModelo
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Acceso a los contenidos guardados en Room. Convierte ContenidoEntity (base de datos)
 * en Contenido (modelo que usan las pantallas).
 */
class ContenidoRepository(private val dao: ContenidoDao) {

    /** Todos los contenidos; se vuelve a emitir cada vez que cambia la tabla. */
    val contenidos: Flow<List<Contenido>> =
        dao.obtenerTodos().map { lista -> lista.map { it.aModelo() } }

    /** RF-02: contenidos filtrados por asignatura y/o estado (null = todos). */
    fun filtrar(asignatura: Asignatura?, estado: EstadoAprobacion?): Flow<List<Contenido>> =
        contenidos.map { lista ->
            lista.filter { contenido ->
                (asignatura == null || contenido.asignatura == asignatura) &&
                        (estado == null || contenido.estado == estado)
            }
        }

    /** RF-01 / RF-02: guarda en Room el estado de aprobación y la observación de UTP. */
    suspend fun actualizarEstado(id: String, estado: EstadoAprobacion, observacion: String) =
        dao.actualizarEstado(id, estado.name, observacion)

    /** RN-04: cuántas unidades APROBADAS tiene la asignatura, sin contar el contenido indicado. */
    suspend fun contarAprobadosPorAsignatura(asignatura: Asignatura, idExcluido: String): Int =
        dao.contarAprobadosPorAsignatura(asignatura.name, idExcluido)
}
