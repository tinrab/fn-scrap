import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    `maven-publish`
    publishing
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
                    username = project.findProperty("github-user") as String? ?: System.getenv("GITHUB_USER")
                    password = project.findProperty("github-token") as String? ?: System.getenv("GITHUB_TOKEN")
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
