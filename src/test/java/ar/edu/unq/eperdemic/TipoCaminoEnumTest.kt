package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.modelo.TipoCaminoEnum
import ar.edu.unq.eperdemic.modelo.TipoDeVectorEnum
import junit.framework.Assert
import org.junit.jupiter.api.Test

class TipoCaminoEnumTest {

    @Test
    fun vectoresQuePuedenAtravesarCaminosTerrestres(){
        Assert.assertTrue(TipoCaminoEnum.Terrestre.puedeSerAtravesadoPor(TipoDeVectorEnum.Persona))
        Assert.assertTrue(TipoCaminoEnum.Terrestre.puedeSerAtravesadoPor(TipoDeVectorEnum.Insecto))
        Assert.assertTrue(TipoCaminoEnum.Terrestre.puedeSerAtravesadoPor(TipoDeVectorEnum.Animal))
    }

    @Test
    fun vectoresQuePuedenAtravesarCaminosMaritimos(){
        Assert.assertTrue(TipoCaminoEnum.Maritimo.puedeSerAtravesadoPor(TipoDeVectorEnum.Persona))
        Assert.assertFalse(TipoCaminoEnum.Maritimo.puedeSerAtravesadoPor(TipoDeVectorEnum.Insecto))
        Assert.assertTrue(TipoCaminoEnum.Maritimo.puedeSerAtravesadoPor(TipoDeVectorEnum.Animal))
    }

    @Test
    fun vectoresQuePuedenAtravesarCaminosAereos(){
        Assert.assertFalse(TipoCaminoEnum.Aereo.puedeSerAtravesadoPor(TipoDeVectorEnum.Persona))
        Assert.assertTrue(TipoCaminoEnum.Aereo.puedeSerAtravesadoPor(TipoDeVectorEnum.Insecto))
        Assert.assertTrue(TipoCaminoEnum.Aereo.puedeSerAtravesadoPor(TipoDeVectorEnum.Animal))
    }
}