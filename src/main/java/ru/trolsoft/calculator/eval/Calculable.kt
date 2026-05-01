package ru.trolsoft.calculator.eval

/**
 * This is the basic result class of the exp4j [ExpressionBuilder]
 * 
 * @author frank asseg
 */
interface Calculable {
    /**
     * calculate the result of the expression
     * 
     * @return the result of the calculation
     */
    fun calculate(): Double = 0.0

    /**
     * calculate the result of the expression
     * 
     * @param variableValues
     * the values of the variable. The values must be in the same order as the declaration of variables in
     * the [ExpressionBuilder] used to construct this [Calculable] instance
     * @return the result of the calculation
     */
    fun calculate(vararg variableValues: Double): Double

    /**
     * return the expression in reverse polish postfix notation
     * 
     * @return the expression used to construct this [Calculable]
     */
    val expression: String?

    /**
     * set a variable value for the calculation
     * 
     * @param name
     * the variable name
     * @param value
     * the value of the variable
     */
    fun setVariable(name: String?, value: Double)
}
