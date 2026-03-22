package com.zlo.inequa.ui.solver

import com.zlo.inequa.domain.model.SolutionStep

data class SolverUiState(
    val expression: String = "",
    val answer: String = "",
    val steps: List<SolutionStep> = emptyList(),
    val errorMessage: String? = null,
    val isStepsSheetVisible: Boolean = false
) {
    val canShowSteps: Boolean get() = steps.isNotEmpty()
}
