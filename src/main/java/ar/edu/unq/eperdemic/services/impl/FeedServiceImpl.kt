package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Evento
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.FeedService

class FeedServiceImpl : FeedService {

    private val eventoDAO = MongoEventoDAO()
    private val ubicacionService = UbicacionServiceImpl(HibernateUbicacionDAO())

    override fun feedPatogeno(tipoDePatogeno: String): List<Evento> {
        return eventoDAO.getFeedPatogeno(tipoDePatogeno)
    }

    override fun feedVector(vectorId: Long): List<Evento> {
        return eventoDAO.getFeedVector(vectorId)
    }

    override fun feedUbicacion(nombreDeUbicacion: String): List<Evento> {
        val nombreUbicacionesLindantes = ubicacionService.conectados(nombreDeUbicacion).map { it.nombreUbicacion }.toMutableList()
        nombreUbicacionesLindantes.add(nombreDeUbicacion)
        return eventoDAO.getFeedUbicacion(nombreDeUbicacion, nombreUbicacionesLindantes)
    }
}