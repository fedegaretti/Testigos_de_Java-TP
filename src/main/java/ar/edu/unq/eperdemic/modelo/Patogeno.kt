package ar.edu.unq.eperdemic.modelo

import ar.edu.unq.eperdemic.modelo.exceptions.CapacidadDeContagioInvalidaException
import java.io.Serializable
import javax.persistence.*

@Entity
class Patogeno() : Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Int = 0
    @Column(name = "cantidad_de_especies")
    var cantidadDeEspecies: Int = 0
    var defensa: Int = 0
    var letalidad: Int = 0
    @Column(unique = true)
    lateinit var tipo: String

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(name = "patogeno_factor_de_contagio",
            joinColumns = [JoinColumn(name = "patogeno_id")],
            inverseJoinColumns = [JoinColumn(name= "factor_de_contagio_id")])
    var capacidadDeContagio: MutableList<FactorDeContagio> = mutableListOf(FactorDeContagio(TipoDeVectorEnum.Persona),
            FactorDeContagio(TipoDeVectorEnum.Insecto),
            FactorDeContagio(TipoDeVectorEnum.Animal))

    constructor(tipo: String) : this() {
        this.tipo = tipo
    }

    fun crearEspecie(nombreEspecie: String, paisDeOrigen: String) : Especie{
        cantidadDeEspecies++
        return Especie(this, nombreEspecie, paisDeOrigen)
    }

    fun getCapacidadDeContagio(tipoVector: TipoDeVectorEnum) : Int? {
        return this.capacidadDeContagio.find{it.tipo == tipoVector}!!.factorDeContagio
    }

    fun setCapacidadDeContagio(tipoVector: TipoDeVectorEnum, capacidad: Int) {
        if (capacidad > 100 || capacidad < 0) {
            throw CapacidadDeContagioInvalidaException("La capacidad de contagio debe ser menor o igual a 100")
        }
        this.capacidadDeContagio.find { it.tipo == tipoVector }!!.factorDeContagio = capacidad
    }
}