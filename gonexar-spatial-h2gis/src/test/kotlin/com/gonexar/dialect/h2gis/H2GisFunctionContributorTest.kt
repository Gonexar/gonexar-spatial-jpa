package com.gonexar.dialect.h2gis

import com.gonexar.dialect.DialectCapabilities
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.query.sqm.function.SqmFunctionRegistry
import org.junit.jupiter.api.Test
import java.io.File

class H2GisFunctionContributorTest {

    private val registry: SqmFunctionRegistry =
        H2GisTestDatabase.emf.unwrap(SessionFactoryImplementor::class.java).queryEngine.sqmFunctionRegistry

    /** Function names registered by [H2GisFunctionContributor], read from its source. */
    private val registeredNames: List<String> = run {
        val source = File("src/main/kotlin/com/gonexar/dialect/h2gis/H2GisFunctionContributor.kt").readLines()
            .filterNot { it.trimStart().startsWith("*") || it.trimStart().startsWith("//") }
            .joinToString("\n")
        Regex("""(?:register\(|registerGeometryFunction\()"(\w+)"""").findAll(source).map { it.groupValues[1] }.toList()
    }

    @Test
    fun `declares H2GIS capabilities`() {
        assertThat(H2GisFunctionContributor.capabilities).isEqualTo(DialectCapabilities.H2GIS)
    }

    @Test
    fun `every contributed function is registered in Hibernate`() {
        assertThat(registeredNames).isNotEmpty
        registeredNames.forEach { name ->
            assertThat(registry.findFunctionDescriptor(name)).describedAs(name).isNotNull
        }
    }

    @Test
    fun `every contributed function exists in H2GIS`() {
        val existing = H2GisTestDatabase.inTransaction { em ->
            @Suppress("UNCHECKED_CAST")
            (em.createNativeQuery("SELECT UPPER(ROUTINE_NAME) FROM INFORMATION_SCHEMA.ROUTINES").resultList as List<String>).toSet()
        }
        val missing = registeredNames.filterNot { it.uppercase() in existing }
        assertThat(missing).describedAs("functions registered but not available in H2GIS").isEmpty()
    }
}
