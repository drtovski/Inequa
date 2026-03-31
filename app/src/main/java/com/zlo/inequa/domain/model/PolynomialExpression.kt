package com.zlo.inequa.domain.model

import kotlin.math.abs

data class PolynomialExpression(
    private val coefficients: Map<Int, Double>
) {

    val degree: Int
        get() = normalizedCoefficients.keys.maxOrNull() ?: 0

    fun isZero(): Boolean = normalizedCoefficients.isEmpty()

    fun isConstant(): Boolean = degree == 0

    fun constantTerm(): Double = coefficientOf(0)

    fun coefficientOf(power: Int): Double = normalizedCoefficients[power] ?: 0.0

    fun plus(other: PolynomialExpression): PolynomialExpression {
        val result = mutableMapOf<Int, Double>()
        (normalizedCoefficients.keys + other.normalizedCoefficients.keys).forEach { power ->
            result[power] = coefficientOf(power) + other.coefficientOf(power)
        }
        return PolynomialExpression(result).normalized()
    }

    fun minus(other: PolynomialExpression): PolynomialExpression {
        val result = mutableMapOf<Int, Double>()
        (normalizedCoefficients.keys + other.normalizedCoefficients.keys).forEach { power ->
            result[power] = coefficientOf(power) - other.coefficientOf(power)
        }
        return PolynomialExpression(result).normalized()
    }

    fun multiply(other: PolynomialExpression): PolynomialExpression {
        if (isZero() || other.isZero()) return ZERO

        val result = mutableMapOf<Int, Double>()
        normalizedCoefficients.forEach { (leftPower, leftValue) ->
            other.normalizedCoefficients.forEach { (rightPower, rightValue) ->
                val power = leftPower + rightPower
                result[power] = (result[power] ?: 0.0) + leftValue * rightValue
            }
        }
        return PolynomialExpression(result).normalized()
    }

    fun divideByConstant(value: Double): PolynomialExpression {
        if (abs(value) < EPSILON) {
            throw IllegalArgumentException("Деление на ноль невозможно.")
        }

        return PolynomialExpression(
            normalizedCoefficients.mapValues { (_, coefficient) -> coefficient / value }
        ).normalized()
    }

    fun pow(exponent: Int): PolynomialExpression {
        if (exponent < 0) {
            throw IllegalArgumentException("Отрицательные степени не поддерживаются.")
        }

        if (exponent == 0) return constant(1.0)
        if (exponent == 1) return this

        var result = constant(1.0)
        repeat(exponent) {
            result = result.multiply(this)
        }
        return result.normalized()
    }

    fun evaluate(x: Double): Double {
        return normalizedCoefficients.entries.sumOf { (power, coefficient) ->
            coefficient * x.powInt(power)
        }
    }

    fun normalized(): PolynomialExpression {
        val cleaned = normalizedCoefficients
            .mapValues { (_, value) -> if (abs(value) < EPSILON) 0.0 else value }
            .filterValues { abs(it) >= EPSILON }

        return PolynomialExpression(cleaned)
    }

    companion object {
        private const val EPSILON = 1e-9

        val ZERO = PolynomialExpression(emptyMap())
        val X = PolynomialExpression(mapOf(1 to 1.0))

        fun constant(value: Double): PolynomialExpression {
            return if (abs(value) < EPSILON) ZERO else PolynomialExpression(mapOf(0 to value))
        }
    }

    private val normalizedCoefficients: Map<Int, Double>
        get() = coefficients
            .mapValues { (_, value) -> if (abs(value) < EPSILON) 0.0 else value }
            .filterValues { abs(it) >= EPSILON }

    private fun Double.powInt(power: Int): Double {
        var result = 1.0
        repeat(power) {
            result *= this
        }
        return result
    }
}
