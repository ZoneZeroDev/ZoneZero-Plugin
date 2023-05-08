import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("signing")
    id("maven-publish")
    id("org.jetbrains.kotlin.kapt") version "1.8.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "kiinse.me.zonezero.plugin"
version = "1.0.0-alpha.64"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
}

@Suppress("vulnerability")
dependencies {
    implementation("com.vdurmont:semver4j:3.1.0")
    implementation("org.tomlj:tomlj:1.1.0")
    implementation("org.springframework:spring-core:6.0.8")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("io.sentry:sentry:6.18.1")
    implementation("org.apache.httpcomponents:fluent-hc:4.5.14")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    compileOnly("org.spigotmc:spigot-api:1.16.2-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.21")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.21")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.19:3.1.0")
}

kapt {
    useBuildCache = false
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "kiinse.me.zonezero.plugin.ZoneZero"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) } })
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to version)
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}