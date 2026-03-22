package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InputExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SolveLinearInequalityUseCaseTest {

    private val useCase = SolveLinearInequalityUseCase()

    @Test
    fun `solves 2x plus 3 greater than 7`() {
        val result = useCase(InputExpression("2x + 3 > 7"))

        assertEquals("x > 2", result.shortAnswer)
    }

    @Test
    fun `solves negative coefficient with sign flip`() {
        val result = useCase(InputExpression("-3x + 12 >= 0"))

        assertEquals("x ≤ 4", result.shortAnswer)
    }

    @Test
    fun `solves decimal coefficient`() {
        val result = useCase(InputExpression("0.5x - 1 < 4"))

        assertEquals("x < 10", result.shortAnswer)
    }

    @Test
    fun `solves unicode comparator input`() {
        val result = useCase(InputExpression("2x + 3 ≥ 7"))

        assertEquals("x ≥ 2", result.shortAnswer)
    }

    @Test
    fun `solves linear inequality with division and parentheses`() {
        val result = useCase(InputExpression("(2x - 1) / 3 <= 5"))

        assertEquals("x ≤ 8", result.shortAnswer)
    }

    @Test
    fun `solves absolute inequality`() {
        val result = useCase(InputExpression("|x - 3| >= 2"))

        assertEquals("x ≤ 1 или x ≥ 5", result.shortAnswer)
    }

    @Test
    fun `solves absolute inequality with abs function`() {
        val result = useCase(InputExpression("abs(x - 3) >= 2"))

        assertEquals("x ≤ 1 или x ≥ 5", result.shortAnswer)
    }

    @Test
    fun `solves root inequality`() {
        val result = useCase(InputExpression("√(x + 1) > 2"))

        assertEquals("x > 3", result.shortAnswer)
    }

    @Test
    fun `returns clear message for unsupported quadratic inequality`() {
        val error = runCatching {
            useCase(InputExpression("x² - 5x + 6 <= 0"))
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error?.message?.contains("Квадратные") == true)
    }
}
