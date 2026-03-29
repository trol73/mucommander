package de.congrace.exp4j;

import java.util.Locale;

public abstract class ExpressionUtil {
    /**
     * normalize a number to an acceptable format for exp4j e.g. normalizing "314e-2" yields "3.14"
     */
    public static String normalizeNumber(String number, Locale loc) {
        return number.replaceAll("e|E", "*10^");
    }

    public static String normalizeNumber(String number) throws UnparsableExpressionException {
        return normalizeNumber(number, Locale.getDefault());
    }
}
