package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Evento
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateEspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.elastic.DetalleOrigen
import ar.edu.unq.eperdemic.services.elastic.Log
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import ar.edu.unq.eperdemic.services.elastic.builder.LogBuilder
import ar.edu.unq.eperdemic.services.exceptions.*
import ar.edu.unq.eperdemic.services.utils.ObjectStructureUtils
import ar.edu.unq.eperdemic.services.utils.validateEntityExists
import java.time.LocalDate
import java.util.*


class PatogenoServiceImpl(val patogenoDAO: PatogenoDAO) : PatogenoService {

    val especieDAO: EspecieDAO = HibernateEspecieDAO()
    val eventoDao = MongoEventoDAO()
    val logService = LogServiceImpl(ElasticSearchApiConsumer())

    override fun crearPatogeno(patogeno: Patogeno): Int {
        ObjectStructureUtils.checkEmptyAttributes(patogeno, TipoDeProceso.CreacionPatogeno)
        if (existePatogenoConTipo(patogeno.tipo)) {
            val ex = EntityAlreadyExistsException("La entidad ${Patogeno::class.simpleName} con tipo ${patogeno.tipo} ya existe")
            logService.postLog(LogBuilder.buildCreacionPatogenoLog(Origen.DataError, ex))
            throw ex
        }
        return TransactionRunner.runTrx {
                patogenoDAO.crear(patogeno)
        }
    }

    override fun recuperarPatogeno(id: Int): Patogeno {
        return TransactionRunner.runTrx {
            patogenoDAO.recuperar(id)
        }?: throw EntityNotFoundException("No se encontro un patogeno con el id ${id}")
    }

    override fun recuperarATodosLosPatogenos(): List<Patogeno> {
        return TransactionRunner.runTrx {
            patogenoDAO.recuperarATodos()
        }
    }

    override fun agregarEspecie(id: Int, nombreEspecie: String, paisDeOrigen: String): Especie {
        var especieCreada: Especie? = null
        TransactionRunner.runTrx {
            validateEntityExists<Patogeno>(id, TipoDeProceso.CreacionEspecie)
            var patogeno = patogenoDAO.recuperar(id)
            especieCreada = patogeno!!.crearEspecie(nombreEspecie,paisDeOrigen)
            especieDAO.guardar(especieCreada!!)
        }
        eventoDao.logearEvento(Evento.buildEventoMutacion(especieCreada!!,
                "Se creo la especie ${especieCreada!!.nombre}"))
        return especieCreada!!
    }

    override fun actualizarPatogeno(patogeno: Patogeno) {
        TransactionRunner.runTrx {
            patogenoDAO.actualizar(patogeno)
        }
    }

    override fun cantidadDeInfectados(especieId: Int): Int {
       return TransactionRunner.runTrx {
            especieDAO.cantidadDeInfectados(especieId)
        }
    }

    override fun esPandemia(especieId: Int): Boolean {
        return TransactionRunner.runTrx {
            especieDAO.esPandemia(especieId)
        }
    }

    override fun recuperarEspecie(id: Int): Especie {
        return TransactionRunner.runTrx {
            validateEntityExists<Especie>(id, TipoDeProceso.RecuperarEspecie)
            especieDAO.recuperar(id)
        }
    }

    override fun actualizarEspecie(especie: Especie) {
        TransactionRunner.runTrx {
            especieDAO.actualizar(especie)
        }
    }

    private fun existePatogenoConTipo(tipo: String): Boolean {
        return TransactionRunner.runTrx {
            patogenoDAO.existePatogenoConTipo(tipo)
        }
    }
}