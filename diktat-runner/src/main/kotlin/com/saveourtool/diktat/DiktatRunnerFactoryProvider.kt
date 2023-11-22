/**
 * Contains only initialized [com.saveourtool.diktat.DiktatRunnerFactory]
 */

package com.saveourtool.diktat

import com.saveourtool.diktat.ktlint.DiktatBaselineFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatProcessorFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatReporterFactoryImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleConfigReaderImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import generated.KTLINT_VERSION

/**
 * Info about engine
 */
const val ENGINE_INFO: String = "Ktlint: $KTLINT_VERSION"

/**
 * @return initialized [DiktatRunnerFactory]
 */
val diktatRunnerFactory: DiktatRunnerFactory = DiktatRunnerFactory(
    DiktatRuleConfigReaderImpl(),
    DiktatRuleSetFactoryImpl(),
    DiktatProcessorFactoryImpl(),
    DiktatBaselineFactoryImpl(),
    DiktatReporterFactoryImpl(),
)
