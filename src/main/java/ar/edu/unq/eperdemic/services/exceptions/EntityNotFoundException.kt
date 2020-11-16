package ar.edu.unq.eperdemic.services.exceptions

import java.lang.Exception

class EntityNotFoundException(override val message: String?) : Exception()