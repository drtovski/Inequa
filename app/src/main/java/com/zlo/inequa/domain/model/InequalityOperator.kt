package com.zlo.inequa.domain.model

enum class InequalityOperator(
    val symbol: String,
    val includesEquality: Boolean
) {
    GREATER(">", false),
    LESS("<", false),
    GREATER_OR_EQUAL(">=", true),
    LESS_OR_EQUAL("<=", true);

    fun reversed(): InequalityOperator {
        return when (this) {
            GREATER -> LESS
            LESS -> GREATER
            GREATER_OR_EQUAL -> LESS_OR_EQUAL
            LESS_OR_EQUAL -> GREATER_OR_EQUAL
        }
    }

    fun evaluate(value: Double): Boolean {
        return when (this) {
            GREATER -> value > EPSILON
            LESS -> value < -EPSILON
            GREATER_OR_EQUAL -> value >= -EPSILON
            LESS_OR_EQUAL -> value <= EPSILON
        }
    }

    private companion object {
        private const val EPSILON = 1e-9
    }
}
