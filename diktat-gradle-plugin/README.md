## Building
Build of gradle plugin is performed by gradle, but is wrapped in maven build. The module's `pom.xml` isn't exactly accurate
and doesn't include gradle-specific dependencies, that are automatically provided by gradle when applying the plugin.

To avoid versions duplication, diktat and ktlint versions are passed to gradle via properties when running gradle task from maven.
These versions are then written in a file and then included in the plugin jar to determine dependencies for JavaExec.

Gradle plugin marker pom, which is normally produced by `java-gradle-plugin` plugin during gradle build,
is added manually as a maven module.

Build is skipped by default on windows to make things easier; if you want to build it, you should change
the property `gradle.executable` to `gradlew.bat` and the property `skip.gradle.build` to `true`.