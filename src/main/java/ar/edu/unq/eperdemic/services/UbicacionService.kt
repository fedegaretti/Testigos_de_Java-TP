package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.TipoCaminoEnum
import ar.edu.unq.eperdemic.modelo.TipoDeVectorEnum
import ar.edu.unq.eperdemic.modelo.Ubicacion

interface UbicacionService {

    fun mover(vectorId: Int, nombreUbicacion: String)
    fun expandir(nombreUbicacion: String)
    fun conectar(nombreUbicacionOrigen: String, nombreUbicacionDestino: String, tipoDeCamino: TipoCaminoEnum)
    fun conectados(nombreUbicacion: String) : List<Ubicacion>
    fun capacidadDeExpansion(vectorId: Long, movimientos:Int): Int
    fun caminoMasCorto(tipoDeVector: TipoDeVectorEnum, nombreUbicacionOrigen: String, nombreUbicacionDestino: String) : List<String>
    fun moverMasCorto(vectorId: Int, nombreUbicacion: String)
    /* Operaciones CRUD*/
    fun crearUbicacion(nombreUbicacion: String): Ubicacion
    fun recuperarUbicacion(nombreUbicacion: String) : Ubicacion?
}