package org.gonexar.type

import org.hibernate.service.UnknownUnwrapTypeException
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.AbstractJavaType

object RasterJavaType : AbstractJavaType<Raster>(Raster::class.java) {

    private fun readResolve(): Any = RasterJavaType

    val INSTANCE: RasterJavaType = this

    override fun areEqual(one: Raster?, another: Raster?): Boolean {
        if (one === another) return true
        if (one == null || another == null) return false
        return one.bytes.contentEquals(another.bytes)
    }

    // Em RasterJavaType
    override fun <X : Any?> unwrap(
        value: Raster?,
        type: Class<X?>?,
        options: WrapperOptions?
    ): X? {
        if (value == null) return null

        // Converte Raster -> byte[]
        if (ByteArray::class.java.isAssignableFrom(type)) {
            return value.bytes as X
        }
        // Converte Raster -> Blob (opcional)
        // if (Blob::class.java.isAssignableFrom(type)) { ... }

        return type?.let { throw UnknownUnwrapTypeException(it) }
    }

    override fun <X : Any?> wrap(
        value: X?,
        options: WrapperOptions?
    ): Raster? {
        if (value == null) return null

        // Converte byte[] -> Raster
        if (value is ByteArray) {
            return Raster(value)
        }
        // Converte Blob -> Raster (opcional)
        // if (value is Blob) { return Raster(value.getBytes(1, value.length().toInt())) }

        throw UnknownUnwrapTypeException(value.javaClass)
    }

    override fun extractHashCode(value: Raster?): Int {
        return value?.bytes?.contentHashCode() ?: 0
    }
}