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
package de.congrace.exp4j;

import java.util.Map;
import java.util.Stack;

/**
 * A {@link Token} for Numbers
 * 
 * @author fas@congrace.de
 * 
 */
class NumberToken extends CalculationToken {

	private final double doubleValue;

	/**
	 * construct a new {@link NumberToken}
	 * 
	 * @param value
	 *            the value of the number as a {@link String}
	 */
	NumberToken(String value) {
		super(value);
        boolean isHex = false;
        boolean isBin = false;
        boolean isOct = false;
        if (value.length() > 1 && value.charAt(0) == '0') {
            char ch2 = value.charAt(1);
            if (ch2 == 'x' || ch2 == 'X') {
                isHex = true;
                value = value.substring(2);
            } else if (ch2 == 'b' || ch2 == 'B') {
                isBin = true;
                value = value.substring(2);
            } else if (ch2 != '.' ) {
                isOct = true;
                value = value.substring(1);
            }
        }
        if (isHex) {
            this.doubleValue = Long.parseLong(value, 16);
        } else if (isBin) {
            this.doubleValue = Long.parseLong(value, 2);
        } else if (isOct) {
            this.doubleValue = Long.parseLong(value, 8);
        } else if (value.indexOf('E') > 0 || value.indexOf('e') > 0){
			//scientific notation as requested in EXP-17
			value = value.toLowerCase();
			int pos = value.indexOf('e');
			double mantissa = Double.parseDouble(value.substring(0,pos));
			double exponent = Double.parseDouble(value.substring(pos + 1));
			this.doubleValue = mantissa * Math.pow(10, exponent);
		} else {
			this.doubleValue = Double.parseDouble(value);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NumberToken) {
			final NumberToken t = (NumberToken) obj;
			return t.getValue().equals(this.getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	void mutateStackForCalculation(Stack<Double> stack, Map<String, Double> variableValues) {
		stack.push(this.doubleValue);
	}

	@Override
	void mutateStackForInfixTranslation(Stack<Token> operatorStack, StringBuilder output) {
		output.append(this.getValue()).append(' ');
	}
}