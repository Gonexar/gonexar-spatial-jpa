package org.gonexar.type

interface RasterInterface {
    val bytes: ByteArray
}

data class Raster(
    override val bytes: ByteArray
) : RasterInterface {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Raster

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}