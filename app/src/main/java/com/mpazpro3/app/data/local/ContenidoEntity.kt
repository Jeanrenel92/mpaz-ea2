package com.mpazpro3.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion

@Entity("contenidos")
data class ContenidoEntity(
    @PrimaryKey val id: String,
    val nivel: String,
    val asignatura: String, // LENGUAJE, MATEMATICA, CIENCIAS_NATURALES, HISTORIA
    val unidad: String,
    val objetivoAprendizaje: String,
    val codigoOA: String?,
    val ejeCurricular: String,
    val recursoReferencia: String,
    val estado: String, // PENDIENTE, APROBADO, RECHAZADO
    val observacionUtp: String
)

// Convierte modelo de la app a fila para la tabla
fun Contenido.aEntity() = ContenidoEntity(
    id = id,
    nivel = nivel,
    asignatura = asignatura.name,
    unidad = unidad,
    objetivoAprendizaje = objetivoAprendizaje,
    codigoOA = codigoOA,
    ejeCurricular = ejeCurricular,
    recursoReferencia = recursoReferencia,
    estado = estado.name,
    observacionUtp = observacionUtp
)

// Convierte una fila de la tabla al modelo que usan las pantallas
fun ContenidoEntity.aModelo() = Contenido(
    id = id,
    nivel = nivel,
    asignatura = Asignatura.valueOf(asignatura),
    unidad = unidad,
    objetivoAprendizaje = objetivoAprendizaje,
    codigoOA = codigoOA,
    ejeCurricular = ejeCurricular,
    recursoReferencia = recursoReferencia,
    estado = EstadoAprobacion.valueOf(estado),
    observacionUtp = observacionUtp
)