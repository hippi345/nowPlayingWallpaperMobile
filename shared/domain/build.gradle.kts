plugins {
    kotlin("jvm") version "2.0.21"
}

group = "com.hippi345.nowplayingwallpaper"
version = "0.1.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}

tasks.test {
    useJUnitPlatform()
}
