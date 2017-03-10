package de.congrace.exp4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Tokenizer {

	private final Set<String> variableNames;

	private final Map<String, CustomFunction> functions;

	private final Map<String, CustomOperator> operators;

	Tokenizer(Set<String> variableNames, Map<String, CustomFunction> functions, Map<String, CustomOperator> operators) {
		super();
		this.variableNames = variableNames;
		this.functions = functions;
		this.operators = operators;
	}

	private boolean isVariable(String name) {
		if (variableNames != null) {
			for (String var : variableNames) {
				if (name.equals(var)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isFunction(String name) {
		return functions.containsKey(name);
	}

	private boolean isOperatorCharacter(char c) {
		for (String symbol : operators.keySet()) {
			if (symbol.indexOf(c) != -1) {
				return true;
			}
		}
		return false;
	}
	
	List<Token> getTokens(final String expression) throws UnparsableExpressionException, UnknownFunctionException {
		final List<Token> tokens = new ArrayList<>();
		final char[] chars = expression.toCharArray();
		int openBraces = 0;
		int openCurly = 0;
		int openSquare = 0;
		// iterate over the chars and fork on different types of input
		Token lastToken;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == ' ') {
                continue;
            }
			if (Character.isDigit(c)) {
				final StringBuilder valueBuilder = new StringBuilder(1);
				// handle the numbers of the expression
				valueBuilder.append(c);
				int numberLen = 1;
				boolean lastCharExpNotationSeparator = false; // needed to determine if a + or - following an e/E is a unary operation
				boolean expNotationSeparatorOccurred = false; // to check if only one E/e notation separator has occurred
                boolean hexNotationPrefixOccurred = false;    // to check if only one '0x' notation prefix has occurred
                boolean binNotationPrefixOccurred = false;    // to check if only one '0b' notation prefix has occurred
                boolean octNotationPrefixOccurred = false;    // to check if only one '0' notation prefix has occurred
				while (i + numberLen < chars.length) {
                    char cc = chars[i + numberLen];
                    if (c == '0' && numberLen == 1) {   // possibly binary, hex or octal notations
                        if (cc == 'x' || cc == 'X') {           // hex
                            hexNotationPrefixOccurred = true;
                            valueBuilder.append(cc);
                            numberLen++;
                            continue;
                        } else if (cc == 'b' || cc == 'B') {    // binary
							binNotationPrefixOccurred = true;
							valueBuilder.append(cc);
							numberLen++;
							continue;
                        } else if (cc != '.') {                  // octal
                            octNotationPrefixOccurred = true;
                        }
                    }
                    if (cc == '.') {
                        if (hexNotationPrefixOccurred || binNotationPrefixOccurred || octNotationPrefixOccurred) {
                            throw new UnparsableExpressionException("Unexpected decimal separator");
                        }
                        valueBuilder.append(cc);
                        lastCharExpNotationSeparator = false;
                    } else if (Character.isDigit(cc)) {
                        valueBuilder.append(cc);
                        lastCharExpNotationSeparator = false;
                    } else if ((cc >= 'a' && cc <= 'f') || (cc >= 'A' && cc <= 'F')) {
                        if (!hexNotationPrefixOccurred && (cc == 'e' || cc == 'E')) {
                            if (expNotationSeparatorOccurred){
                                throw new UnparsableExpressionException("Number can have only one notation separator 'e/E'");
                            }
                            valueBuilder.append(cc);
                            lastCharExpNotationSeparator = true;
                            expNotationSeparatorOccurred = true;
                        } else if (hexNotationPrefixOccurred) {
                            valueBuilder.append(cc);
                        } else {
                            throw new UnparsableExpressionException("Digit expected");
                        }
					} else if (lastCharExpNotationSeparator && (cc == '-' || cc == '+')) {
						valueBuilder.append(chars[i + numberLen]);
						lastCharExpNotationSeparator = false;
					} else if (cc != '_') {
						break; // break out of the while loop here, since the number seem finished
					}
					numberLen++;
				}
				i += numberLen - 1;
				lastToken = new NumberToken(valueBuilder.toString());
			} else if (Character.isLetter(c) || c == '_') {
				// can be a variable or function
				final StringBuilder nameBuilder = new StringBuilder();
				nameBuilder.append(c);
				int offset = 1;
				while (i + offset < chars.length && (Character.isLetter(chars[i + offset]) || Character.isDigit(chars[i + offset]) || chars[i + offset] == '_')) {
					nameBuilder.append(chars[i + offset++]);
				}
				String name = nameBuilder.toString();
				if (isVariable(name)) {
					// a variable
					i += offset - 1;
					lastToken = new VariableToken(name);
				} else if (this.isFunction(name)) {
					// might be a function
					i += offset - 1;
					lastToken = new FunctionToken(name, functions.get(name));
				} else {
					// an unknown symbol was encountered
					throw new UnparsableExpressionException(expression, c, i + 1);
				}
			} else if (c == ',') {
				// a function separator, hopefully
				lastToken = new FunctionSeparatorToken();
			} else if (isOperatorCharacter(c)) {
				// might be an operation
				StringBuilder symbolBuilder = new StringBuilder();
				symbolBuilder.append(c);
				int offset = 1;
				while (chars.length > i + offset && (isOperatorCharacter(chars[i + offset]))
						&& isOperatorStart(symbolBuilder.toString() + chars[i + offset])) {
					symbolBuilder.append(chars[i + offset]);
					offset++;
				}
				String symbol = symbolBuilder.toString();
				if (operators.containsKey(symbol)) {
					i += offset - 1;
					lastToken = new OperatorToken(symbol, operators.get(symbol));
				} else {
					throw new UnparsableExpressionException(expression,  c, i + 1);
				}
			} else if (c == '('){
				openBraces++;
				lastToken = new ParenthesesToken(String.valueOf(c));
			} else if (c == '{'){
				openCurly++;
				lastToken = new ParenthesesToken(String.valueOf(c));
			} else if( c == '['){
				openSquare++;
				lastToken = new ParenthesesToken(String.valueOf(c));
			} else if ( c == ')'){
				openBraces--;
				lastToken = new ParenthesesToken(String.valueOf(c));
			} else if ( c == '}'){
				openCurly--;
				lastToken = new ParenthesesToken(String.valueOf(c));
			} else if ( c == ']'){
				openSquare--;
				lastToken = new ParenthesesToken(String.valueOf(c));
			} else {
				// an unknown symbol was encountered
				throw new UnparsableExpressionException(expression, c, i + 1);
			}
			tokens.add(lastToken);
		}
		if (openCurly != 0 || openBraces != 0 | openSquare != 0){
			StringBuilder errorBuilder = new StringBuilder();
			errorBuilder.append("There are ");
			boolean first = true;
			if (openBraces != 0) {
				errorBuilder.append(Math.abs(openBraces)).append(" unmatched parantheses ");
				first = false;
			}
			if (openCurly != 0) {
				if (!first){
					errorBuilder.append(" and ");
				}
				errorBuilder.append(Math.abs(openCurly)).append(" unmatched curly brackets ");
				first = false;
			}
			if (openSquare != 0){
				if (!first){
					errorBuilder.append(" and ");
				}
				errorBuilder.append(Math.abs(openSquare)).append(" unmatched square brackets ");
				first = false;
			}
			errorBuilder.append("in expression '").append(expression).append("'");
			throw new UnparsableExpressionException(errorBuilder.toString());
		}
		return tokens;

	}

	private boolean isOperatorStart(String op) {
		for (String operatorName : operators.keySet()) {
			if (operatorName.startsWith(op)) {
				return true;
			}
		}
		return false;
	}

}
