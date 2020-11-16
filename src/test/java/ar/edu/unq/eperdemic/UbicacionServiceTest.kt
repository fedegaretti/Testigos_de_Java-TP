package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.modelo.TipoCaminoEnum
import ar.edu.unq.eperdemic.modelo.TipoDeVectorEnum
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.exceptions.UbicacionMuyLejanaException
import ar.edu.unq.eperdemic.modelo.exceptions.UbicacionNoAlcanzableException
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongo.MongoEventoDAO
import ar.edu.unq.eperdemic.services.exceptions.EntityNotFoundException
import ar.edu.unq.eperdemic.services.exceptions.EmptyPropertyException
import ar.edu.unq.eperdemic.services.exceptions.EntityAlreadyExistsException
import ar.edu.unq.eperdemic.services.impl.FeedServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import ar.edu.unq.eperdemic.utils.neo4j.DataServiceNeo4j
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.MockitoAnnotations
import org.mockito.Spy

class UbicacionServiceTest {

    val ubicacionService = UbicacionServiceImpl(HibernateUbicacionDAO())
    @Spy
    val vectorService = VectorServiceImpl(HibernateVectorDAO())
    val dataService = DataServiceHibernate()
    val neo4jDataService = DataServiceNeo4j()
    val mongoDAO = MongoEventoDAO()
    val feedService = FeedServiceImpl()
    lateinit var portenho: Vector
    lateinit var cordobes: Vector
    lateinit var insecto: Vector

    @BeforeEach
    fun crearSetDeDatosIniciales() {
        MockitoAnnotations.initMocks(this)
        dataService.crearSetDeDatosIniciales()
        neo4jDataService.crearSetDeDatosIniciales()
        portenho = vectorService.recuperarVector(1)
        insecto = vectorService.recuperarVector(3)
        cordobes = vectorService.recuperarVector(4)
    }

    @AfterEach
    fun eliminarTodo() {
        dataService.eliminarTodo()
        neo4jDataService.eliminarTodo()
        mongoDAO.deleteAll()
    }

    @Test
    fun crearUbicacionTest(){
        val ubicacion = ubicacionService.crearUbicacion("Adrogue")
        Assert.assertEquals("Adrogue", ubicacion.nombreUbicacion)
    }

    @Test
    fun crearUbicacionExistenteTest(){
        val exception = assertThrows<EntityAlreadyExistsException>{ubicacionService.crearUbicacion("Bariloche")}
        Assert.assertEquals("La entidad Ubicacion con id Bariloche ya existe", exception.message)
    }

    @Test
    fun crearUbicacionSinNombreTest() {
        val exception = assertThrows<EmptyPropertyException> {ubicacionService.crearUbicacion("")}
        Assert.assertEquals("La propiedad nombreUbicacion esta vacia", exception.message)

    }

    @Test
    fun recuperarUbicacionTest() {
        val ubicacion = ubicacionService.recuperarUbicacion("Entre Rios")
        Assert.assertEquals(ubicacion!!.nombreUbicacion, "Entre Rios")
    }

    @Test
    fun recuperarUbicacionInexistenteTest() {
        val exception = assertThrows<EntityNotFoundException> { ubicacionService.recuperarUbicacion("Tucuman") }
        assertEquals("La entidad Ubicacion con id Tucuman no existe", exception.message)
    }

    @Test
    fun moverConVectorInfectadoTest(){
        Assert.assertEquals("Buenos Aires", portenho.nombreDeLocacionActual)
        Assert.assertTrue(portenho.estaInfectado())
        Assert.assertFalse(cordobes.estaInfectado())

        ubicacionService.mover(1, "Cordoba")
        portenho = vectorService.recuperarVector(1)
        cordobes = vectorService.recuperarVector(4)

        //El portenho cambio su ubicacion a "Cordoba" y el Cordobes ahora esta infectado
        Assert.assertEquals("Cordoba", portenho.nombreDeLocacionActual)
        Assert.assertTrue(cordobes.estaInfectado())
    }

    @Test
    fun moverConVectorNoInfectadoTest(){
        Assert.assertEquals("Bariloche", insecto.nombreDeLocacionActual)
        Assert.assertFalse(insecto.estaInfectado())
        Assert.assertFalse(cordobes.estaInfectado())

        ubicacionService.mover(3, "Cordoba")
        insecto = vectorService.recuperarVector(3)
        cordobes = vectorService.recuperarVector(4)

        //El Insecto cambio su ubicacion a "Cordoba" pero el cordobes no se infecto
        Assert.assertEquals("Cordoba", insecto.nombreDeLocacionActual)
        Assert.assertFalse(cordobes.estaInfectado())
    }

    @Test
    fun moverAUbicacionInexistenteTest() {
        val exception = assertThrows<EntityNotFoundException> {ubicacionService.mover(1, "Chaco")}
        Assert.assertEquals("La entidad Ubicacion con id Chaco no existe", exception.message )
    }

