plugins {
    id("net.fabricmc.fabric-loom") version "1.19-SNAPSHOT"
    id("maven-publish")
}

version = property("mod.version") as String
base {
    archivesName.set(property("mod.id") as String)
}

group = property("mod.group") as String

repositories {
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://masa.dy.fi/maven")
    maven("https://maven.fallenbreath.me/releases")
    mavenCentral()
    flatDir { dirs("libs") }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    compileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
    compileOnly("maven.modrinth:sodium:${property("sodium_version")}")
    implementation("maven.modrinth:GcWjdA9I:JX9bESec")
    implementation("maven.modrinth:CdJaAf0y:fEDHtqkn")
}

loom {
    accessWidenerPath = file("src/main/resources/accesswideners/26.3.accesswidener")
}

tasks.processResources {
    inputs.property("id", project.property("mod.id"))
    inputs.property("name", project.property("mod.name"))
    inputs.property("version", project.version)
    inputs.property("minecraft", project.property("minecraft_dependency"))
    val props = mapOf(
        "id" to project.property("mod.id"),
        "name" to project.property("mod.name"),
        "version" to project.version,
        "minecraft" to project.property("minecraft_dependency"),
        "aw_file" to "26.3.accesswidener"
    )
    filesMatching("fabric.mod.json") { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_25") }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}




