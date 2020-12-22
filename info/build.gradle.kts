import org.cqfn.diktat.generation.docs.generateAvailableRules
import org.cqfn.diktat.generation.docs.generateCodeStyle
import org.cqfn.diktat.generation.docs.generateFullDoc
import org.cqfn.diktat.generation.docs.generateRulesMapping

tasks.register("generateRulesMapping") {
    doFirst {
        generateRulesMapping()
    }
}

tasks.register("generateFullDoc") {
    doFirst {
        generateFullDoc(file("$rootDir/guide"), "diktat-coding-convention.md")
    }
}

tasks.register("generateAvailableRules") {
    dependsOn("generateRulesMapping")
    doFirst {
        generateAvailableRules(rootDir, file("$rootDir/../wp"))
    }
}

tasks.register("generateCodeStyle") {
    dependsOn("generateFullDoc")
    doFirst {
        generateCodeStyle(file("$rootDir/guide"), file("$rootDir/../wp"))
    }
}

tasks.register("updateDocumentation") {
    dependsOn(
        "generateRulesMapping",
        "generateAvailableRules",
        "generateFullDoc",
        "generateCodeStyle"
    )
}
