package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.transactions.HibernateTransaction

open class HibernateUbicacionDAO: HibernateDAO<Ubicacion>(Ubicacion::class.java), UbicacionDAO {
    override fun cantidadUbicaciones(): Long {
        val session = HibernateTransaction.currentSession
        val hql = ("select count(*) from Ubicacion")
        val query = session.createQuery(hql, Long::class.javaObjectType)

        return query.singleResult
    }
}