    @Test
    fun expandirVectorInfectadoTest(){
        val buenosAires = ubicacionService.recuperarUbicacion("Buenos Aires")
        val carlos = Vector(buenosAires!!.nombreUbicacion)
        carlos.tipo = TipoDeVectorEnum.Persona
        val carlosId = vectorService.crearVector(carlos).id

        Assert.assertFalse(carlos.estaInfectado())

        ubicacionService.expandir("Buenos Aires")

        Assert.assertTrue(vectorService.recuperarVector(carlosId!!).estaInfectado())
    }

    @Test
    fun expandirVectorNoInfectadoTest(){
        val cordoba = ubicacionService.recuperarUbicacion("Cordoba")
        val jorge = Vector(cordoba!!.nombreUbicacion)
        jorge.tipo = TipoDeVectorEnum.Persona
        val jorgeId = vectorService.crearVector(jorge).id

        Assert.assertFalse(cordobes.estaInfectado())
        Assert.assertFalse(jorge.estaInfectado())

        ubicacionService.expandir("Cordoba")

        Assert.assertFalse(vectorService.recuperarVector(jorgeId!!).estaInfectado())
    }

    @Test
    fun expandirUbicacionInexistenteTest(){
        val exception = assertThrows<EntityNotFoundException> { ubicacionService.expandir("Bernal") }
        assertEquals("La entidad Ubicacion con id Bernal no existe", exception.message)
    }

    @Test
    fun conectadosTest(){
        val entreRios = ubicacionService.recuperarUbicacion("Entre Rios")
        val catamarca = ubicacionService.recuperarUbicacion("Catamarca")
        val cordoba = ubicacionService.recuperarUbicacion("Cordoba")
        ubicacionService.conectar(catamarca!!.nombreUbicacion,entreRios!!.nombreUbicacion, TipoCaminoEnum.Terrestre)
        ubicacionService.conectar(catamarca.nombreUbicacion,cordoba!!.nombreUbicacion,TipoCaminoEnum.Terrestre)
        val conectados = ubicacionService.conectados(catamarca.nombreUbicacion).sortedBy { it.nombreUbicacion }
        assertEquals(2, conectados.size)
        assertEquals("Cordoba" ,conectados.first().nombreUbicacion)
        assertEquals("Entre Rios" ,conectados.last().nombreUbicacion)
    }

    @Test
    fun conectadosSinRepetidos(){
        val entreRios = ubicacionService.recuperarUbicacion("Entre Rios")
        val catamarca = ubicacionService.recuperarUbicacion("Catamarca")
        ubicacionService.conectar(catamarca!!.nombreUbicacion,entreRios!!.nombreUbicacion,TipoCaminoEnum.Terrestre)
        ubicacionService.conectar(catamarca.nombreUbicacion,entreRios.nombreUbicacion,TipoCaminoEnum.Maritimo)
        val conectados = ubicacionService.conectados(catamarca.nombreUbicacion).sortedBy { it.nombreUbicacion }

        assertEquals(conectados.toSet().size, conectados.size)
    }

    @Test
    fun noConectadosTest(){
        val entreRios = ubicacionService.recuperarUbicacion("Entre Rios")
        val catamarca = ubicacionService.recuperarUbicacion("Catamarca")
        ubicacionService.conectar(entreRios!!.nombreUbicacion,catamarca!!.nombreUbicacion,TipoCaminoEnum.Terrestre)
        assertTrue(ubicacionService.conectados("Catamarca").isEmpty())
    }

    @Test
    fun conectadosListaVaciaTest(){
        assertTrue(ubicacionService.conectados("Catamarca").isEmpty())
    }

    @Test
    fun moverAUbicacionMuyLejanaTest(){
        val exception = assertThrows<UbicacionMuyLejanaException> { ubicacionService.mover(1, "La Pampa") }
        assertEquals("La ubicacion a la que intenta moverse no esta conectada", exception.message)
    }

    @Test
    fun moverAUbicacionInalcanzableTest(){
        val exception = assertThrows<UbicacionNoAlcanzableException> { ubicacionService.mover(4, "Quilmes") }
        assertEquals("La ubicacion a la que intenta moverse no tiene un camino alcanzable", exception.message)
    }

    @Test
    fun capacidadDeExpansionTest() {
        ubicacionService.conectar("Buenos Aires", "Entre Rios", TipoCaminoEnum.Terrestre)
        // con un movimiento puede moverse a Cordoba y Entre Rios
        assertEquals(2, ubicacionService.capacidadDeExpansion(1, 1))
        // con dos movimientos puede moverse a Cordoba, Entre Rios y La Pampa
        assertEquals(3, ubicacionService.capacidadDeExpansion(1, 2))
        // con tres movimientos puede moverse a Cordoba, Entre Rios, La Pampa y Quilmes
        assertEquals(4, ubicacionService.capacidadDeExpansion(1, 3))
    }

