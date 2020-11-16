package ar.edu.unq.eperdemic.services.utils

import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDAO
import ar.edu.unq.eperdemic.services.elastic.DetalleOrigen
import ar.edu.unq.eperdemic.services.elastic.Log
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import ar.edu.unq.eperdemic.services.exceptions.EntityAlreadyExistsException
import ar.edu.unq.eperdemic.services.exceptions.EntityNotFoundException
import ar.edu.unq.eperdemic.services.impl.LogServiceImpl
import java.time.LocalDate
import java.util.*

inline fun <reified T> validateEntityExists(id: String, proceso: TipoDeProceso) {
    val dao = HibernateDAO(T::class.java)
    if (dao.recuperar(id) == null) {
        processEntityExistsError<T>(id, proceso)
    }
}

inline fun <reified T> validateEntityExists(id: Int, proceso: TipoDeProceso) {
    val dao = HibernateDAO(T::class.java)
    if (dao.recuperar(id) == null) {
        processEntityExistsError<T>(id.toString(), proceso)
    }
}

inline fun <reified T> validateEntityDoesNotExists(id: String, proceso: TipoDeProceso)  {
    val dao = HibernateDAO(T::class.java)
    if (dao.recuperar(id) != null) {
        processEntityNotExistsError<T>(id, proceso)
    }
}

inline fun <reified T>  processEntityExistsError(id: String, proceso: TipoDeProceso) {
    val logService = LogServiceImpl(ElasticSearchApiConsumer())
    val ex = EntityNotFoundException("La entidad ${T::class.simpleName} con id ${id} no existe")
    val log = Log(proceso, "Error en el proceso $proceso",
            LocalDate.now(),
            DetalleOrigen(Origen.DataError, ex.javaClass.simpleName, ex.message))
    logService.postLog(log)
    throw ex
}

inline fun <reified T>  processEntityNotExistsError(id: String, proceso: TipoDeProceso) {
    val logService = LogServiceImpl(ElasticSearchApiConsumer())
    val ex = EntityAlreadyExistsException("La entidad ${T::class.simpleName} con id ${id} ya existe")
    val log = Log(proceso, "Error en el proceso $proceso",
            LocalDate.now(),
            DetalleOrigen(Origen.DataError, ex.javaClass.simpleName, ex.message))
    logService.postLog(log)
    throw ex
}