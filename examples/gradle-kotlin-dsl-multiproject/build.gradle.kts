import org.cqfn.diktat.plugin.gradle.DiktatExtension

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.cqfn.diktat.diktat-gradle-plugin")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    apply(plugin = "org.cqfn.diktat.diktat-gradle-plugin")
    configure<DiktatExtension> {
        diktatConfigFile = rootProject.file("diktat-analysis.yml")
        inputs = files("src/**/*.kt")
        debug = true
    }
}
