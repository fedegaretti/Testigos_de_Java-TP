package ar.edu.unq.eperdemic.utils.elastic

import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.exceptions.UbicacionMuyLejanaException
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateMutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import ar.edu.unq.eperdemic.services.exceptions.EmptyPropertyException
import ar.edu.unq.eperdemic.services.exceptions.EntityAlreadyExistsException
import ar.edu.unq.eperdemic.services.impl.MutacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import ar.edu.unq.eperdemic.utils.neo4j.DataServiceNeo4j
import org.junit.jupiter.api.assertThrows

fun main() = ElasticSearchDataService().crearSetDeDatosIniciales()

class ElasticSearchDataService {
    private val hibernateDataService = DataServiceHibernate()
    private val patogenoService = PatogenoServiceImpl(HibernatePatogenoDAO())
    private val mutacionService = MutacionServiceImpl(HibernateMutacionDAO())
    private val ubicacionService = UbicacionServiceImpl(HibernateUbicacionDAO())
    private val vectorService = VectorServiceImpl(HibernateVectorDAO())
    private val elastic = ElasticSearchApiConsumer()
    private val neo4j = DataServiceNeo4j()

    fun crearSetDeDatosIniciales() {
        elastic.createIndex()
        hibernateDataService.crearSetDeDatosIniciales()
        neo4j.crearSetDeDatosIniciales()
        val patogeno1 = Patogeno("bacteria")
        val patogeno2 = Patogeno("Virus")
        val patogeno3 = Patogeno()
        val mutacion = Mutacion()
        val vector = Vector()
        // workaround: meto los assert throw para poder ejecutar metodos que fallen sin que se corte la ejecucion
        assertThrows<EntityAlreadyExistsException> { patogenoService.crearPatogeno(patogeno1) }
        assertThrows<EntityAlreadyExistsException> { patogenoService.crearPatogeno(patogeno2) }
        assertThrows<EntityAlreadyExistsException> { ubicacionService.crearUbicacion("Buenos Aires") }
        assertThrows<EmptyPropertyException> { patogenoService.crearPatogeno(patogeno3) }
        assertThrows<EmptyPropertyException> { mutacionService.crearMutacion(mutacion) }
        assertThrows<EmptyPropertyException> { ubicacionService.crearUbicacion("") }
        assertThrows<EmptyPropertyException> { vectorService.crearVector(vector) }
        assertThrows<UbicacionMuyLejanaException> {ubicacionService.mover(1,"Lanus")}
    }

    fun eliminarTodo() {
        elastic.clear()
        neo4j.eliminarTodo()
        hibernateDataService.eliminarTodo()
    }
}