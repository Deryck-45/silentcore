import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {  
    java
    id("fabric-loom") version "1.3.11"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.8")
    mappings("net.fabricmc:yarn:1.21.8+build.6:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.96.0+1.21.8")
    // Add Auto Config & Cloth Config dependencies if you want in-game config later:
    // modImplementation("me.shedaniel.autoconfig:autoconfig-core:4.4.0")
    // modImplementation("me.shedaniel.autoconfig:autoconfig-fabric:4.4.0")
    // modImplementation("me.shedaniel.cloth:cloth-config-fabric:7.6.51")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}