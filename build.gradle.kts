plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "campo.server"
version = "0.3-SNAPSHOT"

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

    implementation("org.jetbrains.kotlinx:dataframe:1.0.0-Beta2")

    testImplementation(kotlin("test"))
    testImplementation("io.vertx:vertx-junit5:5.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.shadowJar {
    manifest {
        attributes(mapOf("Main-Class" to "campo.server.ScholarshipKt"))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("securityAnalysis", JavaExec::class) {
    group = "verification"
    description = "Run security analysis"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass = "campo.server.security.DatabaseSecurityAnalyzerKt"
}
kotlin {
    jvmToolchain(21)
}