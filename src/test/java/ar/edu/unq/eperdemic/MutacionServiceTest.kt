package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.modelo.AtributoEnum
import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateMutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.exceptions.EmptyPropertyException
import ar.edu.unq.eperdemic.services.exceptions.EntityNotFoundException
import ar.edu.unq.eperdemic.services.impl.MutacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MutacionServiceTest {
    val dataService = DataServiceHibernate()
    val mutacionService = MutacionServiceImpl(HibernateMutacionDAO())
    val patogenoService = PatogenoServiceImpl(HibernatePatogenoDAO())
    val mongoDAO = MongoEventoDAO()
    @BeforeEach
    fun crearSetDeDatosIniciales() {
        dataService.crearSetDeDatosIniciales()
    }

    @AfterEach
    fun eliminarTodo() {
        dataService.eliminarTodo()
        mongoDAO.deleteAll()
    }

    @Test
    fun crearMutacionTest(){
        val mutacion = Mutacion(AtributoEnum.Defensa, 5F, 10)
        mutacionService.crearMutacion(mutacion)

        Assert.assertEquals(AtributoEnum.Defensa, mutacion.atributoAIncrementar)
        Assert.assertEquals(5.0F, mutacion.adnRequerido)
        Assert.assertEquals(10, mutacion.valorAIncrementar)
        Assert.assertTrue(mutacion.mutacionesRequeridas.isEmpty())
    }

    @Test
    fun crearMutacionSinAtributoAIncrementarTest(){
        val mutacion = Mutacion()
        val exception = assertThrows<EmptyPropertyException> { mutacionService.crearMutacion(mutacion) }

        Assert.assertEquals("La propiedad atributoAIncrementar esta vacia", exception.message)


    }

    @Test
    fun recuperarMutacionCreadaTest(){
        val mutacionACrear = Mutacion(AtributoEnum.Letalidad, 5F, 10)
        mutacionService.crearMutacion(mutacionACrear)

        val mutacion = mutacionService.recuperarMutacion(4)

        Assert.assertEquals(AtributoEnum.Letalidad, mutacion.atributoAIncrementar)
        Assert.assertEquals(5.0F, mutacion.adnRequerido)
        Assert.assertEquals(10, mutacion.valorAIncrementar)
        Assert.assertTrue(mutacion.mutacionesRequeridas.isEmpty())
    }

    @Test
    fun recuperarMutacionInexistenteTest() {
        val exception = assertThrows<EntityNotFoundException> { mutacionService.recuperarMutacion(10) }
        assertEquals("La entidad Mutacion con id 10 no existe", exception.message)
    }

    @Test
    fun mutarEspecieTest() {
        mutacionService.mutar(1, 1)
        val especie = patogenoService.recuperarEspecie(1)
        assertEquals(1, especie.mutaciones.size)
        assertEquals(1, especie.mutaciones.first().id )
    }

    @Test
    fun mutarConRequisitosTest() {
        val mutacion = Mutacion(AtributoEnum.Defensa, 0F, 10)
        mutacion.mutacionesRequeridas.add(mutacionService.recuperarMutacion(1))
        mutacionService.crearMutacion(mutacion)
        mutacionService.mutar(1, 4)
        val especie = patogenoService.recuperarEspecie(1)
        assertTrue(especie.mutaciones.isEmpty())
        mutacionService.mutar(1,1)
        assertEquals(1, patogenoService.recuperarEspecie(1).mutaciones.first().id )
        assertEquals(1, patogenoService.recuperarEspecie(1).mutaciones.size)
        mutacionService.mutar(1, 4)
        assertEquals(2, patogenoService.recuperarEspecie(1).mutaciones.size)
        assertEquals(4, patogenoService.recuperarEspecie(1).mutaciones.last().id )
    }

    @Test
    fun mutarEspecieQueNoCumpleLosRequisitosTest(){
        mutacionService.mutar(1, 2)
        val especie = patogenoService.recuperarEspecie(1)
        assertEquals(0, especie.mutaciones.size)
        assertTrue(especie.mutaciones.isEmpty() )
    }

    @Test
    fun mutarEspecieInexistenteTest(){
        val exception = assertThrows<EntityNotFoundException> { mutacionService.mutar(100, 1) }
        assertEquals("La entidad Especie con id 100 no existe", exception.message)
    }

    @Test
    fun mutarEspecieConMutacionInexistenteTest() {
        val exception = assertThrows<EntityNotFoundException> { mutacionService.mutar(1, 100) }
        assertEquals("La entidad Mutacion con id 100 no existe", exception.message)
    }
}