plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}

group = "campo.server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:5.0.1")
    implementation("io.vertx:vertx-web:5.0.1")

    implementation("io.vertx:vertx-jdbc-client:5.0.1")
    implementation("io.vertx:vertx-auth-sql-client:5.0.1")
    implementation("org.xerial:sqlite-jdbc:3.50.2.0")
    
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}