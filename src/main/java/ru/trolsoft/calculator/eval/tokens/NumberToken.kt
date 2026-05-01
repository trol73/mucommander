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
import kotlin.math.pow

/**
 * A [Token] for Numbers
 * 
 * @author fas@congrace.de
 */
class NumberToken(value: String) : CalculationToken(value) {
    private val doubleValue: Double

    init {
        var value = value
        var isHex = false
        var isBin = false
        var isOct = false
        if (value.length > 1 && value[0] == '0') {
            val ch2 = value[1]
            if (ch2 == 'x' || ch2 == 'X') {
                isHex = true
                value = value.substring(2)
            } else if (ch2 == 'b' || ch2 == 'B') {
                isBin = true
                value = value.substring(2)
            } else if (ch2 != '.') {
                isOct = true
                value = value.substring(1)
            }
        }
        if (isHex) {
            if (value.indexOf('_') >= 0) {
                value = value.replace("_", "")
            }
            this.doubleValue = value.toLong(16).toDouble()
        } else if (isBin) {
            if (value.indexOf('_') >= 0) {
                value = value.replace("_", "")
            }
            this.doubleValue = value.toLong(2).toDouble()
        } else if (isOct) {
            if (value.indexOf('_') >= 0) {
                value = value.replace("_", "")
            }
            this.doubleValue = value.toLong(8).toDouble()
        } else if (value.indexOf('E') > 0 || value.indexOf('e') > 0) {
            //scientific notation as requested in EXP-17
            value = value.lowercase(Locale.getDefault())
            val pos = value.indexOf('e')
            val mantissa = value.substring(0, pos).toDouble()
            val exponent = value.substring(pos + 1).toDouble()
            this.doubleValue = mantissa * 10.0.pow(exponent)
        } else {
            if (value.indexOf('_') >= 0) {
                value = value.replace("_", "")
            }
            this.doubleValue = value.toDouble()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is NumberToken) {
            return other.value == this.value
        }
        return false
    }

    override fun hashCode(): Int = value.hashCode()


    override fun mutateStackForCalculation(stack: ArrayDeque<Double?>, variableValues: MutableMap<String?, Double?>) {
        stack.push(this.doubleValue)
    }

    override fun mutateStackForInfixTranslation(operatorStack: ArrayDeque<Token>, output: StringBuilder) {
        output.append(this.value).append(' ')
    }
}