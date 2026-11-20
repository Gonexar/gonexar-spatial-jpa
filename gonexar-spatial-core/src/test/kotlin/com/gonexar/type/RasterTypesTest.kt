package com.gonexar.type

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.service.UnknownUnwrapTypeException
import org.junit.jupiter.api.Test
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class RasterTest {

    @Test
    fun `equality compares byte content and srid`() {
        val a = Raster(byteArrayOf(1, 2, 3), 4326)
        val b = Raster(byteArrayOf(1, 2, 3), 4326)

        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a).isNotEqualTo(Raster(byteArrayOf(1, 2, 3), 3857))
        assertThat(a).isNotEqualTo(Raster(byteArrayOf(1, 2, 4), 4326))
        assertThat(a).isNotEqualTo("not a raster")
        assertThat(Raster(byteArrayOf(1)).hashCode()).isEqualTo(Raster(byteArrayOf(1), null).hashCode())
    }
}

class RasterJavaTypeTest {

    private val raster = Raster(byteArrayOf(9, 8, 7), 4326)

    @Test
    fun `unwraps to bytes and wraps bytes back`() {
        assertThat(RasterJavaType.unwrap(raster, ByteArray::class.java as Class<ByteArray?>, null))
            .isEqualTo(byteArrayOf(9, 8, 7))
        assertThat(RasterJavaType.wrap(byteArrayOf(9, 8, 7), null)).isEqualTo(Raster(byteArrayOf(9, 8, 7)))
    }

    @Test
    fun `null values pass through`() {
        assertThat(RasterJavaType.unwrap(null, ByteArray::class.java as Class<ByteArray?>, null)).isNull()
        assertThat(RasterJavaType.wrap<ByteArray>(null, null)).isNull()
        assertThat(RasterJavaType.extractHashCode(null)).isZero()
    }

    @Test
    fun `unsupported conversions are rejected`() {
        assertThatThrownBy { RasterJavaType.unwrap(raster, String::class.java as Class<String?>, null) }
            .isInstanceOf(UnknownUnwrapTypeException::class.java)
        assertThatThrownBy { RasterJavaType.wrap("text", null) }
            .isInstanceOf(UnknownUnwrapTypeException::class.java)
    }

    @Test
    fun `equality delegates to raster content`() {
        assertThat(RasterJavaType.areEqual(raster, Raster(byteArrayOf(9, 8, 7), 4326))).isTrue()
        assertThat(RasterJavaType.areEqual(raster, raster)).isTrue()
        assertThat(RasterJavaType.areEqual(raster, null)).isFalse()
        assertThat(RasterJavaType.areEqual(null, null)).isTrue()
        assertThat(RasterJavaType.extractHashCode(raster)).isEqualTo(raster.hashCode())
        assertThat(RasterJavaType.INSTANCE).isSameAs(RasterJavaType)
    }
}

class RasterUserTypeTest {

    private val session = mockk<SharedSessionContractImplementor>()

    @Test
    fun `describes a raster mapped to Types OTHER`() {
        assertThat(RasterUserType.sqlType).isEqualTo(Types.OTHER)
        assertThat(RasterUserType.returnedClass()).isEqualTo(Raster::class.java)
        assertThat(RasterUserType.isMutable).isTrue()
        assertThat(RasterUserType.INSTANCE).isSameAs(RasterUserType)
    }

    @Test
    fun `reads bytes from the result set`() {
        val rs = mockk<ResultSet>()
        every { rs.getBytes(1) } returns byteArrayOf(1, 2)
        every { rs.getBytes(2) } returns null

        assertThat(RasterUserType.nullSafeGet(rs, 1, session, null)).isEqualTo(Raster(byteArrayOf(1, 2)))
        assertThat(RasterUserType.nullSafeGet(rs, 2, session, null)).isNull()
    }

    @Test
    fun `binds null as OTHER and values as bytes`() {
        val st = mockk<PreparedStatement>(relaxed = true)

        RasterUserType.nullSafeSet(st, null, 1, session)
        RasterUserType.nullSafeSet(st, Raster(byteArrayOf(5)), 2, session)

        verify { st.setNull(1, Types.OTHER) }
        verify { st.setBytes(2, byteArrayOf(5)) }
    }

    @Test
    fun `copies are deep and keep the srid`() {
        val original = Raster(byteArrayOf(1, 2, 3), 4326)

        val copy = RasterUserType.deepCopy(original)!!
        assertThat(copy).isEqualTo(original)
        assertThat(copy.bytes).isNotSameAs(original.bytes)
        assertThat(RasterUserType.deepCopy(null)).isNull()
        assertThat(RasterUserType.replace(original, null, null)).isEqualTo(original)
    }

    @Test
    fun `disassemble and assemble round trip through the cache`() {
        val original = Raster(byteArrayOf(4, 5), 3857)

        val cached = RasterUserType.disassemble(original)
        assertThat(RasterUserType.assemble(cached, null)).isEqualTo(original)
        assertThat(RasterUserType.disassemble(null)).isNull()
        assertThat(RasterUserType.assemble(null, null)).isNull()
    }

    @Test
    fun `equality and hash code follow raster content`() {
        val a = Raster(byteArrayOf(1))
        assertThat(RasterUserType.equals(a, Raster(byteArrayOf(1)))).isTrue()
        assertThat(RasterUserType.equals(a, a)).isTrue()
        assertThat(RasterUserType.equals(a, null)).isFalse()
        assertThat(RasterUserType.hashCode(a)).isEqualTo(a.hashCode())
    }
}
