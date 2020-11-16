package ar.edu.unq.eperdemic.modelo

import ar.edu.unq.eperdemic.modelo.exceptions.VectorNoInfectadoException
import javax.persistence.*

@Entity
class Vector() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "nombre_de_locacion_actual")
    lateinit var nombreDeLocacionActual: String

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(name = "vector_especie",
            joinColumns = [JoinColumn(name = "vector_id")],
            inverseJoinColumns = [JoinColumn(name = "especie_id")])
    var especies: MutableList<Especie> = mutableListOf()

    @Column(name = "tipo_de_vector", nullable = false)
    lateinit var tipo: TipoDeVectorEnum

    constructor(ubicacion: String) : this() {
        this.nombreDeLocacionActual = ubicacion
    }

    fun infectar(especie: Especie) : Boolean{
        if (especie.esContagioExitoso(this.tipo)) {
            especies.add(especie)
            if(this.tipo == TipoDeVectorEnum.Persona) {
                especie.adn += 0.20F
            }
            return true
        }
        return false
    }

    fun estaInfectado() = this.especies.isNotEmpty()

    fun puedeSerInfectadoPor(tipo: TipoDeVectorEnum) = this.tipo.puedeSerInfectadoPor(tipo)

    fun contagiar(vector: Vector): Boolean {
        var fueContagioExitoso = false
        if (estaInfectado()) {
            if (vector.puedeSerInfectadoPor(tipo)) {
                especies.forEach { especie ->
                    fueContagioExitoso = vector.infectar(especie) || fueContagioExitoso
                }
            }
        } else {
            throw VectorNoInfectadoException("El vector no esta infectado")
        }
        return fueContagioExitoso
    }

    fun puedeMoverse(ubicacion: Ubicacion?) : Boolean = nombreDeLocacionActual != ubicacion!!.nombreUbicacion

    fun moverse(ubicacion: String) {
        this.nombreDeLocacionActual = ubicacion
    }

}



