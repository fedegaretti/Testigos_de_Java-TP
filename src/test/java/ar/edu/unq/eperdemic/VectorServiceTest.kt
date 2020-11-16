package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.modelo.exceptions.VectorNoInfectadoException
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateMutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.exceptions.EntityNotFoundException
import ar.edu.unq.eperdemic.services.exceptions.EmptyPropertyException
import ar.edu.unq.eperdemic.services.impl.MutacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.Assert.*
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class VectorServiceTest {
    var vectorService = VectorServiceImpl(HibernateVectorDAO())
    val ubicacionService = UbicacionServiceImpl(HibernateUbicacionDAO())
    val patogenoService = PatogenoServiceImpl(HibernatePatogenoDAO())
    val mutacionService = MutacionServiceImpl(HibernateMutacionDAO())
    val dataService = DataServiceHibernate()
    lateinit var paperas: Especie
    lateinit var cordobes: Vector
    lateinit var barilochense: Vector
    lateinit var pampeano: Vector
    lateinit var entrerriano: Vector
    lateinit var catamarquenho: Vector
    val mongoDAO = MongoEventoDAO()
    @BeforeEach
    fun crearSetDeDatosIniciales() {
        MockitoAnnotations.initMocks(this)
        dataService.crearSetDeDatosIniciales()
        paperas = patogenoService.recuperarEspecie(3)
        cordobes = vectorService.recuperarVector(4)
        barilochense = vectorService.recuperarVector(5)
        pampeano = vectorService.recuperarVector(6)
        entrerriano = Vector()
        entrerriano.nombreDeLocacionActual = "Entre Rios"
        entrerriano.tipo = TipoDeVectorEnum.Persona
        vectorService.crearVector(entrerriano)
        catamarquenho = Vector()
        catamarquenho.nombreDeLocacionActual = "Catamarca"
        catamarquenho.tipo = TipoDeVectorEnum.Persona
        vectorService.crearVector(catamarquenho)
    }

    @AfterEach
    fun eliminarTodo() {
        dataService.eliminarTodo()
        mongoDAO.deleteAll()
    }

    @Test
    fun crearVectorTest() {
        val locacion = ubicacionService.crearUbicacion("Locacion-Test")
        val vector = Vector(locacion.nombreUbicacion)
        vector.tipo = TipoDeVectorEnum.Persona
        vectorService.crearVector(vector)

        assertEquals(9, vector.id)
        assertEquals("Locacion-Test", vector.nombreDeLocacionActual)
    }

    @Test
    fun crearVectorSinLocacionTest() {
        val vector = Vector()
        vector.tipo = TipoDeVectorEnum.Persona
        val exception = assertThrows<EmptyPropertyException> { vectorService.crearVector(vector) }

        assertEquals("La propiedad nombreDeLocacionActual esta vacia", exception.message)
    }

    @Test
    fun crearVectorSinTipoDeVectorTest() {
        val locacion = ubicacionService.crearUbicacion("Locacion-Test")
        val vector = Vector(locacion.nombreUbicacion)
        val exception = assertThrows<EmptyPropertyException> { vectorService.crearVector(vector) }

        assertEquals("La propiedad tipo esta vacia", exception.message)
    }

    @Test
    fun recuperarVectorTest() {
        val vector = vectorService.recuperarVector(1)

        assertEquals("Buenos Aires", vector.nombreDeLocacionActual)
        assertEquals(1, vector.id)
    }

    @Test
    fun recuperarVectorNoExistenteTest() {
        val exception = assertThrows<EntityNotFoundException> { vectorService.recuperarVector(100) }

        assertEquals("La entidad Vector con id 100 no existe", exception.message)
    }

    @Test
    fun borrarVectorTest() {
        vectorService.borrarVector(3)
        
        val exception = assertThrows<EntityNotFoundException> { vectorService.recuperarVector(3) }
        assertEquals("La entidad Vector con id 3 no existe", exception.message)
    }

    @Test
    fun borrarVectorInexistenteTest() {
        val exception = assertThrows<EntityNotFoundException> { vectorService.borrarVector(15) }
        assertEquals("La entidad Vector con id 15 no existe", exception.message)
    }

    @Test
    fun infectarHumanoContagioExitosoTest() {
        val paperasSpy = spy(paperas)
        doReturn(true).`when`(paperasSpy)!!.esContagioExitoso(TipoDeVectorEnum.Persona)

        assertTrue(cordobes.especies.isEmpty())

        vectorService.infectar(cordobes, paperasSpy!!)

        assertFalse(cordobes.especies.isEmpty())
    }

    @Test
    fun infectarHumanoContagioNoExitosoTest() {
        val paperasSpy = spy(paperas)
        doReturn(false).`when`(paperasSpy)!!.esContagioExitoso(TipoDeVectorEnum.Persona)

        assertTrue(cordobes.especies.isEmpty())

        vectorService.infectar(cordobes, paperasSpy!!)

        assertTrue(cordobes.especies.isEmpty())
    }

    @Test
    fun getEnfermedadesTest(){
        val enfermedades = vectorService.enfermedades(1)

        assertEquals(5,enfermedades.size)
    }

    @Test
    fun contagiarTest(){
        // el vector con id 2 es un animal, el vector con id 3 es un insecto
        val vectores = mutableListOf(vectorService.recuperarVector(2), vectorService.recuperarVector(3))

        vectorService.infectar(cordobes, paperas)
        vectorService.contagiar(cordobes, vectores)

        // el vector 2 no tiene que tener especies porque no pudo ser infectado por una Persona
        assertEquals(0, vectorService.enfermedades(2).size)
        // el vector 3 tiene que tener una especie y tiene que ser la misma que la del vector infectado
        assertEquals(1, vectorService.enfermedades(3).size)
        assertEquals(cordobes.especies[0].id, vectorService.enfermedades(3)[0].id)
    }

    @Test
    fun contagiarConVectorNoInfectadoTest() {
        val vectores = mutableListOf(vectorService.recuperarVector(2), vectorService.recuperarVector(3))

        val exception = assertThrows<VectorNoInfectadoException> { vectorService.contagiar(cordobes, vectores) }

        assertEquals("El vector no esta infectado", exception.message)
    }

    @Test
    fun crearVectorConUbicacionInexistenteTest() {
        val pibe = Vector()
        pibe.nombreDeLocacionActual = "Pibelandia"
        pibe.tipo = TipoDeVectorEnum.Persona
        val exception = assertThrows<EntityNotFoundException> { vectorService.crearVector(pibe) }
        assertEquals("La entidad Ubicacion con id Pibelandia no existe", exception.message)
    }

    @Test
    fun adnEspecieAumentaAlInfectarYMutaTest(){
        val mutacion = Mutacion(AtributoEnum.Defensa, 1.00F, 10)
        mutacionService.crearMutacion(mutacion)
        val paperasSpy = spy(paperas)
        doReturn(true).`when`(paperasSpy)!!.esContagioExitoso(TipoDeVectorEnum.Persona)

        vectorService.infectar(barilochense, paperasSpy)

        assertEquals(0.20F, paperasSpy.adn)

        vectorService.infectar(pampeano, paperasSpy)
        vectorService.infectar(cordobes, paperasSpy)

        assertEquals(0.60F, paperasSpy.adn)

        vectorService.infectar(entrerriano, paperasSpy)
        vectorService.infectar(catamarquenho, paperasSpy)

        assertEquals(1.00F, paperasSpy.adn)

        mutacionService.mutar(paperasSpy.id, mutacion.id)

        assertEquals(0.0F, patogenoService.recuperarEspecie(paperasSpy.id).adn)
    }

    @Test
    fun adnNoAumentaInfeccionNoExitosaTest(){
        val paperasSpy = spy(paperas)
        doReturn(false).`when`(paperasSpy)!!.esContagioExitoso(TipoDeVectorEnum.Persona)

        vectorService.infectar(cordobes, paperasSpy)

        assertTrue(cordobes.especies.isEmpty())
        assertEquals(0.0F, paperasSpy.adn)
    }

    @Test
    fun adnNoAumentaInfeccionExitosaANoHumanoTest(){
        val paperasSpy = spy(paperas)
        doReturn(true).`when`(paperasSpy)!!.esContagioExitoso(TipoDeVectorEnum.Persona)
        val animalCordobes = vectorService.recuperarVector(2)

        vectorService.infectar(animalCordobes, paperasSpy)

        assertFalse(animalCordobes.especies.isEmpty())
        assertEquals(0.0F, paperasSpy.adn)
    }

    @Test
    fun mutarEspecieConIncrementoDeCapacidadDeContagioTest() {
        val paperasSpy = spy(paperas)
        doReturn(true).`when`(paperasSpy)!!.esContagioExitoso(TipoDeVectorEnum.Persona)

        val mutacion = Mutacion(AtributoEnum.CapacidadDeContagio, 1.0F, 10)
        mutacionService.crearMutacion(mutacion)

        assertEquals(100, paperasSpy.getCapacidadDeContagio(TipoDeVectorEnum.Persona))
        assertEquals(0.0F, paperasSpy.adn)

        vectorService.infectar(catamarquenho, paperasSpy)
        vectorService.infectar(cordobes, paperasSpy)
        vectorService.infectar(barilochense, paperasSpy)
        vectorService.infectar(pampeano, paperasSpy)
        vectorService.infectar(entrerriano, paperasSpy)

        assertEquals(100, paperasSpy.getCapacidadDeContagio(TipoDeVectorEnum.Persona))
        assertEquals(1.0F, paperasSpy.adn)

        mutacionService.mutar(paperas.id, mutacion.id)

        assertEquals(110, patogenoService.recuperarEspecie(paperas.id).getCapacidadDeContagio(TipoDeVectorEnum.Persona))
    }

    @Test
    fun mutarEspecieSinIncrementoDeCapacidadDeContagioTest() {
        val paperasSpy = spy(paperas)
        doReturn(true).`when`(paperasSpy)!!.esContagioExitoso(TipoDeVectorEnum.Persona)

        val mutacion = Mutacion(AtributoEnum.Letalidad, 1.0F, 10)
        mutacionService.crearMutacion(mutacion)

        assertEquals(100, paperasSpy.getCapacidadDeContagio(TipoDeVectorEnum.Persona))
        assertEquals(0.0F, paperasSpy.adn)

        vectorService.infectar(catamarquenho, paperasSpy)
        vectorService.infectar(cordobes, paperasSpy)
        vectorService.infectar(barilochense, paperasSpy)
        vectorService.infectar(pampeano, paperasSpy)
        vectorService.infectar(entrerriano, paperasSpy)

        assertEquals(100, paperasSpy.getCapacidadDeContagio(TipoDeVectorEnum.Persona))
        assertEquals(1.0F, paperasSpy.adn)

        mutacionService.mutar(paperas.id, mutacion.id)

        assertEquals(100, patogenoService.recuperarEspecie(paperas.id).getCapacidadDeContagio(TipoDeVectorEnum.Persona))
    }
}