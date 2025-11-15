package org.gonexar.type

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

/**
 * Hibernate 6 compliant Geometry <-> WKT mapper
 * Essa classe serve para o hibernate entender como salvar e como ler uma geometry no banco de dados
 */
class GeometryUserType : UserType<Geometry> {

    override fun getSqlType(): Int =
        Types.VARCHAR    // armazenamos como WKT puro

    override fun returnedClass(): Class<Geometry> =
        Geometry::class.java

    override fun nullSafeGet(
        rs: ResultSet?,
        position: Int,
        session: SharedSessionContractImplementor?,
        owner: Any?
    ): Geometry? {

        if (rs == null) return null

        val wkt = rs.getString(position) ?: return null

        return try {
            WKTReader().read(wkt)
        } catch (e: Exception) {
            throw SQLException("Erro ao converter WKT para Geometry", e)
        }
    }

    override fun nullSafeSet(
        st: PreparedStatement?,
        value: Geometry?,
        index: Int,
        session: SharedSessionContractImplementor?
    ) {
        if (st == null) return

        if (value == null) {
            st.setNull(index, Types.VARCHAR)
            return
        }

        val wkt = WKTWriter().write(value)

        st.setString(index, wkt)
    }

    override fun deepCopy(value: Geometry?): Geometry? =
        value?.copy()

    override fun isMutable(): Boolean = true

    override fun equals(x: Geometry?, y: Geometry?): Boolean =
        x?.equalsExact(y) ?: (y == null)

    override fun hashCode(x: Geometry?): Int =
        x?.hashCode() ?: 0

    override fun disassemble(value: Geometry?): Serializable? =
        deepCopy(value) as Serializable?

    override fun assemble(cached: Serializable?, owner: Any?): Geometry? =
        deepCopy(cached as Geometry?)

    override fun replace(
        original: Geometry?,
        target: Geometry?,
        owner: Any?
    ): Geometry? = deepCopy(original)
}