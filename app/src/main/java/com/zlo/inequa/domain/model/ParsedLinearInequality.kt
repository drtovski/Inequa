package com.zlo.inequa.domain.model

data class ParsedLinearInequality(
    val source: InputExpression,
    val left: LinearExpression,
    val right: LinearExpression,
    val operator: InequalityOperator,
    val variableName: String = "x"
)
