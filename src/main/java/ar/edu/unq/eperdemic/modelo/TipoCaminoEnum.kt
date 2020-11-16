package ar.edu.unq.eperdemic.modelo

enum class TipoCaminoEnum {
    Terrestre, Maritimo, Aereo;

    companion object TipoDeCaminoBuilder {
        fun parseTipo(tipo: String): TipoCaminoEnum? {
            return when(tipo) {
                "Terrestre" -> Terrestre
                "Maritimo" -> Maritimo
                "Aereo" -> Aereo
                else -> null
            }
        }

        fun caminosPosibles(tipoDeVector: TipoDeVectorEnum) : List<TipoCaminoEnum> {
            return when(tipoDeVector) {
                TipoDeVectorEnum.Persona -> listOf(Terrestre,Maritimo)
                TipoDeVectorEnum.Animal -> listOf(Terrestre, Maritimo, Aereo)
                TipoDeVectorEnum.Insecto -> listOf(Terrestre, Aereo)
            }
        }
    }

    fun puedeSerAtravesadoPor(tipoVector: TipoDeVectorEnum) : Boolean {
        return when(this) {
            Terrestre -> listOf(TipoDeVectorEnum.Persona, TipoDeVectorEnum.Insecto, TipoDeVectorEnum.Animal).contains(tipoVector)
            Maritimo -> listOf(TipoDeVectorEnum.Persona, TipoDeVectorEnum.Animal).contains(tipoVector)
            Aereo -> listOf(TipoDeVectorEnum.Insecto, TipoDeVectorEnum.Animal).contains(tipoVector)
        }
    }
}

