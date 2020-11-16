package ar.edu.unq.eperdemic.services.elastic.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class ElasticQuery(@JsonProperty("query")
                        val query: QueryString)
data class QueryString(@JsonProperty("query_string")
                       val query: Query)

abstract class Query(@JsonProperty("default_field")
                     open val default_field: String)
data class StandardQuery(@JsonProperty("query")
                         val query: String, override val default_field: String) : Query(default_field)
data class DateQuery(@JsonProperty("query")
                     @JsonFormat(pattern = "yyyy-MM-dd")
                     val query: LocalDate) : Query("fecha")