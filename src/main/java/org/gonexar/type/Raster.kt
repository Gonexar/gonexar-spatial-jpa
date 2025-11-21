package org.gonexar.type

data class Raster(
    // O conteúdo binário (o dado rasterizado em si)
    val bytes: ByteArray,
    // (Opcional) Metadados geográficos essenciais para a interpretação
    val srid: Int? = null
) {
    // Implementações cruciais para o Hibernate (caching e dirty checking)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Raster
        // Compara o conteúdo do array de bytes
        return bytes.contentEquals(other.bytes) && srid == other.srid
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + (srid ?: 0)
        return result
    }
}