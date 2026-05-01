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

import java.util.*

class VariableToken(value: String) : CalculationToken(value) {
    override fun equals(other: Any?): Boolean {
        if (other is VariableToken) {
            return super.value == other.value
        }
        return false
    }

    override fun hashCode(): Int = super.value.hashCode()

    override fun mutateStackForCalculation(stack: ArrayDeque<Double?>, variableValues: MutableMap<String?, Double?>) {
        val value = variableValues[value]!!
        stack.push(value)
    }

    override fun mutateStackForInfixTranslation(operatorStack: ArrayDeque<Token>, output: StringBuilder) {
        output.append(this.value).append(" ")
    }
}