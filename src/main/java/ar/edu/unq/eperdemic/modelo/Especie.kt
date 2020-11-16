package ar.edu.unq.eperdemic.modelo

import javax.persistence.*
import kotlin.random.Random

@Entity
class Especie() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    lateinit var nombre: String
    @Column(name = "pais_de_origen")
    lateinit var paisDeOrigen: String
    @ManyToOne(cascade = [CascadeType.REFRESH, CascadeType.REMOVE])
    lateinit var patogeno: Patogeno

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "especie_mutacion",
            joinColumns = [JoinColumn(name = "especie_id")],
            inverseJoinColumns = [JoinColumn(name = "mutacion_id")])
    var mutaciones: MutableSet<Mutacion> = HashSet()
    var adn: Float = 0.0F

    constructor(patogeno: Patogeno, nombreEspecie: String, paisDeOrigen: String) : this() {
        this.patogeno = patogeno
        this.nombre = nombreEspecie
        this.paisDeOrigen = paisDeOrigen
    }

    fun getCapacidadDeContagio(tipoVector: TipoDeVectorEnum) : Int? {
        return patogeno.getCapacidadDeContagio(tipoVector)!!.plus(getCapacidadDeContagioDeMutaciones())
    }

    private fun getCapacidadDeContagioDeMutaciones(): Int {
        return mutaciones.sumBy { if (it.atributoAIncrementar == AtributoEnum.CapacidadDeContagio)  it.valorAIncrementar else 0  }
    }

    fun mutar(mutacion: Mutacion) {
        if (mutaciones.containsAll(mutacion.mutacionesRequeridas) && mutacion.adnRequerido <= adn) {
            mutaciones.add(mutacion)
            adn -= mutacion.adnRequerido
        }
    }

    fun esContagioExitoso(tipoVector: TipoDeVectorEnum) : Boolean {
        val factorDeContagioExitoso = getCapacidadDeContagio(tipoVector)!!.plus(Random.nextInt(1, 10))
        return if (factorDeContagioExitoso > 50) {
            Random.nextInt(factorDeContagioExitoso-50, 100) < factorDeContagioExitoso
        } else {
            Random.nextInt(1, 100) < factorDeContagioExitoso
        }
    }
}