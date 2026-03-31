package com.zlo.inequa.domain.model

import com.zlo.inequa.domain.solver.Interval

data class InequalitySolution(
    val inputExpression: InputExpression,
    val normalizedExpression: NormalizedExpression,
    val shortAnswer: String,
    val intervalAnswer: String,
    val steps: List<SolutionStep>,
    val variableName: String = "x",
    val graphIntervals: List<Interval> = emptyList()
)
