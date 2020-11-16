package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.modelo.exceptions.UbicacionMuyLejanaException
import ar.edu.unq.eperdemic.modelo.exceptions.UbicacionNoAlcanzableException
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.Neo4JUbicacionDAO
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import ar.edu.unq.eperdemic.services.elastic.builder.LogBuilder
import ar.edu.unq.eperdemic.services.utils.ObjectStructureUtils
import ar.edu.unq.eperdemic.services.utils.validateEntityDoesNotExists
import ar.edu.unq.eperdemic.services.utils.validateEntityExists

class UbicacionServiceImpl(val ubicacionDAO: UbicacionDAO) : UbicacionService {

    val vectorService = VectorServiceImpl(HibernateVectorDAO())
    val neo4jUbicacionDao = Neo4JUbicacionDAO()
    val mongoDao = MongoEventoDAO()
    val logService = LogServiceImpl(ElasticSearchApiConsumer())
    override fun mover(vectorId: Int, nombreUbicacion: String) {
        val vector = vectorService.recuperarVector(vectorId)
        val destino = recuperarUbicacion(nombreUbicacion)
       TransactionRunner.runTrx {
           if (vector.puedeMoverse(destino) &&
                   esCercana(vector.nombreDeLocacionActual, nombreUbicacion) &&
                   puedeAtravesar(vector, nombreUbicacion)) {
                moverse(vector, nombreUbicacion)
           }
        }
    }

    private fun moverse(vector: Vector, destino: String) {
        vector.moverse(destino)
        vectorService.actualizarVector(vector)
        mongoDao.logearEvento(Evento.buildEventoArribo(vector, "El vector id ${vector.id} viajó a $destino"))
        contagiarZona(vector, destino)
    }

    private fun esCercana(origen: String, destino: String) : Boolean {
        if (neo4jUbicacionDao.esUbicacionMuyLejana(origen, destino)) {
            val ex = UbicacionMuyLejanaException("La ubicacion a la que intenta moverse no esta conectada")
            val log = LogBuilder.buildMovimientoVectorLog(ex)
            logService.postLog(log)
            throw ex
        }
        return true
    }

    private fun puedeAtravesar(vector: Vector, destino: String) : Boolean {
        if (neo4jUbicacionDao.caminosConectados(vector.nombreDeLocacionActual, destino).all {
                    !it.puedeSerAtravesadoPor(vector.tipo)
                }) {
            val ex = UbicacionNoAlcanzableException("La ubicacion a la que intenta moverse no tiene un camino alcanzable")
            val log = LogBuilder.buildMovimientoVectorLog(ex)
            logService.postLog(log)
            throw ex
        }
        return true
    }

    private fun contagiarZona(vector: Vector?, locacion: String?) {
        var vectores: List<Vector> = mutableListOf()

        if (vector != null && vector.estaInfectado()) {
            TransactionRunner.runTrx {
                vectores = vectorService.getVectoresByLocacion(locacion).toMutableList()
                (vectores as MutableList<Vector>).removeIf { v -> v.id == vector.id }
            }
            vectorService.contagiar(vector, vectores)
        }
    }

    override fun expandir(nombreUbicacion: String) {
        val vectorInfectado = vectorService.getVectorRandomEnLocacion(nombreUbicacion)
        contagiarZona(vectorInfectado, nombreUbicacion)
    }

    override fun crearUbicacion(nombreUbicacion: String): Ubicacion {
        val ubicacion = Ubicacion(nombreUbicacion)
        ObjectStructureUtils.checkEmptyAttributes(ubicacion,TipoDeProceso.CreacionUbicacion)
        TransactionRunner.runTrx {
            validateEntityDoesNotExists<Ubicacion>(nombreUbicacion, TipoDeProceso.CreacionUbicacion)
            ubicacionDAO.guardar(ubicacion)
            neo4jUbicacionDao.crearUbicacion(ubicacion)
        }
        return ubicacion
    }

    override fun recuperarUbicacion(nombreUbicacion: String): Ubicacion? {
        return TransactionRunner.runTrx {
            validateEntityExists<Ubicacion>(nombreUbicacion, TipoDeProceso.RecuperarUbicacion)
            ubicacionDAO.recuperar(nombreUbicacion)
        }
    }

    override fun conectar(nombreUbicacionOrigen: String, nombreUbicacionDestino: String, tipoDeCamino: TipoCaminoEnum) {
        TransactionRunner.runTrx {
            neo4jUbicacionDao.conectar(nombreUbicacionOrigen, nombreUbicacionDestino, tipoDeCamino)
        }
    }

    override fun conectados(nombreUbicacion: String): List<Ubicacion> {
        return TransactionRunner.runTrx {
            neo4jUbicacionDao.conectados(nombreUbicacion)
        }
    }

    override fun capacidadDeExpansion(vectorId: Long, movimientos: Int): Int {
        val vector = vectorService.recuperarVector(vectorId.toInt())
        return TransactionRunner.runTrx {
            val ubicacion = vector.nombreDeLocacionActual
            neo4jUbicacionDao.capacidadDeExpansion(ubicacion, movimientos, vector.tipo)
        }
    }

    override fun caminoMasCorto(tipoDeVector: TipoDeVectorEnum, nombreUbicacionOrigen: String, nombreUbicacionDestino: String): List<String> {
        var caminoMasCorto = listOf<String>()
        if (nombreUbicacionOrigen != nombreUbicacionDestino) {
            TransactionRunner.runTrx {
                caminoMasCorto = neo4jUbicacionDao.caminoMasCorto(tipoDeVector, nombreUbicacionOrigen, nombreUbicacionDestino)
            }
            if (caminoMasCorto.isEmpty()) {
                val ex = UbicacionNoAlcanzableException("La ubicacion a la que intenta moverse no tiene un camino alcanzable")
                val log = LogBuilder.buildMovimientoVectorLog(ex)
                logService.postLog(log)
                throw ex
            }
        }
        return caminoMasCorto
    }

    override fun moverMasCorto(vectorId: Int, nombreUbicacion: String) {
        TransactionRunner.runTrx {
            val vector = vectorService.recuperarVector(vectorId)
            val caminoMasCorto = caminoMasCorto(vector.tipo, vector.nombreDeLocacionActual, nombreUbicacion).drop(1)
            caminoMasCorto.forEach{moverse(vector, it)}
        }
    }

    fun cantidadUbicaciones(): Long {
        return TransactionRunner.runTrx {
            ubicacionDAO.cantidadUbicaciones()
        }
    }
}
