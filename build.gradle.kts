plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
}

group = "nl.nopermission.litebansplaceholders"
version = "1.0.4"

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/central") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi")}
    maven(url = "https://jitpack.io")
}
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly("me.clip:placeholderapi:2.11.5")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    shadowJar {
        archiveFileName.set("Expansion-Litebans-v${project.version}.jar")
    }

    build {
        dependsOn(shadowJar)
    }
}
