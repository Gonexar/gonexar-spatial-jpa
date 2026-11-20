package com.gonexar.dialect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DialectCapabilitiesTest {

    @Test
    fun `PostGIS supports every capability`() {
        with(DialectCapabilities.POSTGIS) {
            assertThat(listOf(supportsGeography, supportsRaster, supportsTransform, supportsCollect, supportsLineMerge, supportsHausdorff))
                .containsOnly(true)
        }
    }

    @Test
    fun `H2GIS has no geography, raster or Hausdorff support`() {
        with(DialectCapabilities.H2GIS) {
            assertThat(supportsGeography).isFalse()
            assertThat(supportsRaster).isFalse()
            assertThat(supportsHausdorff).isFalse()
            assertThat(supportsTransform).isTrue()
            assertThat(supportsCollect).isTrue()
            assertThat(supportsLineMerge).isTrue()
        }
    }

    @Test
    fun `MySQL spatial supports none of the optional capabilities`() {
        assertThat(DialectCapabilities.MYSQL_SPATIAL)
            .isEqualTo(DialectCapabilities(supportsGeography = false, supportsRaster = false))
    }

    @Test
    fun `unsupported operation exception names the operation and dialect`() {
        val e = UnsupportedSpatialOperationException("ST_Slope", "H2GIS")
        assertThat(e).isInstanceOf(UnsupportedOperationException::class.java)
        assertThat(e.message).contains("'ST_Slope'").contains("'H2GIS'")
    }
}
