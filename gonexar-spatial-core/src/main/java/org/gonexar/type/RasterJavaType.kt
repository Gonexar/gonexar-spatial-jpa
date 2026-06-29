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
        return one == another  // delegates to Raster.equals() which compares bytes + srid
    }

    override fun <X : Any?> unwrap(
        value: Raster?,
        type: Class<X?>?,
        options: WrapperOptions?
    ): X? {
        if (value == null) return null
        if (type == ByteArray::class.java) {
            @Suppress("UNCHECKED_CAST")
            return value.bytes as X?
        }
        throw UnknownUnwrapTypeException(type ?: return null)
    }

    override fun <X : Any?> wrap(
        value: X?,
        options: WrapperOptions?
    ): Raster? {
        if (value == null) return null
        if (value is ByteArray) return Raster(value)
        throw UnknownUnwrapTypeException(value.javaClass)
    }

    override fun extractHashCode(value: Raster?): Int = value?.hashCode() ?: 0
}
