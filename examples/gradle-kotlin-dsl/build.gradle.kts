plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.1.4"
}

diktat {
    inputs = files("src/**/*.kt")
}
