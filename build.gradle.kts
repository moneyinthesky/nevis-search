plugins {
    kotlin("jvm") version "2.3.0"
    application
}

group = "com.nevis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val http4kVersion = "5.47.0.0"
val exposedVersion = "0.58.0"
val flywayVersion = "11.3.4"

dependencies {
    implementation(platform("org.http4k:http4k-bom:$http4kVersion"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-client-okhttp")
    implementation("org.http4k:http4k-contract")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.testcontainers:postgresql:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
}

application {
    mainClass.set("com.nevis.MainKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("seedLocalDb") {
    description = "Seed the local database via the REST API"
    group = "application"
    mainClass.set("com.nevis.seed.SeedLocalDbKt")
    classpath = sourceSets["test"].runtimeClasspath
}
