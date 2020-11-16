package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.transactions.HibernateTransaction

open class HibernatePatogenoDAO: HibernateDAO<Patogeno>(Patogeno::class.java), PatogenoDAO {

    override fun crear(patogeno: Patogeno): Int {
        guardar(patogeno)
        return patogeno.id
    }

    override fun recuperarATodos(): List<Patogeno> {
            val session = HibernateTransaction.currentSession
            val hql = """
            select p
            from Patogeno p
            order by tipo ASC
            """.trimIndent()
            val query = session.createQuery(hql, Patogeno::class.java)
            return query.resultList
    }

    override fun existePatogenoConTipo(tipo: String): Boolean {
        val session = HibernateTransaction.currentSession
        val hql = """
            select p
            from Patogeno p
            where p.tipo = :tipo
        """.trimIndent()
        val query = session.createQuery(hql, Patogeno::class.java)
        query.setParameter("tipo", tipo)

        return query.resultList.isNotEmpty()
    }

}