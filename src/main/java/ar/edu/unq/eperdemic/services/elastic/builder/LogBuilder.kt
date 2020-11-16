package ar.edu.unq.eperdemic.services.elastic.builder

import ar.edu.unq.eperdemic.services.elastic.DetalleOrigen
import ar.edu.unq.eperdemic.services.elastic.Log
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import java.lang.Exception
import java.time.LocalDate

class LogBuilder {
    companion object {
        fun buildCreacionPatogenoLog(origen: Origen, ex: Exception): Log {
            return Log(TipoDeProceso.CreacionPatogeno,
                    "Error creando patogeno",
                    LocalDate.now(),
                    DetalleOrigen(origen, ex.javaClass.simpleName, ex.message))
        }

        fun buildMovimientoVectorLog(ex: Exception) : Log {
            return Log(TipoDeProceso.MovimientoVector,
            "Error en el proceso MovimientoVector",
                    LocalDate.now(),
                    DetalleOrigen(Origen.ProcessError, ex.javaClass.simpleName, ex.message))
        }

        fun buildContagioVectorLog(ex: Exception) : Log {
            return Log(TipoDeProceso.ContagioVector,
                    "Error en el proceso ContagioVector",
                    LocalDate.now(),
                    DetalleOrigen(Origen.ProcessError, ex.javaClass.simpleName, ex.message))
        }
    }
}