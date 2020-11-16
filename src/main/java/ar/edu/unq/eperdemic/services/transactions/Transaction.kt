package ar.edu.unq.eperdemic.services.transactions

interface Transaction {
    fun start()
    fun commit()
    fun rollback()
    fun close()
}
