package ar.edu.unq.eperdemic.modelo

enum class TipoDeVectorEnum() {
    Persona, Animal, Insecto;

    companion object TipoDeVectorBuilder {
        fun parseTipo(tipo: String): TipoDeVectorEnum? {
            return when(tipo) {
                "Persona" -> Persona
                "Animal" -> Animal
                "Insecto" -> Insecto
                else -> null
            }
        }
    }

    fun puedeSerInfectadoPor(tipoVectorInfectado: TipoDeVectorEnum) : Boolean {
        return when(this) {
            Persona -> listOf(Persona, Insecto, Animal).contains(tipoVectorInfectado)
            Animal -> listOf(Insecto).contains(tipoVectorInfectado)
            Insecto -> listOf(Persona, Animal).contains(tipoVectorInfectado)
        }
    }
}

