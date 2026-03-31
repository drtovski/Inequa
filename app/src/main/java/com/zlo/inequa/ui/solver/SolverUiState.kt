package com.zlo.inequa.ui.solver

import com.zlo.inequa.domain.model.SolutionStep
import com.zlo.inequa.domain.solver.Interval

data class SolverUiState(
    val expression: String = "",
    val answer: String = "",
    val steps: List<SolutionStep> = emptyList(),
    val graphIntervals: List<Interval> = emptyList(),
    val variableName: String = "x",
    val errorMessage: String? = null,
    val isStepsSheetVisible: Boolean = false
) {
    val canShowSteps: Boolean get() = steps.isNotEmpty()
    val canShowGraph: Boolean get() = errorMessage == null && graphIntervals.isNotEmpty()
}
