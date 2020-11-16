package ar.edu.unq.eperdemic.modelo

import javax.persistence.*
import javax.validation.constraints.Max

@Entity
@Table(name = "FACTOR_DE_CONTAGIO")
class FactorDeContagio() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Max(100)
    @Column(name = "factor_de_contagio")
    var factorDeContagio: Int = 0
    var tipo: TipoDeVectorEnum? = null

    constructor(tipo: TipoDeVectorEnum): this() {
        this.tipo = tipo
    }
}