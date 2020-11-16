package org.cqfn.diktat.ruleset.chapter6

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.USELESS_SUPERTYPE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.UselessSupertype
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class UselessSupertypeWarnTest: LintTestBase(::UselessSupertype) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:useless-override"

    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    fun `check simple wrong example`() {
        lintMethod(
                """
                    open class Rectangle {
                        open fun draw() { /* ... */ }
                    }
                    
                    class Square() : Rectangle() {
                        override fun draw() {
                        /**
                         *
                         * hehe
                         */
                            super<Rectangle>.draw()
                        }
                    }
                    
                    class Square2() : Rectangle() {
                        override fun draw() {
                            //hehe
                            /*
                                hehe
                            */
                            super<Rectangle>.draw()
                        }
                    }
                    
                    class Square2() : Rectangle() {
                        override fun draw() {
                            val q = super.draw()
                        }
                    }
                    
                    class A: Runnable {
                        override fun run() {
                        
                        }
                    }
                """.trimMargin(),
                LintError(11,35, ruleId, "${USELESS_SUPERTYPE.warnText()} Rectangle", true),
                LintError(21,35, ruleId, "${USELESS_SUPERTYPE.warnText()} Rectangle", true)
        )
    }

    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    fun `check example with two super`() {
        lintMethod(
                """
                    open class Rectangle {
                        open fun draw() { /* ... */ }
                    }
                    
                    interface KK {
                        fun draw() {}
                        fun kk() {}
                    }
                    
                    class Square2() : Rectangle(), KK {
                        override fun draw() {
                            super<Rectangle>.draw()
                            super<KK>.draw()
                        }
                        
                        private fun goo() {
                            super<KK>.kk()
                        }

                    }
                """.trimMargin(),
                LintError(17,35, ruleId, "${USELESS_SUPERTYPE.warnText()} KK", true)
        )
    }
}
