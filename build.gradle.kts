plugins {
    application
    kotlin("jvm") version "1.9.0"
    id("io.ktor.plugin") version "2.3.7"
}

application {
    mainClass.set("com.meloda.kubsau.ApplicationKt")
}

group = "com.meloda.kubsau.backend"
version = "0.2.1"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.10"

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")

    val exposedVersion = "0.49.0"

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    val h2Version = "2.2.224"

    implementation("com.h2database:h2:$h2Version")

    val koinVersion = "3.5.3"

    implementation(platform("io.insert-koin:koin-bom:$koinVersion"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-ktor")
    implementation("io.insert-koin:koin-logger-slf4j")

    val sqliteJdbcVersion = "3.45.3.0"

    implementation("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")

    val logBackVersion = "1.5.6"

    implementation("ch.qos.logback:logback-classic:$logBackVersion")

    val kotlinxHtmlVersion = "0.11.0"

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")
}

kotlin {
    jvmToolchain(17)
}
