plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.2.0"
}

repositories {
    mavenCentral()
}

diktat {
    inputs = files("src/**/*.kt")
}
