plugins {
    kotlin("jvm") version "1.9.23"
}

group = "xyz.wagyourtail.jsmacros"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}