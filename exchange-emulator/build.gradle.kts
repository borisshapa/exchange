plugins {
    kotlin("jvm")
    id("com.palantir.docker") version "0.32.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-server-netty:1.6.8")
    implementation("com.beust:klaxon:5.6")
    implementation("org.slf4j:slf4j-simple:1.7.36")
}

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "ApplicationKt"
        }
    }
}

docker {
    name="${project.name}:${project.version}"
    files("build/libs/exchange-emulator-1.0-SNAPSHOT-all.jar")
}