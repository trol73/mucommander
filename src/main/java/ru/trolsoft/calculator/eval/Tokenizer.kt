package ru.trolsoft.calculator.eval

import ru.trolsoft.calculator.eval.exceptions.UnknownFunctionException
import ru.trolsoft.calculator.eval.exceptions.UnparsableExpressionException
import ru.trolsoft.calculator.eval.tokens.FunctionSeparatorToken
import ru.trolsoft.calculator.eval.tokens.FunctionToken
import ru.trolsoft.calculator.eval.tokens.NumberToken
import ru.trolsoft.calculator.eval.tokens.OperatorToken
import ru.trolsoft.calculator.eval.tokens.ParenthesesToken
import ru.trolsoft.calculator.eval.tokens.Token
import ru.trolsoft.calculator.eval.tokens.VariableToken
import kotlin.math.abs

internal class Tokenizer(
    private val variableNames: MutableSet<String?>?,
    private val functions: MutableMap<String?, CustomFunction?>,
    private val operators: MutableMap<String, CustomOperator?>
) {
    private fun isVariable(name: String): Boolean =
        variableNames?.contains(name) ?: false

    private fun isFunction(name: String): Boolean
    = functions.containsKey(name)

    private fun isOperatorCharacter(c: Char): Boolean {
        for (symbol in operators.keys) {
            if (symbol.indexOf(c) != -1) {
                return true
            }
        }
        return false
    }

    @Throws(UnparsableExpressionException::class, UnknownFunctionException::class)
    fun getTokens(expression: String): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        val chars = expression.toCharArray()
        var openBraces = 0
        var openCurly = 0
        var openSquare = 0
        // iterate over the chars and fork on different types of input
        var lastToken: Token?
        var i = 0
        while (i < chars.size) {
            val c = chars[i]
            if (c == ' ') {
                i++
                continue
            }
            if (Character.isDigit(c)) {
                val valueBuilder = StringBuilder(1)
                // handle the numbers of the expression
                valueBuilder.append(c)
                var numberLen = 1
                var lastCharExpNotationSeparator =
                    false // needed to determine if a + or - following an e/E is a unary operation
                var expNotationSeparatorOccurred = false // to check if only one E/e notation separator has occurred
                var hexNotationPrefixOccurred = false // to check if only one '0x' notation prefix has occurred
                var binNotationPrefixOccurred = false // to check if only one '0b' notation prefix has occurred
                var octNotationPrefixOccurred = false // to check if only one '0' notation prefix has occurred
                while (i + numberLen < chars.size) {
                    val cc = chars[i + numberLen]
                    if (c == '0' && numberLen == 1) {   // possibly binary, hex or octal notations
                        if (cc == 'x' || cc == 'X') {           // hex
                            hexNotationPrefixOccurred = true
                            valueBuilder.append(cc)
                            numberLen++
                            continue
                        } else if (cc == 'b' || cc == 'B') {    // binary
                            binNotationPrefixOccurred = true
                            valueBuilder.append(cc)
                            numberLen++
                            continue
                        } else if (cc != '.') {                  // octal
                            octNotationPrefixOccurred = true
                        }
                    }
                    if (cc == '.') {
                        if (hexNotationPrefixOccurred || binNotationPrefixOccurred || octNotationPrefixOccurred) {
                            throw UnparsableExpressionException("Unexpected decimal separator")
                        }
                        valueBuilder.append(cc)
                        lastCharExpNotationSeparator = false
                    } else if (Character.isDigit(cc)) {
                        valueBuilder.append(cc)
                        lastCharExpNotationSeparator = false
                    } else if ((cc >= 'a' && cc <= 'f') || (cc >= 'A' && cc <= 'F')) {
                        if (!hexNotationPrefixOccurred && (cc == 'e' || cc == 'E')) {
                            if (expNotationSeparatorOccurred) {
                                throw UnparsableExpressionException("Number can have only one notation separator 'e/E'")
                            }
                            valueBuilder.append(cc)
                            lastCharExpNotationSeparator = true
                            expNotationSeparatorOccurred = true
                        } else if (hexNotationPrefixOccurred) {
                            valueBuilder.append(cc)
                        } else {
                            throw UnparsableExpressionException("Digit expected")
                        }
                    } else if (lastCharExpNotationSeparator && (cc == '-' || cc == '+')) {
                        valueBuilder.append(chars[i + numberLen])
                        lastCharExpNotationSeparator = false
                    } else if (cc != '_') {
                        break // break out of the while loop here, since the number seem finished
                    }
                    numberLen++
                }
                i += numberLen - 1
                lastToken = NumberToken(valueBuilder.toString())
            } else if (Character.isLetter(c) || c == '_') {
                // can be a variable or function
                val nameBuilder = StringBuilder()
                nameBuilder.append(c)
                var offset = 1
                while (i + offset < chars.size && (Character.isLetter(chars[i + offset]) || Character.isDigit(chars[i + offset]) || chars[i + offset] == '_')) {
                    nameBuilder.append(chars[i + offset++])
                }
                val name = nameBuilder.toString()
                if (isVariable(name)) {
                    // a variable
                    i += offset - 1
                    lastToken = VariableToken(name)
                } else if (this.isFunction(name)) {
                    // might be a function
                    i += offset - 1
                    lastToken = FunctionToken(name, functions.get(name)!!)
                } else {
                    // an unknown symbol was encountered
                    throw UnparsableExpressionException(expression, c, i + 1)
                }
            } else if (c == ',') {
                // a function separator, hopefully
                lastToken = FunctionSeparatorToken()
            } else if (isOperatorCharacter(c)) {
                // might be an operation
                val symbolBuilder = StringBuilder()
                symbolBuilder.append(c)
                var offset = 1
                while (chars.size > i + offset && (isOperatorCharacter(chars[i + offset]))
                    && isOperatorStart(symbolBuilder.toString() + chars[i + offset])
                ) {
                    symbolBuilder.append(chars[i + offset])
                    offset++
                }
                val symbol = symbolBuilder.toString()
                if (operators.containsKey(symbol)) {
                    i += offset - 1
                    lastToken = OperatorToken(symbol, operators.get(symbol)!!)
                } else {
                    throw UnparsableExpressionException(expression, c, i + 1)
                }
            } else if (c == '(') {
                openBraces++
                lastToken = ParenthesesToken(c.toString())
            } else if (c == '{') {
                openCurly++
                lastToken = ParenthesesToken(c.toString())
            } else if (c == '[') {
                openSquare++
                lastToken = ParenthesesToken(c.toString())
            } else if (c == ')') {
                openBraces--
                lastToken = ParenthesesToken(c.toString())
            } else if (c == '}') {
                openCurly--
                lastToken = ParenthesesToken(c.toString())
            } else if (c == ']') {
                openSquare--
                lastToken = ParenthesesToken(c.toString())
            } else {
                // an unknown symbol was encountered
                throw UnparsableExpressionException(expression, c, i + 1)
            }
            tokens.add(lastToken)
            i++
        }
        if (openCurly != 0 || (openBraces != 0) or (openSquare != 0)) {
            val errorBuilder = StringBuilder()
            errorBuilder.append("There are ")
            var first = true
            if (openBraces != 0) {
                errorBuilder.append(abs(openBraces)).append(" unmatched parantheses ")
                first = false
            }
            if (openCurly != 0) {
                if (!first) {
                    errorBuilder.append(" and ")
                }
                errorBuilder.append(abs(openCurly)).append(" unmatched curly brackets ")
                first = false
            }
            if (openSquare != 0) {
                if (!first) {
                    errorBuilder.append(" and ")
                }
                errorBuilder.append(abs(openSquare)).append(" unmatched square brackets ")
                first = false
            }
            errorBuilder.append("in expression '").append(expression).append("'")
            throw UnparsableExpressionException(errorBuilder.toString())
        }
        return tokens
    }

    private fun isOperatorStart(op: String): Boolean {
        operators.keys.forEach {
            if (it.startsWith(op)) {
                return true
            }
        }
        return false
    }
}
