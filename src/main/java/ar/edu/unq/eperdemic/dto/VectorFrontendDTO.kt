package ar.edu.unq.eperdemic.dto

import ar.edu.unq.eperdemic.modelo.*

class VectorFrontendDTO(val tipoDeVector : TipoDeVector,
                        val nombreDeUbicacionPresente: String) {


    enum class TipoDeVector {
        Persona, Insecto, Animal
    }

    fun aModelo() : Vector {
        var vector = Vector()
        vector.nombreDeLocacionActual = nombreDeUbicacionPresente
        vector.tipo = TipoDeVectorEnum.parseTipo(tipoDeVector.name)!!
        return vector
    }
}