package com.zlo.inequa.domain.formatter

import org.junit.Assert.assertEquals
import org.junit.Test

class MathExpressionFormatterTest {

    private val formatter = MathExpressionFormatter()

    @Test
    fun `formats compact linear inequality`() {
        val actual = formatter.format("2x+3>7")

        assertEquals("2x + 3 > 7", actual)
    }

    @Test
    fun `formats unary minus and comparator`() {
        val actual = formatter.format("-3x+12>=0")

        assertEquals("-3x + 12 ≥ 0", actual)
    }

    @Test
    fun `formats parentheses and division`() {
        val actual = formatter.format("(2x-1)/3<=5")

        assertEquals("(2x - 1) / 3 ≤ 5", actual)
    }

    @Test
    fun `formats absolute inequality`() {
        val actual = formatter.format("|x-3|>=2")

        assertEquals("|x - 3| ≥ 2", actual)
    }

    @Test
    fun `formats root inequality`() {
        val actual = formatter.format("sqrt(x+1)>2")

        assertEquals("√(x + 1) > 2", actual)
    }
}
