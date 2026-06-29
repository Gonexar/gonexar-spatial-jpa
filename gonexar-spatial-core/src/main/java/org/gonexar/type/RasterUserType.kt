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

    // Types.OTHER tells the PostgreSQL JDBC driver this is a custom opaque type (raster).
    // Types.BINARY causes "column is of type raster but expression is of type bytea" on writes.
    override fun getSqlType(): Int = Types.OTHER

    override fun returnedClass(): Class<Raster> =
        Raster::class.java

    override fun equals(x: Raster?, y: Raster?): Boolean {
        if (x === y) return true
        if (x == null || y == null) return false
        return x == y  // delegates to Raster.equals() which compares bytes + srid
    }

    override fun hashCode(x: Raster): Int = x.hashCode()

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
            st.setNull(index, Types.OTHER)
        } else {
            st.setBytes(index, value.bytes)
        }
    }

    override fun deepCopy(value: Raster?): Raster? {
        if (value == null) return null
        return Raster(value.bytes.clone(), value.srid)  // was hardcoding srid=0, losing SRID
    }

    override fun isMutable(): Boolean = true

    override fun disassemble(value: Raster?): Serializable? {
        if (value == null) return null
        return Pair(value.bytes.clone(), value.srid)  // Kotlin Pair is Serializable; preserve both fields
    }

    @Suppress("UNCHECKED_CAST")
    override fun assemble(cached: Serializable?, owner: Any?): Raster? {
        if (cached == null) return null
        val (bytes, srid) = cached as Pair<ByteArray, Int?>
        return Raster(bytes.clone(), srid)
    }

    override fun replace(original: Raster?, target: Raster?, owner: Any?): Raster? =
        deepCopy(original)
}
