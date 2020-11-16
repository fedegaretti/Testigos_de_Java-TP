package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import ar.edu.unq.eperdemic.services.impl.LogServiceImpl
import ar.edu.unq.eperdemic.utils.elastic.ElasticSearchDataService
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LogServiceTest {
    private val dataService = ElasticSearchDataService()
    private val service = LogServiceImpl(ElasticSearchApiConsumer())

    @BeforeEach
    fun init() {
        dataService.crearSetDeDatosIniciales()
        Thread.sleep(1000)
    }
    @AfterEach
    fun cleanUp() {
        dataService.eliminarTodo()
    }

    @Test
    fun findAllTest() {
        val result = service.findAll()
        Assert.assertEquals(8, result.size)
    }

    @Test
    fun findByProcesoTest() {
        Assert.assertEquals(3, service.findByProceso(TipoDeProceso.CreacionPatogeno).size)
        Assert.assertEquals(2, service.findByProceso(TipoDeProceso.CreacionUbicacion).size)
        Assert.assertEquals(1, service.findByProceso(TipoDeProceso.CreacionMutacion).size)
        Assert.assertEquals(1, service.findByProceso(TipoDeProceso.CreacionVector).size)
        Assert.assertEquals(1, service.findByProceso(TipoDeProceso.MovimientoVector).size)
    }

    @Test
    fun findByOrigenTest() {
        val result = service.findByOrigen(Origen.DataError)
        Assert.assertEquals(7, result.size)
    }

    @Test
    fun findByFechaTest(){
        val localDate = LocalDate.now()
        val result = service.findByFecha(localDate)
        Assert.assertEquals(8, result.size)
    }
}