package ru.trolsoft.calculator.eval

import java.util.Locale

fun reverseArray(data: DoubleArray): DoubleArray {
    var left = 0
    var right = data.size - 1

    while (left < right) {
        // swap the values at the left and right indices
        val temp = data[left]
        data[left] = data[right]
        data[right] = temp

        // move the left and right index pointers in toward the center
        left++
        right--
    }
    return data
}


/**
 * normalize a number to an acceptable format for exp4j e.g. normalizing "314e-2" yields "3.14"
 */
fun normalizeNumber(number: String, loc: Locale? = Locale.getDefault()): String =
    number.replace("e|E".toRegex(), "*10^")
