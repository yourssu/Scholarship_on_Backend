plugins {
    kotlin("jvm") version "2.1.21"
}

group = "campo.server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:5.0.1")
    implementation("io.vertx:vertx-web:5.0.1")

    implementation("io.vertx:vertx-mysql-client:5.0.1")
    implementation("io.vertx:vertx-auth-sql-client:5.0.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}