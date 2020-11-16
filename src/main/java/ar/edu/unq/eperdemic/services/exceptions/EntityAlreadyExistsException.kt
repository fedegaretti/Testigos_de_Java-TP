package ar.edu.unq.eperdemic.services.exceptions

import java.lang.Exception

class EntityAlreadyExistsException(override val message: String?) : Exception() {
}