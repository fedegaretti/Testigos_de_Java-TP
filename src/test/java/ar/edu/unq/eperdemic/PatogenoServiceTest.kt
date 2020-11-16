package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.TipoDeVectorEnum
import ar.edu.unq.eperdemic.modelo.exceptions.CapacidadDeContagioInvalidaException
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import ar.edu.unq.eperdemic.services.exceptions.EmptyPropertyException
import ar.edu.unq.eperdemic.services.exceptions.EntityAlreadyExistsException
import ar.edu.unq.eperdemic.services.exceptions.EntityNotFoundException
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import junit.framework.Assert.*
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.jupiter.api.*
import org.mockito.Mockito

class PatogenoServiceTest {

    private val patogenoService = PatogenoServiceImpl(HibernatePatogenoDAO())
    private val vectorService = VectorServiceImpl(HibernateVectorDAO())
    private val ubicacionService = UbicacionServiceImpl(HibernateUbicacionDAO())
    private val dataService = DataServiceHibernate()
    private val elastic = ElasticSearchApiConsumer()
    val mongoDAO = MongoEventoDAO()

    @BeforeEach
    fun init() {
        dataService.crearSetDeDatosIniciales()
        elastic.createIndex()
    }
    @AfterEach
    fun cleanUp() {
        dataService.eliminarTodo()
        mongoDAO.deleteAll()
        elastic.clear()
    }

    @Test
    fun setFactorDeContagioMayorACienTest() {
        val patogeno = Patogeno("Hongo")
        val exception = assertThrows<CapacidadDeContagioInvalidaException> {patogeno.setCapacidadDeContagio(TipoDeVectorEnum.Persona, 150)}
        Assert.assertEquals("La capacidad de contagio debe ser menor o igual a 100", exception.message )
    }

    @Test
    fun crearPatogenoTest() {
        val patogeno = Patogeno("test-tipo")
        patogeno.id = patogenoService.crearPatogeno(patogeno)
        Assert.assertEquals(3, patogeno.id)
        Assert.assertEquals(patogeno.id, patogenoService.recuperarPatogeno(3).id)
    }

    @Test
    fun crearPatogenoSinTipoTest(){
        val patogeno = Patogeno()
        val exception = assertThrows<EmptyPropertyException> { patogenoService.crearPatogeno(patogeno) }

        Assert.assertEquals("La propiedad tipo esta vacia", exception.message)
    }

    @Test
    fun crearPatogenoExistenteTest() {
        val patogeno = Patogeno("bacteria")
        val exception = assertThrows<EntityAlreadyExistsException> { patogenoService.crearPatogeno(patogeno) }
        Assert.assertEquals("La entidad Patogeno con tipo bacteria ya existe", exception.message)
        Thread.sleep(1000)
        val logs = elastic.findAll()
        Assert.assertEquals(1, logs.size)
        Assert.assertEquals(Origen.DataError, logs[0].origen!!.tipo)
        Assert.assertEquals(TipoDeProceso.CreacionPatogeno, logs[0].proceso)
        Assert.assertEquals("Error creando patogeno", logs[0].descripcion)
        Assert.assertEquals("La entidad ${Patogeno::class.simpleName} con tipo ${patogeno.tipo} ya existe", logs[0].origen!!.causa)
    }

    @Test
    fun testRecuperarATodosLosPatogenosConPatogenos() {
        val hongo = Patogeno("Hongo")
        val protozoo = Patogeno("Protozoo")
        patogenoService.crearPatogeno(hongo)
        patogenoService.crearPatogeno(protozoo)
        val patogenos = mutableListOf<Patogeno>()
        patogenos.addAll(patogenoService.recuperarATodosLosPatogenos())
        Assert.assertEquals(4, patogenos.size)
        Assert.assertEquals("Bacteria", patogenos.first().tipo)
        Assert.assertEquals("Virus", patogenos.last().tipo)
    }

    @Test
    fun testRecuperarPatogenoExistente(){
        val patogeno = patogenoService.recuperarPatogeno(1)
        Assert.assertEquals("Virus", patogeno.tipo)
        Assert.assertEquals(4, patogeno.cantidadDeEspecies)
    }


    @Test
    fun testAgregarEspecieConPatogenoExistente(){
        val especie = patogenoService.agregarEspecie(1, "sarampion", "indefinido")
        val patogeno = patogenoService.recuperarPatogeno(1)
        Assert.assertEquals(patogeno.id, especie.patogeno.id)
        Assert.assertEquals("sarampion",especie.nombre)
        Assert.assertEquals("indefinido", especie.paisDeOrigen)
        Assert.assertEquals(5,patogeno.cantidadDeEspecies)
    }

    @Test
    fun testAgregarEspecieConPatogenoInexistente(){
        val exception = assertThrows<EntityNotFoundException> {patogenoService.agregarEspecie(99, "test-especie", "test-pais")}
        Assert.assertEquals("La entidad Patogeno con id 99 no existe", exception.message)
    }

    @Test
    fun recuperarEspecieTest() {
        val especie = patogenoService.recuperarEspecie(1)

        Assert.assertEquals("Gripe", especie.nombre)
        Assert.assertEquals("China", especie.paisDeOrigen)
    }

    @Test
    fun recuperarEspecieInexistenteTest() {
        val exception = assertThrows<EntityNotFoundException> {patogenoService.recuperarEspecie(100)}
        Assert.assertEquals("La entidad Especie con id 100 no existe", exception.message)
    }

    @Test
    fun noEsPandemiaTest() {
        assertFalse(patogenoService.esPandemia(1))
    }

    @Test
    fun esPandemiaTest(){
        val gripe = patogenoService.recuperarEspecie(1)
        val cordobes = vectorService.recuperarVector(4)
        val barilochense = vectorService.recuperarVector(3)
        val pampeano = VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Persona,"La Pampa")
                .aModelo()
        val quilmenho = VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Persona,"Quilmes")
                .aModelo()
        val vectores = listOf(cordobes, barilochense, pampeano, quilmenho)
        vectores.forEach {
                it.especies.add(gripe)
        }

        vectorService.crearVector(pampeano)
        vectorService.crearVector(quilmenho)
        vectorService.actualizarVector(cordobes)
        vectorService.actualizarVector(barilochense)

        assertEquals(9, ubicacionService.cantidadUbicaciones())
        assertTrue(patogenoService.esPandemia(1))
    }

    @Test
    fun cantidadDeInfectadosTest(){
        val pampeano = vectorService.recuperarVector(6)
        val cordobes = vectorService.recuperarVector(4)
        val barilochense = vectorService.recuperarVector(5)


        vectorService.infectar(cordobes, patogenoService.recuperarEspecie(1))
        // se espera que la cantidad de infectados sea 2 porque el dataService ya infecta a uno con gripe
        assertEquals(2,patogenoService.cantidadDeInfectados(1))

        vectorService.infectar(pampeano, patogenoService.recuperarEspecie(1))

        assertEquals(3,patogenoService.cantidadDeInfectados(1))

        vectorService.infectar(barilochense, patogenoService.recuperarEspecie(1))

        assertEquals(4,patogenoService.cantidadDeInfectados(1))

    }
}
