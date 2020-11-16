package ar.edu.unq.eperdemic.services.elastic

import ar.edu.unq.eperdemic.services.utils.LocalDateDeserializer
import ar.edu.unq.eperdemic.services.utils.LocalDateSerializer
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.LocalDate

data class Log (val proceso: TipoDeProceso?, val descripcion: String?,
                @JsonSerialize(using = LocalDateSerializer::class)
                @JsonDeserialize(using = LocalDateDeserializer::class)
                @JsonFormat(pattern = "yyyy-MM-dd") val fecha: LocalDate?, val origen: DetalleOrigen?)

data class HitsMetaData(@JsonProperty("hits") val hits: InnerHits = InnerHits())

data class InnerHits(@JsonProperty("hits") val hits: List<SourceLog> = emptyList())

data class SourceLog(@JsonProperty("_source") val log: Log?)

/*
    {
      "hits": {
        "hits": [
          {
            "_source": {
              "proceso": "MovimientoVector",
              "descripcion": "Error en el proceso MovimientoVector",
              "fecha": "2020-07-15",
              "origen": {
                "tipo": "ProcessError",
                "exception": "UbicacionMuyLejanaException",
                "causa": "La ubicacion a la que intenta moverse no esta conectada"
              }
            }
          },
          {
            "_source": {
              "proceso": "CreacionPatogeno",
              "descripcion": "Error creando patogeno",
              "fecha": "2020-07-15",
              "origen": {
                "tipo": "DataError",
                "exception": "EntityAlreadyExistsException",
                "causa": "La entidad Patogeno con tipo bacteria ya existe"
              }
           }
        ]
      }
    }
*/