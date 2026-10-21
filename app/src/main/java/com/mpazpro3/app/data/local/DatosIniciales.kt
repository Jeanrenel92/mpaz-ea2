package com.mpazpro3.app.data.local

import com.mpazpro3.app.data.MockData

// Si la tabla esta vacia (primera vez), carga los contenidos de ejemplo
suspend fun cargarDatosInicialesSiVacia(dao: ContenidoDao){
    if(dao.contarTodos()==0){
        dao.insertarTodos(MockData.contenidos.map { it.aEntity() })
    }
}