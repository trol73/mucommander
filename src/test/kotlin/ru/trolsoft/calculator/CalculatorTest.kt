package ru.trolsoft.calculator

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CalculatorTest {

    @Test
    fun arithmetic() {
        with (Calculator()) {
            calculate("1+ 1") shouldBe 2.0
            calculate("10/2") shouldBe 5.0
            calculate("10 % 3") shouldBe 1.0
            calculate("2 ^ 3") shouldBe 8.0
            calculate("(1+3)*4") shouldBe 16.0
            calculate("(1+3)*3/2.5") shouldBe 4.8
        }
    }

    @Test
    fun formats() {
        with(Calculator()) {
            calculate("0x20") shouldBe 32.0
            calculate("0b111") shouldBe 7.0
        }
    }

    @Test
    fun logic() {
        with(Calculator()) {
            calculate("1<<3") shouldBe 8
            calculate("16 >> 2") shouldBe 4
            calculate("~0b1010") shouldBe 0b11110101
            calculate("0b11000 | 0b11") shouldBe 27
            calculate("0b11111 & 0b101") shouldBe 5
            calculate("0b1010 ^^ 0b111") shouldBe 13
        }
    }
}