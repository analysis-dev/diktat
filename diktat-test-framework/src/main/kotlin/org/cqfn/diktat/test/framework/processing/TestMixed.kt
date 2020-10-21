package org.cqfn.diktat.test.framework.processing

import org.cqfn.diktat.test.framework.common.TestBase
import org.cqfn.diktat.test.framework.config.TestConfig
import org.cqfn.diktat.test.framework.config.TestFrameworkProperties

@Suppress("MISSING_KDOC_TOP_LEVEL", "KDOC_NO_EMPTY_TAGS")  // fixme: add documentation when implementation is done
class TestMixed : TestBase {
    private lateinit var testConfig: TestConfig

    /**
     * @return
     */
    override fun runTest() = true

    /**
     * @param testConfig
     * @param properties
     * @return
     */
    override fun initTestProcessor(testConfig: TestConfig, properties: TestFrameworkProperties): TestMixed {
        this.testConfig = testConfig
        return this
    }
}
