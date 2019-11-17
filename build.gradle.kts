import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    `maven-publish`
    publishing
    id("com.diffplug.gradle.spotless") version "3.26.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.diffplug.gradle.spotless")

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation(kotlin("stdlib"))
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        }
        withType<Test> {
            testLogging {
                exceptionFormat = TestExceptionFormat.FULL
                events("passed", "skipped", "failed")
            }
        }
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri(property("maven-repository").toString())
                credentials {
                    username = project.findProperty("github.user") as String? ?: System.getenv("GITHUB_USER")
                    password = project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
        publications {
            register("gpr", MavenPublication::class) {
                groupId = property("group-id").toString()
                artifactId = project.name
                version = property("version").toString()

                from(components["java"])
            }
        }
    }
}

spotless {
    isEnforceCheck = false

    format("misc") {
        target("**/*.gradle", "**/*.md", "**/.gitignore")

        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target("**/*.kt")
        ktlint().userData(mapOf(
            "disabled_rules" to "import-ordering"
        ))
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
    }
}
