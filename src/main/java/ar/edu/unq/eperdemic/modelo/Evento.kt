package ar.edu.unq.eperdemic.modelo

import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.BSONTimestamp

class Evento() {
    @BsonProperty("id")
    val id: String? = null
    var timestamp: BSONTimestamp? = null
    var vectorId: Int? = null
    var nombreUbicacion: String? = null
    var tipoDePatogeno: String? = null
    var tipo: TipoDeEventoEnum? = null
    var especie: String? = null
    var explicacion: String? = null

    companion object {
        fun buildEventoArribo(vector: Vector, explicacion: String) : Evento {
            return Evento(vector.id, vector.nombreDeLocacionActual, explicacion)
        }

        fun buildEventoMutacion(especie: Especie, explicacion: String) : Evento {
            return Evento(especie.patogeno.tipo, especie.nombre, explicacion)
        }

        fun buildEventoContagio(vector: Vector, vectorInfectado: Vector, explicacion: String): Evento {
            return Evento(vector, vectorInfectado, explicacion)
        }

        fun buildEventoContagio(vector: Vector, explicacion: String ) : Evento {
            return Evento(vector, explicacion)
        }

        fun buildEventoContagio(especie: Especie, explicacion: String) : Evento {
            return Evento(especie, explicacion)
        }

    }

    private constructor(especie: Especie, explicacion: String) : this() {
        this.tipoDePatogeno = especie.patogeno.tipo
        this.especie = especie.nombre
        this.explicacion = explicacion
        this.tipo = TipoDeEventoEnum.Contagio
        this.timestamp = BSONTimestamp()
    }

    private constructor(vector: Vector, explicacion: String ) : this(){
        this.vectorId = vector.id
        this.explicacion = explicacion
        this.tipo = TipoDeEventoEnum.Contagio
        this.timestamp = BSONTimestamp()
    }

    private constructor(vector: Vector, vectorInfectado: Vector, explicacion: String) : this() {
        this.vectorId = vectorInfectado.id
        this.nombreUbicacion = vector.nombreDeLocacionActual
        this.explicacion = explicacion
        this.tipo = TipoDeEventoEnum.Contagio
        this.timestamp = BSONTimestamp()
    }

    private constructor(vectorId: Int?, nombreUbicacion: String?, explicacion: String) : this() {
        this.vectorId = vectorId
        this.nombreUbicacion = nombreUbicacion
        this.tipo = TipoDeEventoEnum.Arribo
        this.explicacion = explicacion
        this.timestamp = BSONTimestamp()
    }

    private constructor(tipoDePatogeno: String?, especie: String?, explicacion: String) : this() {
        this.tipoDePatogeno = tipoDePatogeno
        this.tipo = TipoDeEventoEnum.Mutacion
        this.especie = especie
        this.explicacion = explicacion
        this.timestamp = BSONTimestamp()
    }
    
}