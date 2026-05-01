package ru.trolsoft.calculator.eval.tokens

import java.util.*

abstract class Token
    internal constructor(
        @JvmField val value: String
    )
{
    abstract fun mutateStackForInfixTranslation(operatorStack: ArrayDeque<Token>, output: StringBuilder)
}