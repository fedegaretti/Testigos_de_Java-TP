package ar.edu.unq.eperdemic.services.runner

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Session

class Neo4JSessionFactoryProvider private constructor() {
    private val driver: Driver

    init {
        val env = System.getenv()
        val url = env.getOrDefault("NEO4J_URL", "bolt://localhost:7687")
        val username = env.getOrDefault("NEO4J_USER", "neo4j")
        val password = env.getOrDefault("NEO4J_PASSWORD", "root")

        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password))
    }

    fun createSession(): Session {
        return this.driver.session()
    }

    companion object {

        private var INSTANCE: Neo4JSessionFactoryProvider? = null

        val instance: Neo4JSessionFactoryProvider
            get() {
                if (INSTANCE == null) {
                    INSTANCE = Neo4JSessionFactoryProvider()
                }
                return INSTANCE!!
            }

        fun destroy() {
            if (INSTANCE != null) {
                INSTANCE!!.driver.close()
            }
            INSTANCE = null
        }
    }
}