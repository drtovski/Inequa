package com.zlo.inequa.domain.model

import kotlin.math.abs

data class LinearExpression(
    val coefficient: Double,
    val constant: Double
) {
    operator fun plus(other: LinearExpression): LinearExpression {
        return LinearExpression(
            coefficient = coefficient + other.coefficient,
            constant = constant + other.constant
        ).normalized()
    }

    operator fun minus(other: LinearExpression): LinearExpression {
        return LinearExpression(
            coefficient = coefficient - other.coefficient,
            constant = constant - other.constant
        ).normalized()
    }

    fun multiply(other: LinearExpression): LinearExpression {
        if (!isConstant() && !other.isConstant()) {
            throw IllegalArgumentException("Пока поддерживаются только линейные неравенства.")
        }

        return when {
            other.isConstant() -> LinearExpression(
                coefficient = coefficient * other.constant,
                constant = constant * other.constant
            ).normalized()

            isConstant() -> LinearExpression(
                coefficient = other.coefficient * constant,
                constant = other.constant * constant
            ).normalized()

            else -> ZERO
        }
    }

    fun divide(other: LinearExpression): LinearExpression {
        if (!other.isConstant()) {
            throw IllegalArgumentException("Деление на выражение с x пока не поддерживается.")
        }
        if (isAlmostZero(other.constant)) {
            throw IllegalArgumentException("Деление на ноль невозможно.")
        }
        return LinearExpression(
            coefficient = coefficient / other.constant,
            constant = constant / other.constant
        ).normalized()
    }

    fun isConstant(): Boolean = isAlmostZero(coefficient)

    fun normalized(): LinearExpression {
        return LinearExpression(
            coefficient = zeroIfTiny(coefficient),
            constant = zeroIfTiny(constant)
        )
    }

    companion object {
        val ZERO = LinearExpression(0.0, 0.0)
        val X = LinearExpression(1.0, 0.0)
        fun constant(value: Double) = LinearExpression(0.0, value)

        private const val EPSILON = 1e-9
        private fun isAlmostZero(value: Double): Boolean = abs(value) < EPSILON
        private fun zeroIfTiny(value: Double): Double = if (isAlmostZero(value)) 0.0 else value
    }
}
