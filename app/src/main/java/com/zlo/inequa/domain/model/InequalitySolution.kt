package com.zlo.inequa.domain.model

data class InequalitySolution(
    val inputExpression: InputExpression,
    val normalizedExpression: NormalizedExpression,
    val shortAnswer: String,
    val intervalAnswer: String,
    val steps: List<SolutionStep>
)
