package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.services.LogService
import ar.edu.unq.eperdemic.services.elastic.Log
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import java.time.LocalDate

class LogServiceImpl(val elastic: ElasticSearchApiConsumer) : LogService {
    override fun findAll(): List<Log> {
        return elastic.findAll()
    }

    override fun findByProceso(proceso: TipoDeProceso): List<Log> {
        return elastic.findByStandardQuery(proceso.name, "proceso")
    }

    override fun findByOrigen(origen: Origen): List<Log> {
        return elastic.findByStandardQuery(origen.name, "origen.tipo")
    }

    override fun findByFecha(fecha: LocalDate): List<Log> {
        return elastic.findByDateQuery(fecha)
    }

    override fun postLog(log: Log) {
        elastic.doLog(log)
    }


}