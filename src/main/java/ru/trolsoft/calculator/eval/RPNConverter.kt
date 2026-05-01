package ru.trolsoft.calculator.eval

import ru.trolsoft.calculator.eval.exceptions.UnknownFunctionException
import ru.trolsoft.calculator.eval.exceptions.UnparsableExpressionException
import ru.trolsoft.calculator.eval.tokens.*
import java.util.*

//internal object RPNConverter {

    private fun substituteUnaryOperators(expr: String, operators: MutableMap<String, CustomOperator?>): String {
        val resultBuilder = StringBuilder()
        var whitespaceCount = 0
        for (i in 0..<expr.length) {
            var afterOperator = false
            var afterParantheses = false
            var expressionStart = false
            val c = expr.get(i)
            if (Character.isWhitespace(c)) {
                whitespaceCount++
                resultBuilder.append(c)
                continue
            }
            if (resultBuilder.length == whitespaceCount) {
                expressionStart = true
            }
            // check if last char in the result is an operator
            if (resultBuilder.length > whitespaceCount) {
                if (isOperatorCharacter(resultBuilder.get(resultBuilder.length - 1 - whitespaceCount), operators)) {
                    afterOperator = true
                } else if (resultBuilder.get(resultBuilder.length - 1 - whitespaceCount) == '(') {
                    afterParantheses = true
                }
            }
            when (c) {
                '+' -> if (!resultBuilder.isEmpty() && !afterOperator && !afterParantheses && !expressionStart) {
                    // not an unary plus so append the char
                    resultBuilder.append(c)
                }

                '-' -> if (!resultBuilder.isEmpty() && !afterOperator && !afterParantheses && !expressionStart) {
                    // not unary
                    resultBuilder.append(c)
                } else {
                    // unary so we substitute it
                    resultBuilder.append('\'')
                }

                else -> resultBuilder.append(c)
            }
            whitespaceCount = 0
        }
        return resultBuilder.toString()
    }


    @Throws(UnknownFunctionException::class, UnparsableExpressionException::class)
    fun toRPNExpression(
        infix: String, variables: MutableMap<String?, Double?>,
        customFunctions: MutableMap<String?, CustomFunction?>, operators: MutableMap<String, CustomOperator?>
    ): RPNExpression {
        val tokenizer = Tokenizer(variables.keys, customFunctions, operators)
        val output = StringBuilder(infix.length)
        val operatorStack = ArrayDeque<Token>()
        var tokens: MutableList<Token> = tokenizer.getTokens(substituteUnaryOperators(infix, operators))
        validateRPNExpression(tokens, operators)
        for (token in tokens) {
            token.mutateStackForInfixTranslation(operatorStack, output)
        }
        // all tokens read, put the rest of the operations on the output;
        while (!operatorStack.isEmpty()) {
            output.append(operatorStack.pop().value).append(" ")
        }
        val postfix = output.toString().trim { it <= ' ' }
        tokens = tokenizer.getTokens(postfix)
        return RPNExpression(tokens, postfix, variables)
    }

    @Throws(UnparsableExpressionException::class)
    private fun validateRPNExpression(tokens: MutableList<Token>, operators: MutableMap<String, CustomOperator?>?) {
        for (i in 1..<tokens.size) {
            val t = tokens[i]
            if (tokens[i - 1] is NumberToken) {
                if (t is VariableToken || (t is ParenthesesToken && t.isOpen()) || t is FunctionToken) {
                    throw UnparsableExpressionException("Implicit multiplication is not supported. E.g. always use '2*x' instead of '2x'")
                }
            }
        }
    }

    private fun isOperatorCharacter(c: Char, operators: MutableMap<String, CustomOperator?>): Boolean {
        for (symbol in operators.keys) {
            if (symbol.indexOf(c) != -1) {
                return true
            }
        }
        return false
    }
//}
