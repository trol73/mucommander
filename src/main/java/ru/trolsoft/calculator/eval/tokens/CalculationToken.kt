package ru.trolsoft.calculator.eval.tokens

import java.util.*

abstract class CalculationToken internal constructor(value: String) : Token(value) {
    abstract fun mutateStackForCalculation(stack: ArrayDeque<Double?>, variableValues: MutableMap<String?, Double?>)
}