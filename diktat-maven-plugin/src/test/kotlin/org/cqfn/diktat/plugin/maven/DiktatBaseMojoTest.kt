package org.cqfn.diktat.plugin.maven

import org.apache.maven.plugin.testing.MojoRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

/**
 * Tests for mojo configuration
 *
 * FixMe: `@Parameter` properties are not initialized with default values
 */
@OptIn(ExperimentalPathApi::class)
class DiktatBaseMojoTest {
    @get:Rule
    val mojoRule = MojoRule()

    @Test
    fun `test plugin configuration`() {
        val pom = createTempFile()
        pom.writeText(
            """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    
                    <groupId>org.cqfn.diktat</groupId>
                    <artifactId>diktat-test</artifactId>
                    <version>0.1.6-SNAPSHOT</version>
                    
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.cqfn.diktat</groupId>
                                <artifactId>diktat-maven-plugin</artifactId>
                                <configuration>
                                    <diktatConfigFile>diktat-analysis.yml</diktatConfigFile>
                                </configuration>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>check</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
            """.trimIndent()
        )
        val diktatCheckMojo = mojoRule.lookupMojo("check", pom.toFile()) as DiktatCheckMojo
        Assert.assertEquals(false, diktatCheckMojo.debug)
        Assert.assertEquals("diktat-analysis.yml", diktatCheckMojo.diktatConfigFile)
    }
}
