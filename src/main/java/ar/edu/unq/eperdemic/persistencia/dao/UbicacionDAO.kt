package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Ubicacion

interface UbicacionDAO {
    fun guardar(ubicacion: Ubicacion)
    fun recuperar(nombreUbicacion: String) : Ubicacion?
    fun cantidadUbicaciones(): Long
}