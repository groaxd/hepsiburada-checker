plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "tr.groax"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-io:commons-io:2.15.0")
    implementation("org.jsoup", "jsoup", "1.14.3")
    implementation("org.seleniumhq.selenium", "selenium-api", "4.15.0")
    implementation("org.seleniumhq.selenium", "selenium-support", "4.15.0")
    implementation("org.seleniumhq.selenium", "selenium-chrome-driver", "4.15.0")
    implementation("org.apache.commons", "commons-lang3", "3.14.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("tr.groax.CheckerKt")
}