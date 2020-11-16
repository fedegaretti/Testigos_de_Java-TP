package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.transactions.HibernateTransaction

open class HibernateVectorDAO : HibernateDAO<Vector>(Vector::class.java), VectorDAO {
    override fun getVectoresByLocacion(nombreDeLocacionActual: String?) : List<Vector> {
        val session = HibernateTransaction.currentSession
        val hql = ("from Vector v "
                + "where v.nombreDeLocacionActual = :nombreDeLocacionActual ")

        val query = session.createQuery(hql, Vector::class.java)
        query.setParameter("nombreDeLocacionActual", nombreDeLocacionActual)

        return query.resultList
    }

    override fun getVectorRandomEnLocacion(nombreDeLocacionActual: String?) : Vector? {
        val session = HibernateTransaction.currentSession
        val hql = """
            select v
            from Vector v
            join v.especies
            where v.nombreDeLocacionActual = :nombreDeLocacionActual
            order by rand()
            """.trimIndent()
        val query = session.createQuery(hql, Vector::class.java)
        query.setParameter("nombreDeLocacionActual", nombreDeLocacionActual)

        return query.resultList.firstOrNull()
    }

    override fun enfermedades(vectorId: Int): List<Especie> {
        val session = HibernateTransaction.currentSession
        val hql = ("select e from Vector v"
                + " inner join v.especies e "
                + "where v.id = :vectorId ")

        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("vectorId", vectorId)

        return query.resultList
    }
}