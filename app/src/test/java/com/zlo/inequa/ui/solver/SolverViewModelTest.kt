package com.zlo.inequa.ui.solver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SolverViewModelTest {

    @Test
    fun `initial state is empty`() {
        val viewModel = SolverViewModel()

        val state = viewModel.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.answer)
        assertNull(state.errorMessage)
        assertTrue(state.steps.isEmpty())
    }

    @Test
    fun `keyboard inserts unicode comparator buttons`() {
        val viewModel = SolverViewModel()

        viewModel.onInsertToken("x")
        viewModel.onInsertToken("≥")
        viewModel.onInsertToken("2")

        assertEquals("x ≥ 2", viewModel.uiState.value.expression)
    }

    @Test
    fun `keyboard inserts module and root symbols`() {
        val viewModel = SolverViewModel()

        viewModel.onInsertToken("|")
        viewModel.onInsertToken("x")
        viewModel.onInsertToken("-")
        viewModel.onInsertToken("3")
        viewModel.onInsertToken("|")
        viewModel.onInsertToken("≥")
        viewModel.onInsertToken("2")
        assertEquals("|x - 3| ≥ 2", viewModel.uiState.value.expression)

        viewModel.onClear()
        viewModel.onInsertToken("√")
        viewModel.onInsertToken("x")
        viewModel.onInsertToken("+")
        viewModel.onInsertToken("1")
        viewModel.onInsertToken(")")
        viewModel.onInsertToken(">")
        viewModel.onInsertToken("2")
        assertEquals("√(x + 1) > 2", viewModel.uiState.value.expression)
    }

    @Test
    fun `backspace removes logical tokens`() {
        val viewModel = SolverViewModel()

        viewModel.onInsertToken("x")
        viewModel.onInsertToken("≤")
        viewModel.onInsertToken("5")

        viewModel.onBackspace()
        assertEquals("x ≤", viewModel.uiState.value.expression)

        viewModel.onBackspace()
        assertEquals("x", viewModel.uiState.value.expression)
    }

    @Test
    fun `clear resets expression and result`() {
        val viewModel = SolverViewModel()

        viewModel.onInsertToken("2")
        viewModel.onInsertToken("x")
        viewModel.onInsertToken("+")
        viewModel.onInsertToken("3")
        viewModel.onInsertToken(">")
        viewModel.onInsertToken("7")
        viewModel.onSolve()

        assertEquals("x > 2", viewModel.uiState.value.answer)

        viewModel.onClear()
        val state = viewModel.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.answer)
        assertTrue(state.steps.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun `equal solves valid expression and reports invalid`() {
        val validViewModel = SolverViewModel()
        validViewModel.onInsertToken("2")
        validViewModel.onInsertToken("x")
        validViewModel.onInsertToken("+")
        validViewModel.onInsertToken("3")
        validViewModel.onInsertToken(">")
        validViewModel.onInsertToken("7")
        validViewModel.onSolve()

        val validState = validViewModel.uiState.value
        assertEquals("x > 2", validState.answer)
        assertTrue(validState.steps.isNotEmpty())
        assertNull(validState.errorMessage)

        val invalidViewModel = SolverViewModel()
        invalidViewModel.onSolve()
        assertEquals("Введите неравенство.", invalidViewModel.uiState.value.errorMessage)

        invalidViewModel.onInsertToken("2")
        invalidViewModel.onInsertToken("+")
        invalidViewModel.onSolve()
        assertNotNull(invalidViewModel.uiState.value.errorMessage)
        assertFalse(invalidViewModel.uiState.value.canShowSteps)
    }
}
