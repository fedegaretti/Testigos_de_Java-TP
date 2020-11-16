package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VectorFrontendDTOTest {
    val dataService = DataServiceHibernate()

    @BeforeEach
    fun crearSetDeDatosIniciales() {
        dataService.crearSetDeDatosIniciales()
    }

    @AfterEach
    fun eliminarTodo() {
        dataService.eliminarTodo()
    }

    @Test
    fun aModeloTest(){
        val vectorPersona = VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Persona, "Quilmes")
        val vectorAnimal =  VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Animal, "Berazategui")
        val vectorInsecto =  VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Insecto, "Lanus")

        Assert.assertEquals(vectorPersona.nombreDeUbicacionPresente, vectorPersona.aModelo().nombreDeLocacionActual)
        Assert.assertEquals(vectorPersona.tipoDeVector.name, vectorPersona.aModelo().tipo.name)

        Assert.assertEquals(vectorAnimal.nombreDeUbicacionPresente, vectorAnimal.aModelo().nombreDeLocacionActual)
        Assert.assertEquals(vectorAnimal.tipoDeVector.name, vectorAnimal.aModelo().tipo.name)

        Assert.assertEquals(vectorInsecto.nombreDeUbicacionPresente, vectorInsecto.aModelo().nombreDeLocacionActual)
        Assert.assertEquals(vectorInsecto.tipoDeVector.name, vectorInsecto.aModelo().tipo.name)
    }
}