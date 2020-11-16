package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.services.elastic.Log
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import java.time.LocalDate

interface LogService {
    fun findAll() : List<Log>
    fun findByProceso(proceso: TipoDeProceso) : List<Log>
    fun findByOrigen(origen: Origen) : List<Log>
    fun findByFecha(fecha: LocalDate) : List<Log>
    fun postLog(log: Log)
}