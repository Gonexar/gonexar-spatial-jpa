package org.gonexar.type

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

object RasterUserType : UserType<Raster> {

    val INSTANCE: RasterUserType = this

    override fun getSqlType(): Int =
        Types.BINARY // salva no PostgreSQL como BYTEA

    override fun returnedClass(): Class<Raster> =
        Raster::class.java

    override fun equals(x: Raster?, y: Raster?): Boolean {
        if (x === y) return true
        if (x == null || y == null) return false
        return x.bytes.contentEquals(y.bytes)
    }

    override fun hashCode(x: Raster): Int =
        x.bytes.contentHashCode()

    @Throws(SQLException::class)
    override fun nullSafeGet(
        rs: ResultSet,
        position: Int,
        session: SharedSessionContractImplementor,
        owner: Any?
    ): Raster? {
        val bytes = rs.getBytes(position)
        if (bytes == null) return null

        return Raster(bytes)
    }

    @Throws(SQLException::class)
    override fun nullSafeSet(
        st: PreparedStatement,
        value: Raster?,
        index: Int,
        session: SharedSessionContractImplementor
    ) {
        if (value == null) {
            st.setNull(index, Types.BINARY)
        } else {
            st.setBytes(index, value.bytes)
        }
    }

    override fun deepCopy(value: Raster?): Raster? {
        if (value == null) return null
        return Raster(value.bytes.clone(), 0)
    }

    override fun isMutable(): Boolean = true

    override fun disassemble(value: Raster?): Serializable? {
        return value?.bytes
    }

    override fun assemble(cached: Serializable?, owner: Any?): Raster? {
        if (cached == null) return null
        return Raster((cached as ByteArray).clone())
    }

    override fun replace(original: Raster?, target: Raster?, owner: Any?): Raster? =
        deepCopy(original)
}



