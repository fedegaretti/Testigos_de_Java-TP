package ar.edu.unq.eperdemic.services.utils


import ar.edu.unq.eperdemic.services.elastic.DetalleOrigen
import ar.edu.unq.eperdemic.services.elastic.Log
import ar.edu.unq.eperdemic.services.elastic.Origen
import ar.edu.unq.eperdemic.services.elastic.TipoDeProceso
import ar.edu.unq.eperdemic.services.elastic.api.ElasticSearchApiConsumer
import ar.edu.unq.eperdemic.services.exceptions.EmptyPropertyException
import ar.edu.unq.eperdemic.services.impl.LogServiceImpl
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter

/**
 * Utils para chequeos relacionados a la estructura de objetos de manera generica
 */
object ObjectStructureUtils {
    private val service = LogServiceImpl(ElasticSearchApiConsumer())
    /**
     * Metodo para validar que la estructura de un objeto no contenga nulls o strings vacios
     * Nota: los ID estan exceptuados de este chequeo
     *
     * @param o (Any)
     * @throws EmptyPropertyException
     */
    fun checkEmptyAttributes(o: Any, proceso: TipoDeProceso) {
        val properties = o::class.declaredMemberProperties.filter{ isFieldAccessible(it) }
        properties.forEach {
            if (isNullOrEmpty(it, o)) {
                val ex = EmptyPropertyException("La propiedad ${it.name} esta vacia")
                val log = Log(proceso, "Error creando ${o.javaClass.simpleName}",
                        LocalDate.now(),
                        DetalleOrigen(Origen.DataError, ex.javaClass.simpleName, ex.message))
                service.postLog(log)
                throw ex
            }
        }
    }


    private fun isFieldAccessible(property: KProperty1<*, *>): Boolean {
        return property.javaGetter?.modifiers?.let { !Modifier.isPrivate(it) } ?: false
    }

    private fun isNullOrEmpty(property: KProperty1<*, *>, o: Any) : Boolean {
        val field = o.javaClass.getDeclaredField(property.name)
        val propertyValue = try { property.javaGetter!!.invoke(o) } catch (e: InvocationTargetException) { null }
        return field.name != "id" && propertyValue == null ||
                property.javaGetter!!.returnType == String::class.java &&
                (propertyValue == "" || propertyValue == null)
    }

}