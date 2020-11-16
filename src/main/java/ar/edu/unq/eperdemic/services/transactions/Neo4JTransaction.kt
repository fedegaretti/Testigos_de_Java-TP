package ar.edu.unq.eperdemic.services.transactions

import ar.edu.unq.eperdemic.services.runner.Neo4JSessionFactoryProvider

class Neo4JTransaction : Transaction {
    companion object {
        private var session: org.neo4j.driver.Session? = null
        var transaction: org.neo4j.driver.Transaction? = null
    }

    override fun start() {
        session = Neo4JSessionFactoryProvider.instance.createSession()
        transaction = session?.beginTransaction()
    }

    override fun commit() {
        transaction?.commit()
    }

    override fun rollback() {
        transaction?.rollback()
    }

    override fun close() {
        session?.close()
        transaction = null
    }
}