import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    kotlin("jvm") version "2.2.0" apply false
    kotlin("plugin.jpa") version "2.2.0" apply false
    id("org.jetbrains.dokka") version "2.3.0" apply false
    id("com.vanniktech.maven.publish") version "0.35.0" apply false
}

group = "com.gonexar"
version = "0.3.0-alpha"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    group = rootProject.group
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    // Publicação no Maven Central (Central Portal) com POM completo, assinatura GPG,
    // jar de fontes e javadoc gerado pelo Dokka.
    //
    // Credenciais e chave são lidas de propriedades do Gradle (em CI, variáveis
    // ORG_GRADLE_PROJECT_*):
    //   mavenCentralUsername / mavenCentralPassword  -> token de usuário do Central Portal
    //   signingInMemoryKey / signingInMemoryKeyPassword -> chave GPG ASCII-armored
    extensions.configure<MavenPublishBaseExtension> {
        configure(
            KotlinJvm(
                javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
                sourcesJar = true,
            )
        )

        // O deployment fica em "Validated" no Central Portal e precisa ser liberado
        // manualmente (Publish) ou com ./gradlew releaseRepository.
        publishToMavenCentral(automaticRelease = false)

        if (providers.gradleProperty("signingInMemoryKey").isPresent) {
            signAllPublications()
        }

        coordinates(rootProject.group.toString(), project.name, rootProject.version.toString())

        pom {
            name.set(project.name)
            description.set(provider { project.description })
            inceptionYear.set("2025")
            url.set("https://github.com/Gonexar/gonexar-spatial-jpa")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("gonexar")
                    name.set("Gonexar")
                    url.set("https://github.com/Gonexar")
                }
            }
            scm {
                url.set("https://github.com/Gonexar/gonexar-spatial-jpa")
                connection.set("scm:git:https://github.com/Gonexar/gonexar-spatial-jpa.git")
                developerConnection.set("scm:git:ssh://git@github.com/Gonexar/gonexar-spatial-jpa.git")
            }
            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/Gonexar/gonexar-spatial-jpa/issues")
            }
        }
    }
}
