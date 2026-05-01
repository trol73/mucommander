/*
   Copyright 2011 frank asseg

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package ru.trolsoft.calculator.eval.tokens

import ru.trolsoft.calculator.eval.CustomOperator
import java.util.*

/**
 * [Token] for Operations like +,-,*,/,% and ^
 * 
 * @author fas@congrace.de
 */
class OperatorToken(
    value: String,
    var operation: CustomOperator
) : CalculationToken(value) {
    fun applyOperation(vararg values: Double): Double =
        operation.applyOperation(values)

    override fun equals(other: Any?): Boolean {
        if (other is OperatorToken) {
            return other.value == this.value
        }
        return false
    }

    override fun hashCode(): Int = value.hashCode()

    override fun mutateStackForCalculation(stack: ArrayDeque<Double?>, variableValues: MutableMap<String?, Double?>) {
        val operands = DoubleArray(operation.operandCount)
        for (i in 0..< operation.operandCount) {
            operands[operation.operandCount - i - 1] = stack.pop()!!
        }
        stack.push(operation.applyOperation(operands))
    }

    override fun mutateStackForInfixTranslation(operatorStack: ArrayDeque<Token>, output: StringBuilder) {
        while (!operatorStack.isEmpty()) {
            val before = operatorStack.peek()
            if (before is FunctionToken) {
                operatorStack.pop()
                output.append(before.value).append(" ")
            } else if (before is OperatorToken) {
                if (isLeftAssociative() && precedence() <= before.precedence()) {
                    output.append(operatorStack.pop().value).append(" ")
                } else if (!isLeftAssociative() && precedence() < before.precedence()) {
                    output.append(operatorStack.pop().value).append(" ")
                } else {
                    break
                }
            } else {
                break
            }
        }
        operatorStack.push(this)
    }

    private fun isLeftAssociative(): Boolean = operation.leftAssociative
    private fun precedence(): Int = operation.precedence
}