package org.gonexar.type

import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType

object RasterType : AbstractSingleColumnStandardBasicType<Raster>(
    BinaryJdbcType.INSTANCE,
    RasterJavaType
) {
    private fun readResolve(): Any = RasterType

    override fun getName(): String? {
        return "raster"
    }
}