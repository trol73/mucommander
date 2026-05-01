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

/**
 * Token for parenthesis
 * 
 * @author fas@congrace.de
 */
class ParenthesesToken(value: String) : Token(value) {
    override fun equals(other: Any?): Boolean {
        if (other is ParenthesesToken) {
            return other.value == this.value
        }
        return false
    }

    override fun hashCode(): Int = value.hashCode()

    fun isOpen(): Boolean = value == "(" || value == "[" || value == "{"


    override fun mutateStackForInfixTranslation(operatorStack: ArrayDeque<Token>, output: StringBuilder) {
        if (isOpen()) {
            operatorStack.push(this)
        } else {
            var next: Token?
            while ((operatorStack.peek().also { next = it }) is OperatorToken || next is FunctionToken || (next is ParenthesesToken && !next.isOpen())) {
                output.append(operatorStack.pop().value).append(" ")
            }
            operatorStack.pop()
        }
    }
}