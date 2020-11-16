package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Mutacion

interface MutacionDAO {
    fun guardar(mutacion: Mutacion)
    fun recuperar(id: Int): Mutacion
}
