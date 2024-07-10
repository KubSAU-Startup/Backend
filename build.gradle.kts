plugins {
    application
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.10"
}

application {
    mainClass.set("com.meloda.kubsau.ApplicationKt")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }
}

group = "com.meloda.kubsau.backend"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.12"

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

    val exposedVersion = "0.51.1"

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    val h2Version = "2.2.224"

    implementation("com.h2database:h2:$h2Version")

    val koinVersion = "3.5.6"

    implementation(platform("io.insert-koin:koin-bom:$koinVersion"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-ktor")
    implementation("io.insert-koin:koin-logger-slf4j")

    val sqliteJdbcVersion = "3.45.3.0"

    implementation("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")

    val postgreVersion = "42.7.3"
    implementation("org.postgresql:postgresql:$postgreVersion")

    val logBackVersion = "1.5.6"

    implementation("ch.qos.logback:logback-classic:$logBackVersion")

    val qrCodeVersion = "4.2.0"

    implementation("io.github.g0dkar:qrcode-kotlin-jvm:$qrCodeVersion")
    implementation("io.github.g0dkar:qrcode-kotlin:$qrCodeVersion")

    implementation("org.mindrot:jbcrypt:0.4")
}

kotlin {
    jvmToolchain(17)
}