    @Test
    fun capacidadDeExpansionVariosTiposDeCaminoTest() {
        ubicacionService.conectar("Buenos Aires", "Entre Rios", TipoCaminoEnum.Maritimo)
        // con un movimiento puede moverse a Cordoba y Entre Rios
        assertEquals(2, ubicacionService.capacidadDeExpansion(1, 1))
        // con dos movimientos puede moverse a Cordoba, Entre Rios y La Pampa
        assertEquals(3, ubicacionService.capacidadDeExpansion(1, 2))
        // con tres movimientos puede moverse a Cordoba, Entre Rios, La Pampa y Quilmes
        assertEquals(4, ubicacionService.capacidadDeExpansion(1, 3))
    }

    @Test
    fun capacidadDeExpansionConTiposDeCaminoNoValidosTest() {
        ubicacionService.conectar("Buenos Aires", "Entre Rios", TipoCaminoEnum.Aereo)
        // con un movimiento puede moverse a Cordoba
        assertEquals(1, ubicacionService.capacidadDeExpansion(1, 1))
        // con dos movimientos puede moverse a Cordoba
        assertEquals(1, ubicacionService.capacidadDeExpansion(1, 2))
        // con tres movimientos puede moverse a Cordoba
        assertEquals(1, ubicacionService.capacidadDeExpansion(1, 3))
    }

    @Test
    fun caminoMasCortoMenorAlConectarOrigenYDestinoTest() {
        //Buenos Aires-[terrestre]->Cordoba-[aereo]->Quilmes
        var caminoMasCorto = ubicacionService.caminoMasCorto(TipoDeVectorEnum.Animal, "Buenos Aires", "Quilmes")
        assertEquals(listOf("Buenos Aires", "Cordoba", "Quilmes"), caminoMasCorto)
        ubicacionService.conectar("Buenos Aires", "Quilmes", TipoCaminoEnum.Aereo)
        caminoMasCorto = ubicacionService.caminoMasCorto(TipoDeVectorEnum.Animal, "Buenos Aires", "Quilmes")
        assertEquals(listOf("Buenos Aires", "Quilmes"), caminoMasCorto)
    }

    @Test
    fun caminoMasCortoNoAlcanzableParaUnaPersonaTest() {
        //Buenos Aires-[terrestre]->Cordoba-[aereo]->Quilmes
        val exception = assertThrows<UbicacionNoAlcanzableException>{
            ubicacionService.caminoMasCorto(TipoDeVectorEnum.Persona, "Buenos Aires", "Quilmes")
        }
        assertEquals("La ubicacion a la que intenta moverse no tiene un camino alcanzable", exception.message)
    }

    @Test
    fun caminoMasCortoALaMismaUbicacionEsVacioTest() {
        assertTrue(ubicacionService.caminoMasCorto(TipoDeVectorEnum.Persona, "Buenos Aires", "Buenos Aires").isEmpty())
    }

    @Test
    fun caminoMasCortoNoAlcanzableParaUbicacionesConCaminosOpuestosTest() {
        //Buenos Aires-[terrestre]->Cordoba-[aereo]->Quilmes
        val exception = assertThrows<UbicacionNoAlcanzableException>{
            ubicacionService.caminoMasCorto(TipoDeVectorEnum.Animal, "Quilmes", "Buenos Aires")
        }
        assertEquals("La ubicacion a la que intenta moverse no tiene un camino alcanzable", exception.message)
    }

    @Test
    fun unaPersonaDeCordobaNoPuedeMoverseMasCortoABerazateguiTest() {
        //Cordoba-[Aereo]->Quilmes-[Aereo/Terrestre]->Berazategui
        val exception = assertThrows<UbicacionNoAlcanzableException>{
            ubicacionService.moverMasCorto(4, "Berazategui")
        }
        assertEquals("La ubicacion a la que intenta moverse no tiene un camino alcanzable", exception.message)
    }

    @Test
    fun unAnimalDeCordobaPuedeMoverseMasCortoABerazateguiTest() {
        //Cordoba-[Aereo]->Quilmes-[Aereo/Terrestre]->Berazategui
        assertEquals("Cordoba", vectorService.recuperarVector(2).nombreDeLocacionActual)
        ubicacionService.moverMasCorto(2, "Berazategui")
        assertEquals("Berazategui", vectorService.recuperarVector(2).nombreDeLocacionActual)
    }

    @Test
    fun unAnimalDeCordobaNoPuedeMoverseMasCortoABuenosAiresTest() {
        //Buenos Aires-[Terrestre]->Cordoba
        assertEquals("Cordoba", vectorService.recuperarVector(2).nombreDeLocacionActual)
        val exception = assertThrows<UbicacionNoAlcanzableException>{
            ubicacionService.moverMasCorto(2, "Buenos Aires")
        }
        assertEquals("La ubicacion a la que intenta moverse no tiene un camino alcanzable", exception.message)
        assertEquals("Cordoba", vectorService.recuperarVector(2).nombreDeLocacionActual)
    }
}
