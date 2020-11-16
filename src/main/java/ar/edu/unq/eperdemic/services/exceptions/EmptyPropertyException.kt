package ar.edu.unq.eperdemic.services.exceptions

import java.lang.Exception

class EmptyPropertyException(override val message: String?) : Exception() {
}