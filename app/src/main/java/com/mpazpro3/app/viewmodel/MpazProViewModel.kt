package com.mpazpro3.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mpazpro3.app.data.MockData
import com.mpazpro3.app.data.local.AppDatabase
import com.mpazpro3.app.data.local.cargarDatosInicialesSiVacia
import com.mpazpro3.app.data.repository.ContenidoRepository
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * ESTO ES UN ESQUELETO: las pantallas ya llaman a todas estas funciones, pero la lógica de
 * negocio del caso está vacía a propósito y marcada con TODO(equipo), citando el módulo de
 * Requerimientos_Seccion007 al que corresponde. Mientras no se implemente, las listas se
 * muestran sin filtrar y las acciones (aprobar, asignar, responder, validar) no cambian nada.
 */
class MpazProViewModel(application: Application) : AndroidViewModel(application) {

    // Room + Repository de contenidos. Debe declararse antes de las propiedades que lo usan.
    private val contenidoDao = AppDatabase.obtener(application).contenidoDao()
    private val contenidoRepository = ContenidoRepository(contenidoDao)

    init {
        // Primera ejecución: carga los contenidos de ejemplo en Room si la tabla está vacía.
        viewModelScope.launch { cargarDatosInicialesSiVacia(contenidoDao) }
    }

    // =====================================================================
    // Autenticación
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /**
     * TODO(equipo): validar usuario y clave contra MockData.usuarios. Si son correctos, dejar
     * _usuarioActivo con ese usuario y devolver true (AppNavigation ya lleva a cada perfil a su
     * pantalla). Si no, dejar _errorLogin con un mensaje y devolver false.
     * Pendiente con el CITT: cómo se autentica el estudiante.
     */
    fun login(usuario: String, clave: String): Boolean {
        _errorLogin.value = "Login sin implementar — ver TODO(equipo) en MpazProViewModel.login()."
        return false
    }

