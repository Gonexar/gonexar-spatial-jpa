package com.gonexar.dialect.postgis

import com.gonexar.dialect.DialectCapabilities
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.junit.jupiter.api.Test
import java.io.File

class PostgisFunctionContributorTest {

    /** Function names registered by [PostgisFunctionContributor], read from its source. */
    private val registeredNames: List<String> = run {
        val source = File("src/main/kotlin/com/gonexar/dialect/postgis/PostgisFunctionContributor.kt").readLines()
            .filterNot { it.trimStart().startsWith("*") || it.trimStart().startsWith("//") }
            .joinToString("\n")
        Regex("""(?:register\(|registerGeometryFunction\(|registerPattern\()"(\w+)"""")
            .findAll(source).map { it.groupValues[1] }.toList()
    }

    @Test
    fun `declares PostGIS capabilities`() {
        assertThat(PostgisFunctionContributor.capabilities).isEqualTo(DialectCapabilities.POSTGIS)
    }

    @Test
    fun `every contributed function is registered in Hibernate`() {
        val registry = PostgisTestDatabase.emf.unwrap(SessionFactoryImplementor::class.java)
            .queryEngine.sqmFunctionRegistry
        assertThat(registeredNames).hasSizeGreaterThan(40)
        registeredNames.forEach { name ->
            assertThat(registry.findFunctionDescriptor(name)).describedAs(name).isNotNull
        }
    }

    @Test
    fun `every contributed SQL function exists in PostGIS`() {
        // raster_stat_* are Hibernate pattern functions rendered as (ST_SummaryStats(x)).field.
        val sqlNames = registeredNames.filterNot { it.startsWith("raster_stat_") }
        val existing = PostgisTestDatabase.inTransaction { em ->
            @Suppress("UNCHECKED_CAST")
            (em.createNativeQuery("SELECT DISTINCT lower(proname) FROM pg_proc").resultList as List<String>).toSet()
        }
        val missing = sqlNames.filterNot { it.lowercase() in existing }
        assertThat(missing).describedAs("functions registered but not available in PostGIS").isEmpty()
    }
}
