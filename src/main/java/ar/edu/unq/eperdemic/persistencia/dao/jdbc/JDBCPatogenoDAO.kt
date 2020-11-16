package ar.edu.unq.eperdemic.persistencia.dao.jdbc

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.jdbc.JDBCConnector.execute
import java.sql.Connection
import java.sql.Statement

class JDBCPatogenoDAO : PatogenoDAO {

    override fun crear(patogeno: Patogeno): Int {
        return execute { conn: Connection ->
            var id = 0
            val ps = conn.prepareStatement("INSERT INTO patogeno (tipo, cantidad_de_especies) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, patogeno.tipo)
            ps.setInt(2, patogeno.cantidadDeEspecies)
            ps.executeUpdate()
            val rs = ps.generatedKeys
            if (rs.next()) {
                id = rs.getInt(1)
            }
            ps.close()
            id
        }
    }

    override fun actualizar(patogeno: Patogeno) {
        execute { conn: Connection ->
            val ps = conn.prepareStatement("UPDATE patogeno SET tipo = ?, cantidad_de_especies = ? WHERE id = ?")
            ps.setString(1, patogeno.tipo)
            ps.setInt(2, patogeno.cantidadDeEspecies)
            patogeno.id.let { ps.setInt(3, it) }
            ps.executeUpdate()
            if (ps.updateCount != 1) {
                throw RuntimeException("No se actualizo el patogeno $patogeno")
            }
            ps.close()
        }
    }

    override fun recuperar(patogenoId: Int): Patogeno {
        return execute { conn: Connection ->
            val ps = conn.prepareStatement("SELECT * FROM patogeno WHERE id = ?")
            ps.setInt(1, patogenoId)
            val resultSet = ps.executeQuery()
            if (!resultSet.next()){
                throw RuntimeException("No existe un patogeno con el id $patogenoId")
            }
            val patogeno = Patogeno(resultSet.getString("tipo"))
            patogeno.id = resultSet.getInt("id")
            patogeno.cantidadDeEspecies = resultSet.getInt("cantidad_de_especies")

            ps.close()
            patogeno
        }
    }

    override fun recuperarATodos(): List<Patogeno> {
        return execute { conn: Connection ->
            val ps = conn.prepareStatement("SELECT * FROM patogeno ORDER BY tipo ASC")
            val resultSet = ps.executeQuery()
            val patogenos = mutableListOf<Patogeno>()
            while (resultSet.next()) {
                var patogeno = Patogeno(resultSet.getString("tipo"))
                patogeno.cantidadDeEspecies = resultSet.getInt("cantidad_de_especies")
                patogeno.id = resultSet.getInt("id")
                patogenos.add(patogeno)
            }
            ps.close()
            patogenos
        }
    }

    override fun existePatogenoConTipo(tipo: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun eliminarPatogenos() {
        return execute { conn: Connection ->
            val st = conn.createStatement()
            st.executeUpdate("TRUNCATE TABLE patogeno")
            st.close()
            null
        }
    }

    init {
        val initializeScript = javaClass.classLoader.getResource("createAll.sql").readText()
        execute {
            val ps = it.prepareStatement(initializeScript)
            ps.execute()
            ps.close()
            null
        }
    }
}