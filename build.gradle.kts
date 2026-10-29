plugins {
    id("net.fabricmc.fabric-loom") version "1.19-SNAPSHOT"
    id("maven-publish")
}

version = property("mod.version") as String
base { archivesName.set(property("mod.id") as String) }
group = property("mod.group") as String

repositories {
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/releases/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java26"))
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft", project.property("minecraft_dependency"))
    filesMatching("fabric.mod.json") {
        expand("version" to project.version, "minecraft" to project.property("minecraft_dependency"))
    }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(25) }

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") { from(components["java"]) }
    }
}

