package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateMutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import ar.edu.unq.eperdemic.utils.neo4j.DataServiceNeo4j
import org.junit.Assert.assertEquals

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations

class FeedServiceTest {

    val ubicacionService = UbicacionServiceImpl(HibernateUbicacionDAO())
    val feedService = FeedServiceImpl()
    val dataService = DataServiceHibernate()
    val neo4jDataService = DataServiceNeo4j()
    val mongoDAO = MongoEventoDAO()
    val vectorService = VectorServiceImpl(HibernateVectorDAO())
    val patogenoService = PatogenoServiceImpl(HibernatePatogenoDAO())
    val mutacionService = MutacionServiceImpl(HibernateMutacionDAO())
    lateinit var portenho: Vector
    lateinit var cordobes: Vector
    lateinit var pampeano: Vector
    lateinit var barilochense: Vector
    lateinit var quilmenho: Vector
    lateinit var catamarquenho: Vector
    lateinit var entrerriano: Vector


    @BeforeEach
    fun crearSetDeDatosIniciales() {
        MockitoAnnotations.initMocks(this)
        dataService.crearSetDeDatosIniciales()
        neo4jDataService.crearSetDeDatosIniciales()
        portenho = vectorService.recuperarVector(1)
        cordobes = vectorService.recuperarVector(4)
        pampeano = vectorService.recuperarVector(6)
        barilochense = vectorService.recuperarVector(5)
        catamarquenho = Vector()
        catamarquenho.nombreDeLocacionActual = "Catamarca"
        catamarquenho.tipo = TipoDeVectorEnum.Persona
        vectorService.crearVector(catamarquenho)
        quilmenho = Vector()
        quilmenho.nombreDeLocacionActual = "Quilmes"
        quilmenho.tipo = TipoDeVectorEnum.Persona
        vectorService.crearVector(quilmenho)
        entrerriano = Vector()
        entrerriano.nombreDeLocacionActual = "Entre Rios"
        entrerriano.tipo = TipoDeVectorEnum.Persona
        vectorService.crearVector(entrerriano)

    }

    @AfterEach
    fun eliminarTodo() {
        dataService.eliminarTodo()
        neo4jDataService.eliminarTodo()
        mongoDAO.deleteAll()
    }

    @Test
    fun feedPatogenoTest(){
        //Al crearse la especie sarampion se genera un logeo de mutación
        val sarampion = patogenoService.agregarEspecie(1, "sarampion", "indefinido")
        val virus = patogenoService.recuperarPatogeno(1)
        val mutacion = Mutacion(AtributoEnum.Defensa, 0F, 10)
        val vectores = listOf(cordobes, pampeano, barilochense, catamarquenho, entrerriano)
        vectorService.crearVector(quilmenho)
        vectorService.crearVector(catamarquenho)
        quilmenho.infectar(sarampion)
        //Al contagiar a cinco vectores de diferentes ubicaciones se generan 5 logeos de contagio
        //ya que la especie no se encontraba presente anteriormente en estas ubicaciones.
        //Tambien se genera un logeo de contagio mas debido a que la especie sarampion se convirtió en pandemia
        vectorService.contagiar(quilmenho, vectores)
        mutacion.mutacionesRequeridas.add(mutacionService.recuperarMutacion(1))
        mutacionService.crearMutacion(mutacion)
        //Al mutar la especie sarampion se genera un logeo de mutación
        mutacionService.mutar(sarampion.id, mutacion.id)
        val eventos = feedService.feedPatogeno(virus.tipo)
        assertEquals(8,eventos.size)
        assertEquals(TipoDeEventoEnum.Mutacion, eventos.first().tipo)
        assertEquals(TipoDeEventoEnum.Contagio, eventos[4].tipo)
        assertEquals("sarampion", eventos[4].especie)
        assertEquals(TipoDeEventoEnum.Mutacion, eventos.last().tipo)
        assertEquals("Virus", eventos.first().tipoDePatogeno)
        assertEquals("Virus", eventos.last().tipoDePatogeno)
    }

    @Test
    fun feedVectorTest() {
        ubicacionService.conectar("Cordoba", "Bariloche", TipoCaminoEnum.Terrestre)
        ubicacionService.mover(1, "Cordoba")
        ubicacionService.mover(4, "Bariloche")
        val eventos = feedService.feedVector(4)
        assertEquals(4, eventos.size)
        assertEquals(TipoDeEventoEnum.Contagio, eventos.first().tipo)
        assertEquals(TipoDeEventoEnum.Contagio, eventos.last().tipo)
    }

    @Test
    fun moverUnVectorGeneraEventoDeArriboYContagioTest() {
        ubicacionService.conectar("Cordoba", "Quilmes", TipoCaminoEnum.Terrestre)
        ubicacionService.mover(1, "Cordoba")
        ubicacionService.mover(1, "Quilmes")
        val eventos = feedService.feedUbicacion("Cordoba")
        assertEquals(3, eventos.size)
        assertEquals(TipoDeEventoEnum.Arribo, eventos.first().tipo)
        assertEquals("Quilmes", eventos.first().nombreUbicacion)
        assertEquals(TipoDeEventoEnum.Contagio, eventos[1].tipo)
        assertEquals(TipoDeEventoEnum.Arribo, eventos.last().tipo)
        assertEquals("Cordoba", eventos.last().nombreUbicacion)
    }
}