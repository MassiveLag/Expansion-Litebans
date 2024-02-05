plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
}

group = "nl.nopermission.litebansplaceholders"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi")}
    maven(url = "https://jitpack.io")
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly("me.clip:placeholderapi:2.11.5")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        archiveFileName.set("Expansion-Litebans-v${project.version}.jar")
    }

    build {
        dependsOn(shadowJar)
    }
}
