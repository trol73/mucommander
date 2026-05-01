package ru.trolsoft.calculator.eval.tokens

import ru.trolsoft.calculator.eval.CustomFunction
import ru.trolsoft.calculator.eval.exceptions.UnknownFunctionException
import ru.trolsoft.calculator.eval.reverseArray
import java.util.*

class FunctionToken(value: String, function: CustomFunction) : CalculationToken(value) {
    val name: String
    val function: CustomFunction

    init {
        try {
            this.name = function.name
            this.function = function
        } catch (_: IllegalArgumentException) {
            throw UnknownFunctionException(value)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is FunctionToken) {
            return this.name == other.name
        }
        return false
    }

    override fun hashCode(): Int = name.hashCode()

    override fun mutateStackForCalculation(stack: ArrayDeque<Double?>, variableValues: MutableMap<String?, Double?>) {
        val args = DoubleArray(function.argc)
        for (i in 0..< function.argc) {
            args[i] = stack.pop()!!
        }
        stack.push(this.function.applyFunction(*reverseArray(args)))
    }

    override fun mutateStackForInfixTranslation(operatorStack: ArrayDeque<Token>, output: StringBuilder) {
        operatorStack.push(this)
    }
}