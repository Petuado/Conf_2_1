plugins {
    kotlin("jvm") version "1.9.24"
    application
}

repositories { mavenCentral() }

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

kotlin { jvmToolchain(17) }