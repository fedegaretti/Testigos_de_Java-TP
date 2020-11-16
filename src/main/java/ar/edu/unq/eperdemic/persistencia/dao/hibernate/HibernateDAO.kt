package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.services.transactions.HibernateTransaction

open class HibernateDAO<T>(private val entityType: Class<T>) {

    fun guardar(objeto: T) {
        val session = HibernateTransaction.currentSession
        session.save(objeto)
    }

    fun recuperar(id: Int): T {
        val session = HibernateTransaction.currentSession
        return session.get(entityType, id)
    }

    fun borrar(objeto : T){
        val session = HibernateTransaction.currentSession
        session.delete(objeto)
    }

    fun actualizar(objeto : T) {
        val session = HibernateTransaction.currentSession
        session.update(objeto)
    }

    fun recuperar(id: String): T {
        val session = HibernateTransaction.currentSession
        return session.get(entityType, id)
    }

}