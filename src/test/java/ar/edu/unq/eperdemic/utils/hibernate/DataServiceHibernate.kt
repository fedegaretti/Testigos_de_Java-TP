package ar.edu.unq.eperdemic.utils.hibernate

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.utils.DataService

class DataServiceHibernate : DataService {
    val hibernateDao = HibernateDataDAO()

    override fun crearSetDeDatosIniciales() {
        val patogenos = crearPatogenos()
        val ubicaciones = crearUbicaciones()
        val vectores = crearVectores()
        val especies = crearEspecies(patogenos)
        val mutaciones = crearMutaciones()
        vectores[0].especies.addAll(especies)

        createDataSet(patogenos, ubicaciones, vectores, mutaciones)
    }
    private fun crearMutaciones() : List<Mutacion> {
        return mutableListOf(Mutacion(AtributoEnum.Defensa, 0f, 10),
                Mutacion(AtributoEnum.CapacidadDeContagio, 5f, 10),
                Mutacion(AtributoEnum.Letalidad, 5f, 10))

    }

    private fun crearPatogenos() : List<Patogeno> {
        val virus = Patogeno("Virus")
        val bacteria = Patogeno("Bacteria")
        virus.setCapacidadDeContagio(TipoDeVectorEnum.Persona,100)
        virus.setCapacidadDeContagio(TipoDeVectorEnum.Insecto, 100)
        virus.setCapacidadDeContagio(TipoDeVectorEnum.Animal, 100)
        bacteria.setCapacidadDeContagio(TipoDeVectorEnum.Persona,100)
        return listOf(virus, bacteria)
    }

    private fun crearUbicaciones() : List<Ubicacion> {
        val nombres = mutableListOf("Entre Rios", "La Pampa", "Catamarca", "Buenos Aires", "Cordoba", "Bariloche", "Quilmes", "Berazategui", "Lanus")
        return nombres.map{u-> Ubicacion(u)}
    }

    private fun crearVectores() : List<Vector> {
        return listOf(
                VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Persona,"Buenos Aires")
                        .aModelo(),
                VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Animal, "Cordoba")
                        .aModelo(),
                VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Insecto,"Bariloche")
                        .aModelo(),
                VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Persona,"Cordoba")
                        .aModelo(),
                VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Persona,"Bariloche")
                .aModelo(),
                VectorFrontendDTO(VectorFrontendDTO.TipoDeVector.Persona,"La Pampa")
                        .aModelo()
        )
    }

    private fun crearEspecies(patogenos : List<Patogeno>) : List<Especie> {
        val virus = patogenos[0]
        val bacteria = patogenos[1]
        return listOf(virus.crearEspecie("Gripe","China"),
                virus.crearEspecie("H1N1", "Uruguay"),
                virus.crearEspecie("Paperas", "Yugoslavia"),
                virus.crearEspecie("Coronavirus","China"),
                bacteria.crearEspecie("Angina", "Argentina"))
    }

    private fun createDataSet(patogenos: List<Patogeno>, ubicaciones: List<Ubicacion>, vectores: List<Vector>, mutaciones: List<Mutacion>) {
        TransactionRunner.runTrx {
            patogenos.forEach {
                patogeno -> hibernateDao.create(patogeno)
            }

            ubicaciones.forEach{
                ubicacion -> hibernateDao.create(ubicacion)

            }

            vectores.forEach {
                vector ->
                hibernateDao.create(vector)
            }
            mutaciones.forEach {
                mutacion -> hibernateDao.create(mutacion)
            }
        }
    }

    override fun eliminarTodo() {
        TransactionRunner.runTrx {
            hibernateDao.clear()
        }
    }
}