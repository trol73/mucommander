package ru.trolsoft.calculator.eval;

import ru.trolsoft.calculator.eval.exceptions.InvalidCustomFunctionException;

/**
 * This classed is used to create custom functions for exp4j<br>
 *
 * <b>Example</b><br>
 * <pre>{@code
 * CustomFunction fooFunc = new CustomFunction("foo") {
 * 		public double applyFunction(double value) {
 * 			return value*Math.E;
 * 		}
 * };
 * double varX=12d;
 * Calculable calc = new ExpressionBuilder("foo(x)").withCustomFunction(fooFunc).withVariable("x",varX).build();
 * assertTrue(calc.calculate() == Math.E * varX);
 * }</pre>
 * 
 * @author frank asseg
 * 
 */
public abstract class CustomFunction {
	public final int argc;
	public final String name;

	/**
	 * create a new single value input CustomFunction with a set name
	 * 
	 * @param name the name of the function (e.g. foo)
	 */
	CustomFunction(String name) throws InvalidCustomFunctionException {
		this.argc = 1;
		this.name = name;
		int firstChar = name.charAt(0);
		if ((firstChar < 'A' || firstChar > 'Z') && (firstChar < 'a' || firstChar > 'z')) {
			throw new InvalidCustomFunctionException("functions have to start with a lowercase or uppercase character");
		}
	}

	/**
	 * create a new single value input CustomFunction with a set name
	 * 
	 * @param name the name of the function (e.g. foo)
	 */
	protected CustomFunction(String name, int argumentCount) {
		this.argc = argumentCount;
		this.name = name;
	}

	public int getArgumentCount(){
		return argc;
	}
	
	public abstract double applyFunction(double... args);
}
