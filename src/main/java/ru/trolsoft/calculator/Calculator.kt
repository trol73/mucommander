package ru.trolsoft.calculator

import ru.trolsoft.calculator.eval.CustomOperator
import ru.trolsoft.calculator.eval.ExpressionBuilder
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class Calculator {
    fun calculate(expression: String): Double =
        if (expression.isBlank()) {
            return 0.0
        } else {
            ExpressionBuilder(expression).apply {
                withOperations(OPERATORS)
                withVariable("pi", Math.PI)
                withVariable("e", Math.E)
            }.build().calculate()
        }

    companion object {

        private fun opLL(values: DoubleArray, operation: (Long, Long) -> Long): Double =
            operation(values[0].roundToLong(), values[1].roundToLong()).toDouble()
        private fun opLI(values: DoubleArray, operation: (Long, Int) -> Long): Double =
            operation(values[0].roundToLong(), values[1].roundToInt()).toDouble()

        private val OP_SHL = object : CustomOperator("<<", true, 10, 2) {
            override fun applyOperation(values: DoubleArray) =
                opLI(values) { a: Long, b: Int -> a shl b }
        }

        private val OP_SHR = object : CustomOperator(">>", true, 11, 2) {
            override fun applyOperation(values: DoubleArray) =
                opLI(values) { a: Long, b: Int -> a shr b }
        }

        private val OP_AND = object : CustomOperator("&", true, 8, 2) {
            override fun applyOperation(values: DoubleArray) =
                opLL(values) { a: Long, b: Long -> a and b }
        }

        private val OP_OR = object : CustomOperator("|", true, 6, 2) {
            override fun applyOperation(values: DoubleArray) =
                opLL(values) { a: Long, b: Long -> a or b }
        }

        private val OP_NOT = object : CustomOperator("~", true, 15, 1) {
            override fun applyOperation(values: DoubleArray): Double {
                val arg = values[0].roundToLong()
                val mask = if (arg < 0xff) {
                    0xffL
                } else if (arg < 0xffff) {
                    0xffffL
                } else if (arg < 0xffffffff) {
                    0xffffffffL
                } else {
                    (1L shl 32) - 1L
                }
                return (arg.inv() and mask).toDouble()
            }
        }

        private val OP_XOR = object : CustomOperator("^^", true, 7, 2) {
            override fun applyOperation(values: DoubleArray) =
                opLL(values) { a: Long, b: Long -> a xor b }
        }

        val OPERATORS = listOf(
            OP_SHL, OP_SHR, OP_AND, OP_OR, OP_NOT, OP_XOR
        )
    }

}