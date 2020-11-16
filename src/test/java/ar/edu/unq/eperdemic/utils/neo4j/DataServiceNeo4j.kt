package ar.edu.unq.eperdemic.utils.neo4j

import ar.edu.unq.eperdemic.modelo.TipoCaminoEnum
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.Neo4JUbicacionDAO
import ar.edu.unq.eperdemic.utils.DataService

class DataServiceNeo4j : DataService {

    val ubicaciones= crearUbicaciones()
    val dao = Neo4JUbicacionDAO()
    private fun crearUbicaciones() : List<Ubicacion> {
        val nombres = mutableListOf("Entre Rios", "La Pampa", "Catamarca", "Buenos Aires", "Cordoba", "Bariloche", "Quilmes", "Berazategui", "Lanus")
        return nombres.map{u-> Ubicacion(u) }
    }

    override fun crearSetDeDatosIniciales() {
        TransactionRunner.runTrx {
            ubicaciones.forEach { u ->
                dao.crearUbicacion(u)
            }
            dao.conectar("Entre Rios", "La Pampa", TipoCaminoEnum.Terrestre)
            dao.conectar("Buenos Aires", "Cordoba", TipoCaminoEnum.Terrestre)
            dao.conectar("Bariloche", "Cordoba", TipoCaminoEnum.Terrestre)
            dao.conectar("La Pampa", "Quilmes", TipoCaminoEnum.Maritimo)
            dao.conectar("Quilmes", "Berazategui", TipoCaminoEnum.Aereo)
            dao.conectar("Cordoba", "Quilmes", TipoCaminoEnum.Aereo)
            dao.conectar("Quilmes", "Berazategui", TipoCaminoEnum.Terrestre)

        }
    }

    override fun eliminarTodo() {
        TransactionRunner.runTrx {
            dao.clear()
        }
    }

}