package ar.edu.unq.eperdemic.modelo


import javax.persistence.*

@Entity
class Ubicacion() {
    @Id
    @Column(name = "nombre_ubicacion", nullable = false, unique = true)
    lateinit var nombreUbicacion: String

    @OneToMany(mappedBy = "nombreDeLocacionActual", targetEntity = Vector::class, fetch = FetchType.EAGER)
    var vectores: MutableSet<Vector> = HashSet()

    constructor(nombreUbicacion: String) : this() {
        this.nombreUbicacion = nombreUbicacion
    }

    private fun vectoresTotales(): Int = this.vectores.size

    private fun vectoresInfectados(): Int {
        return this.vectores.count {
            it.estaInfectado()
        }
    }

    private fun especieConMasVectoresInfectados(): String {
        val especies = this.vectores.flatMap { it.especies }

        if (especies.isNotEmpty()) {
            return especies.groupingBy { it.nombre }
                    .eachCount()
                    .maxBy { it.value }!!
                    .key
        }
        return ""
    }

    fun generarReporte(): ReporteDeContagios {
        val totales = vectoresTotales()
        val infectados = vectoresInfectados()
        val especie = especieConMasVectoresInfectados()

        return ReporteDeContagios(totales, infectados, especie)
    }
}