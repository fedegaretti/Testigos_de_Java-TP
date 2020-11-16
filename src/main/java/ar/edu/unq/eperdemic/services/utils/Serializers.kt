package ar.edu.unq.eperdemic.services.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDate

class LocalDateSerializer : JsonSerializer<LocalDate>() {

    override fun serialize(arg0: LocalDate, arg1: JsonGenerator, arg2: SerializerProvider) {
        arg1.writeString(arg0.toString())
    }
}

class LocalDateDeserializer : JsonDeserializer<LocalDate>() {

    override fun deserialize(arg0: JsonParser, arg1: DeserializationContext): LocalDate {
        return LocalDate.parse(arg0.text)
    }
}