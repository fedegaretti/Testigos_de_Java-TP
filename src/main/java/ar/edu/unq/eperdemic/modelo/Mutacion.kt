package ar.edu.unq.eperdemic.modelo

import javax.persistence.*

@Entity
class Mutacion() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    lateinit var atributoAIncrementar: AtributoEnum
    var adnRequerido: Float = 0.0F
    var valorAIncrementar: Int = 0

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(name = "mutacion_mutaciones_requeridas",
            joinColumns = [JoinColumn(name = "mutacion_id")],
            inverseJoinColumns = [JoinColumn(name = "mutacion_requerida_id")])
    var mutacionesRequeridas: MutableSet<Mutacion> = HashSet()

    constructor(atributo: AtributoEnum, adn: Float, valor: Int) : this() {
        this.atributoAIncrementar = atributo
        this.adnRequerido = adn
        this.valorAIncrementar = valor
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mutacion

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }


}