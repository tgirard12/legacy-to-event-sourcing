
plugins {
    kotlin("jvm") version "1.9.22"
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

group = "tgirard12.ltes"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9")
}

tasks.test {
    useJUnitPlatform()
}
