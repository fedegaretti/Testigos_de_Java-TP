package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.TipoCaminoEnum
import ar.edu.unq.eperdemic.modelo.TipoDeVectorEnum
import ar.edu.unq.eperdemic.modelo.Ubicacion

interface INeo4JUbicacionDAO {
    fun crearUbicacion(ubicacion: Ubicacion) : Ubicacion
    fun conectar(nombreUbicacionOrigen:String, nombreUbicacionDestino:String, tipoDeCamino:TipoCaminoEnum)
    fun conectados(nombreUbicacion: String) : List<Ubicacion>
    fun esUbicacionMuyLejana(origen:String, destino:String) : Boolean
    fun capacidadDeExpansion(nombreDeUbicacion:String, movimientos:Int, tipoDeVector: TipoDeVectorEnum): Int
    fun caminosConectados(origen: String, destino: String) : List<TipoCaminoEnum>
    fun caminoMasCorto(tipoDeVector: TipoDeVectorEnum, nombreUbicacionOrigen: String, nombreUbicacionDestino: String) : List<String>
}
