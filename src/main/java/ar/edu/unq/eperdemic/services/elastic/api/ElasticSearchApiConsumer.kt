package ar.edu.unq.eperdemic.services.elastic.api

import ar.edu.unq.eperdemic.services.elastic.HitsMetaData
import ar.edu.unq.eperdemic.services.elastic.Log
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.builder.QueryBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import java.time.LocalDate
import java.util.*


class ElasticSearchApiConsumer() {
    private val url = "https://fde6d9bdae1f48b99d8c48362d0169df.southamerica-east1.gcp.elastic-cloud.com:9243/"
    private val index = "errorlog"
    private var requestFactory = RequestFactory()
    private var headers: HttpHeaders = HttpHeaders()

    init {
        val plainCreds = "elastic:FRnwRt7Iy2MeqnIfaIL7wi5W"
        val base64CredsBytes = String(Base64.getEncoder().encode(plainCreds.toByteArray()))
        headers.contentType = MediaType.APPLICATION_JSON
        headers.add("Authorization", "Basic $base64CredsBytes")
    }

    fun doLog(log: Log) {
        val entity: HttpEntity<Log> = HttpEntity(log, headers)
        requestFactory.restTemplate.exchange("$url$index/1", HttpMethod.POST, entity, Log::class.java)
    }

    fun findAll() : List<Log> {
        val entity: HttpEntity<Any> = HttpEntity(headers)
        val json = getLogs(entity)
        return parseResult(json!!)
    }

    fun findByStandardQuery(value: String, field: String) : List<Log> {
        val query = QueryBuilder.buildStandardQuery(value, field)
        val entity = HttpEntity(query, headers)
        val json = getLogs(entity)
        return parseResult(json!!)
    }

    fun findByDateQuery(fecha: LocalDate): List<Log> {
        val query = QueryBuilder.buildDateQuery(fecha)
        val entity = HttpEntity(query, headers)
        val json = getLogs(entity)
        return parseResult(json!!)
    }

    fun clear() {
        val delete = "$url/$index"
        val entity: HttpEntity<Any> = HttpEntity(headers)
        requestFactory.restTemplate.exchange(delete, HttpMethod.DELETE, entity, String::class.java)
    }

    fun createIndex() {
        val put = "$url/$index"
        val entity: HttpEntity<Any> = HttpEntity(headers)
        requestFactory.restTemplate.exchange(put, HttpMethod.PUT, entity, String::class.java)
    }

    private fun getLogs(entity: HttpEntity<*>): String? {
        val searchPath = "/_search?filter_path=hits.hits._source"
        return requestFactory.restTemplate.exchange("$url$index$searchPath",
                HttpMethod.GET,
                entity,
                String::class.java).body
    }

    private fun parseResult(result: String): List<Log> {
        val mapper = jacksonObjectMapper()
        return mapper.readValue<HitsMetaData>(result).hits.hits.map { it.log!! }
    }
}