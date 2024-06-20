plugins {
    java
}

group = "xyz.wagyourtail.jsmacros"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.jemnetworks.com/snapshots")
    maven("https://jitpack.io")
    maven("https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
}

val jsmacrosExtensionInclude by configurations.creating

dependencies {
    implementation("curse.maven:jsmacros-403185:5339287")

    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha7")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("javassist:javassist:3.12.1.GA")
    implementation("it.unimi.dsi:fastutil:8.5.12")

    implementation("io.github.gaming32:pyjabr:1.0-SNAPSHOT")

    jsmacrosExtensionInclude("io.github.gaming32:pyjabr:1.0-SNAPSHOT") {
        exclude(group = "com.google.guava")
        exclude(group = "org.slf4j")
        exclude(group = "it.unimi.dsi")
    }

    testImplementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // jetbrains annotations
    testImplementation("org.jetbrains:annotations:22.0.0")
}

tasks.processResources {
    filesMatching("jsmacros.ext.pyjabr.json") {
        expand("dependencies" to jsmacrosExtensionInclude.files.joinToString(" ") { it.name })
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

tasks.jar {
    from("LICENSE")
    from (jsmacrosExtensionInclude.files) {
        include("*")
        into("META-INF/jsmacrosdeps")
    }
}


tasks.compileJava {
    options.release = 22
}

tasks.test {
    useJUnitPlatform()
}