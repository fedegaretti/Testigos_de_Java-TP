package ar.edu.unq.eperdemic.services.elastic.builder

import ar.edu.unq.eperdemic.services.elastic.api.DateQuery
import ar.edu.unq.eperdemic.services.elastic.api.ElasticQuery
import ar.edu.unq.eperdemic.services.elastic.api.QueryString
import ar.edu.unq.eperdemic.services.elastic.api.StandardQuery
import java.time.LocalDate

class QueryBuilder {
    companion object {
        fun buildStandardQuery(query: String, default_field: String) : ElasticQuery {
            return ElasticQuery(QueryString(StandardQuery(query, default_field)))
        }
        fun buildDateQuery(date: LocalDate) : ElasticQuery {
            return ElasticQuery(QueryString(DateQuery(date)))
        }
    }
}