    /**
     * SOLO PARA DESARROLLO: entra con el usuario de prueba del perfil indicado, sin validar
     * credenciales. TODO(equipo): eliminar esta función y los botones "Modo desarrollo".
     */
    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value = MockData.usuarios.first { it.perfil == perfil }
        _errorLogin.value = null
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _contenidoSeleccionadoId.value = null
        _estudianteSeleccionadoId.value = null
    }

    // =====================================================================
    // Datos compartidos y selección (no es lógica de negocio)
    // =====================================================================

    val contenidos: StateFlow<List<Contenido>> = contenidoRepository.contenidos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _recursos = MutableStateFlow(MockData.recursos)
    private val _actividades = MutableStateFlow(MockData.actividades)

    private val _asignaciones = MutableStateFlow(MockData.asignaciones)
    val asignaciones: StateFlow<List<Asignacion>> = _asignaciones.asStateFlow()

    private val _estudiantes = MutableStateFlow(MockData.estudiantes)
    val estudiantes: StateFlow<List<Estudiante>> = _estudiantes.asStateFlow()

    private val _seguimientos = MutableStateFlow(MockData.seguimientos)
    val seguimientos: StateFlow<List<SeguimientoAvance>> = _seguimientos.asStateFlow()

    private val _informes = MutableStateFlow<List<InformeDocente>>(emptyList())
    val informes: StateFlow<List<InformeDocente>> = _informes.asStateFlow()

    private val _contenidoSeleccionadoId = MutableStateFlow<String?>(null)
    val contenidoSeleccionadoId: StateFlow<String?> = _contenidoSeleccionadoId.asStateFlow()

    private val _estudianteSeleccionadoId = MutableStateFlow<String?>(null)
    val estudianteSeleccionadoId: StateFlow<String?> = _estudianteSeleccionadoId.asStateFlow()

    fun seleccionarContenido(id: String?) {
        _contenidoSeleccionadoId.value = id
    }

    fun seleccionarEstudiante(id: String?) {
        _estudianteSeleccionadoId.value = id
    }

    fun contenidoPorId(id: String?): Contenido? = contenidos.value.find { it.id == id }

    fun estudiantePorId(id: String?): Estudiante? = _estudiantes.value.find { it.id == id }

    fun recursosDe(contenidoId: String?): List<RecursoDidactico> =
        _recursos.value.filter { it.contenidoId == contenidoId }

    fun actividadPorId(id: String): Actividad? = _actividades.value.find { it.id == id }

    // =====================================================================
    // Módulo "Autorización de contenidos (UTP)"
    // =====================================================================

    /**
     * RF-02: contenidos filtrados por asignatura y por estado (null = todos). Se vuelve a
     * emitir cuando cambian los datos en Room.
     */
    fun contenidosFiltrados(asignatura: Asignatura?, estado: EstadoAprobacion?): Flow<List<Contenido>> =
        contenidoRepository.filtrar(asignatura, estado)

    /** Error que se muestra debajo del campo de observación (null = sin error). */
    private val _errorObservacion = MutableStateFlow<String?>(null)
    val errorObservacion: StateFlow<String?> = _errorObservacion.asStateFlow()

    /** Mensaje general para UTP: éxito o error de una regla de negocio (null = nada). */
    private val _mensajeUtp = MutableStateFlow<String?>(null)
    val mensajeUtp: StateFlow<String?> = _mensajeUtp.asStateFlow()

    /** Al escribir en el campo se limpia su error. */
    fun limpiarErrorObservacion() {
        _errorObservacion.value = null
    }

    /** Al abrir otro contenido se limpian los mensajes anteriores. */
    fun limpiarMensajesUtp() {
        _errorObservacion.value = null
        _mensajeUtp.value = null
    }

    /**
     * RF-01 / RF-02: cambia el estado del contenido a APROBADO y guarda la observación en Room.
     * La pantalla se actualiza sola porque `contenidos` viene de un Flow del DAO.
     */
    fun aprobarContenido(contenidoId: String, observacion: String) {
        val contenido = contenidoPorId(contenidoId) ?: return
        viewModelScope.launch {
            // RN-04: máximo una unidad aprobada por asignatura
            val otrosAprobados = contenidoRepository.contarAprobadosPorAsignatura(contenido.asignatura, contenidoId)
            if (otrosAprobados > 0) {
                _mensajeUtp.value = "Ya existe una unidad aprobada para esta asignatura"
                return@launch
            }
            contenidoRepository.actualizarEstado(contenidoId, EstadoAprobacion.APROBADO, observacion)
            _errorObservacion.value = null
            _mensajeUtp.value = "Contenido aprobado"
        }
    }

    /** RF-01: el rechazo exige motivo. Devuelve true si se puede continuar. */
    fun validarRechazo(observacion: String): Boolean {
        if (observacion.isBlank()) {
            _errorObservacion.value = "Indica el motivo del rechazo"
            return false
        }
        _errorObservacion.value = null
        return true
    }

    /** RF-01 / RF-02: igual que aprobarContenido(), pero con estado RECHAZADO. */
    fun rechazarContenido(contenidoId: String, observacion: String) {
        if (!validarRechazo(observacion)) return
        viewModelScope.launch {
            contenidoRepository.actualizarEstado(contenidoId, EstadoAprobacion.RECHAZADO, observacion)
            _mensajeUtp.value = "Contenido rechazado"
        }
    }

    // =====================================================================
    // Módulo "Planificación docente"
    // =====================================================================

    /**
     * TODO(equipo): devolver SOLO los contenidos APROBADOS por UTP (regla de negocio: un
     * contenido no autorizado no puede asignarse). Hoy devuelve todos.
     */
    fun contenidosDisponiblesParaAsignar(): List<Contenido> = contenidos.value

    /**
     * TODO(equipo): crear las asignaciones del curso para los contenidos elegidos, con el máximo
     * de intentos de la evaluación final. RF: "seleccionar los OA disponibles", "asignar
     * contenidos y desafíos a su curso", "definir el número máximo de intentos". Validar que
     * todos los contenidos estén aprobados. Hoy no hace nada.
     */
    fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int) {
        // Sin implementar todavía.
    }

    /** TODO(equipo): devolver solo las asignaciones del curso indicado. Hoy devuelve todas. */
    fun asignacionesDelCurso(curso: String): List<Asignacion> = _asignaciones.value

    /**
     * TODO(equipo): calcular el avance del estudiante (0.0 a 1.0) a partir de sus
     * SeguimientoAvance. RF: "revisar los resultados de sus estudiantes". Hoy devuelve 0.
     */
    fun progresoGeneral(estudianteId: String): Float = 0f

    /**
     * TODO(equipo): exportar los resultados del curso (por ejemplo a un archivo CSV o PDF que
     * se pueda compartir). RF: "revisar, descargar y validar los resultados". Hoy no hace nada.
     */
    fun descargarResultados(curso: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Informe pedagógico"
    // =====================================================================

    fun informeDe(estudianteId: String?): InformeDocente? = _informes.value.find { it.estudianteId == estudianteId }

    /**
     * TODO(equipo): generar el informe del estudiante (rendimiento, fortalezas y aspectos a
     * reforzar) a partir de sus respuestas, en estado BORRADOR. Restricciones del caso: sin
     * diagnósticos ni consecuencias académicas automáticas. Hoy no hace nada.
     */
    fun generarInforme(estudianteId: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): pasar el informe de BORRADOR a VALIDADO. RF: "el informe debe quedar
     * sujeto a validación docente antes de considerarse definitivo". Hoy no hace nada.
     */
    fun validarInforme(informeId: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Experiencia del estudiante"
    // =====================================================================

    /**
     * TODO(equipo): devolver solo los contenidos asignados al curso del estudiante. Hoy devuelve
     * todos. Regla: el estudiante no puede modificar contenidos ni ver el informe pedagógico.
     */
    fun contenidosDelEstudiante(usuario: Usuario?): List<Contenido> = contenidos.value

    /** TODO(equipo): avance del estudiante en el contenido (0.0 a 1.0). Hoy devuelve 0. */
    fun progreso(estudianteId: String, contenidoId: String): Float = 0f

    /**
     * TODO(equipo): guardar que el estudiante terminó de revisar el material. RF: "conservar el
     * progreso del estudiante y confirmar la finalización de cada actividad". Hoy no hace nada.
     */
    fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): devolver los desafíos del contenido (sin la evaluación final), ordenados
     * por dificultad progresiva. Hoy devuelve TODAS las actividades, sin filtrar ni ordenar.
     */
    fun desafiosDe(contenidoId: String?): List<Actividad> = _actividades.value

    /**
     * TODO(equipo): registrar la respuesta en el seguimiento del estudiante y devolver si fue
     * correcta (true/false) para mostrar la retroalimentación. Hoy devuelve null.
     */
    fun responderDesafio(estudianteId: String, actividad: Actividad, alternativa: Int): Boolean? = null

    /** TODO(equipo): devolver solo las preguntas de la evaluación final del contenido. Hoy devuelve todas. */
    fun evaluacionFinalDe(contenidoId: String?): List<Actividad> = _actividades.value

    /**
     * TODO(equipo): intentos que le quedan al estudiante en la evaluación final (máximo definido
     * por el docente menos los usados). Regla de negocio: el máximo debe respetarse
     * estrictamente. Hoy devuelve null (sin calcular).
     */
    fun intentosRestantes(estudianteId: String, contenidoId: String): Int? = null

    /**
     * TODO(equipo): registrar las respuestas de la evaluación final como un nuevo intento
     * (solo si quedan intentos) y actualizar el seguimiento. Hoy no hace nada.
     * @param respuestas mapa idActividad -> índice de la alternativa elegida.
     */
    fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): devolver las respuestas del estudiante en el contenido, para que revise
     * cuáles fueron correctas e incorrectas. RF: "revisar cuáles de sus respuestas fueron
     * correctas o incorrectas". Hoy devuelve una lista vacía.
     */
    fun respuestasDe(estudianteId: String, contenidoId: String): List<RespuestaEstudiante> = emptyList()
}
