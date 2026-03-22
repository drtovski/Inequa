package com.zlo.inequa.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class InequalityInputNormalizerTest {

    private val normalizer = InequalityInputNormalizer()

    @Test
    fun `normalizeForSolve converts unicode comparator to canonical`() {
        val actual = normalizer.normalizeForSolve("x ≥ 2")

        assertEquals("x >= 2", actual)
    }

    @Test
    fun `normalizeForSolve keeps ascii comparator`() {
        val actual = normalizer.normalizeForSolve("x <= 3")

        assertEquals("x <= 3", actual)
    }

    @Test
    fun `normalizeForSolve keeps compact expression canonical`() {
        val actual = normalizer.normalizeForSolve("2x+3>7")

        assertEquals("2x+3>7", actual)
    }

    @Test
    fun `normalizeForSolve converts decimal comma and unicode less or equal`() {
        val actual = normalizer.normalizeForSolve("0,5x - 1 ≤ 4")

        assertEquals("0.5x - 1 <= 4", actual)
    }

    @Test
    fun `normalizeForSolve converts root symbol to sqrt`() {
        val actual = normalizer.normalizeForSolve("√(x + 1) > 2")

        assertEquals("sqrt(x + 1) > 2", actual)
    }
}
