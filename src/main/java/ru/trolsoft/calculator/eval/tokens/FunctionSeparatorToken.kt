package ru.trolsoft.calculator.eval.tokens

import java.util.*

class FunctionSeparatorToken : Token(",") {
    override fun mutateStackForInfixTranslation(operatorStack: ArrayDeque<Token>, output: StringBuilder) {
        var token: Token
        while ((operatorStack.peek().also { token = it }) !is ParenthesesToken && token.value != "(") {
            output.append(operatorStack.pop().value).append(" ")
        }
    }
}