package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Evento
import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.MutacionService
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.utils.ObjectStructureUtils
import ar.edu.unq.eperdemic.services.utils.validateEntityExists

class MutacionServiceImpl(val mutacionDAO: MutacionDAO) : MutacionService {
    val eventoDao = MongoEventoDAO()

    val patogenoService = PatogenoServiceImpl(HibernatePatogenoDAO())
    override fun mutar(especieId: Int, mutacionId: Int) {
        TransactionRunner.runTrx {
            validateEntityExists<Especie>(especieId, TipoDeProceso.MutacionEspecie)
            validateEntityExists<Mutacion>(mutacionId, TipoDeProceso.MutacionEspecie)
        }
        val especie = patogenoService.recuperarEspecie(especieId)
        especie.mutar(recuperarMutacion(mutacionId))
        patogenoService.actualizarEspecie(especie)
        eventoDao.logearEvento(Evento.buildEventoMutacion(especie, "Se muto la especie ${especie.nombre} "))
    }

    override fun crearMutacion(mutacion: Mutacion): Mutacion {
        ObjectStructureUtils.checkEmptyAttributes(mutacion, TipoDeProceso.CreacionMutacion)
        TransactionRunner.runTrx {
            mutacionDAO.guardar(mutacion)
        }
        return mutacion
    }

    override fun recuperarMutacion(mutacionId: Int): Mutacion {
        return TransactionRunner.runTrx {
            validateEntityExists<Mutacion>(mutacionId, TipoDeProceso.RecuperarMutacion)
            mutacionDAO.recuperar(mutacionId)
        }
    }
}