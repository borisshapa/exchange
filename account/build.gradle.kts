plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-server-netty:1.6.8")
    implementation("com.beust:klaxon:5.6")
    implementation("io.ktor:ktor-client-cio:1.6.8")
    implementation("com.beust:klaxon:5.6")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("io.ktor:ktor-server-test-host:1.6.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.8")
}