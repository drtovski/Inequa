package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.PolynomialExpression
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

internal object PolynomialSupport {
    private const val EPSILON = 1e-9

    fun formatPolynomial(expression: PolynomialExpression, variableName: String): String {
        if (expression.isZero()) return "0"

        val parts = mutableListOf<String>()
        for (power in expression.degree downTo 0) {
            val coefficient = expression.coefficientOf(power)
            if (abs(coefficient) < EPSILON) continue

            val absCoefficient = formatNumber(abs(coefficient))
            val variablePart = when (power) {
                0 -> ""
                1 -> variableName
                2 -> "${variableName}²"
                3 -> "${variableName}³"
                else -> "$variableName^$power"
            }

            val coefficientPart = when {
                power == 0 -> absCoefficient
                abs(abs(coefficient) - 1.0) < EPSILON -> variablePart
                else -> "$absCoefficient$variablePart"
            }

            val signedPart = when {
                parts.isEmpty() && coefficient < 0 -> "-$coefficientPart"
                parts.isEmpty() -> coefficientPart
                coefficient < 0 -> "- $coefficientPart"
                else -> "+ $coefficientPart"
            }

            parts += signedPart
        }

        return parts.joinToString(" ")
    }

    fun formatNumber(value: Double): String {
        if (abs(value) < EPSILON) return "0"

        val rounded = value.toLong().toDouble()
        if (abs(value - rounded) < EPSILON) {
            return rounded.toLong().toString()
        }

        return String.format(Locale.US, "%.6f", value)
            .trimEnd('0')
            .trimEnd('.')
            .replace('.', ',')
    }

    fun realRoots(expression: PolynomialExpression): List<Double> {
        return when (expression.degree) {
            0 -> emptyList()
            1 -> {
                val a = expression.coefficientOf(1)
                val b = expression.coefficientOf(0)
                if (abs(a) < EPSILON) emptyList() else listOf(-b / a)
            }

            2 -> {
                val a = expression.coefficientOf(2)
                val b = expression.coefficientOf(1)
                val c = expression.coefficientOf(0)
                val discriminant = b * b - 4.0 * a * c

                when {
                    discriminant < -EPSILON -> emptyList()
                    abs(discriminant) < EPSILON -> listOf(-b / (2.0 * a))
                    else -> {
                        val delta = sqrt(discriminant)
                        listOf(
                            (-b - delta) / (2.0 * a),
                            (-b + delta) / (2.0 * a)
                        ).sorted()
                    }
                }
            }

            else -> throw IllegalArgumentException("Пока поддерживаются степени не выше второй.")
        }
    }

    fun solveBySignChart(
        numerator: PolynomialExpression,
        denominator: PolynomialExpression,
        operator: InequalityOperator
    ): IntervalSet {
        if (numerator.degree > 2 || denominator.degree > 2) {
            throw IllegalArgumentException("Пока поддерживаются степени не выше второй.")
        }

        val criticalPoints = (realRoots(numerator) + realRoots(denominator))
            .sorted()
            .fold(mutableListOf<Double>()) { acc, value ->
                if (acc.none { abs(it - value) < EPSILON }) {
                    acc += value
                }
                acc
            }

        var result = IntervalSet.empty()
        val boundaries = listOf<Double?>(null) + criticalPoints + listOf<Double?>(null)

        for (index in 0 until boundaries.lastIndex) {
            val left = boundaries[index]
            val right = boundaries[index + 1]
            val sample = samplePoint(left, right)
            val denominatorValue = denominator.evaluate(sample)
            if (abs(denominatorValue) < EPSILON) continue

            val value = numerator.evaluate(sample) / denominatorValue
            if (operator.evaluate(value)) {
                result = result.union(
                    IntervalSet.from(
                        Interval(
                            start = left,
                            end = right,
                            includeStart = false,
                            includeEnd = false
                        )
                    )
                )
            }
        }

        if (operator.includesEquality) {
            realRoots(numerator).forEach { root ->
                if (abs(denominator.evaluate(root)) >= EPSILON) {
                    result = result.union(
                        IntervalSet.from(
                            Interval(
                                start = root,
                                end = root,
                                includeStart = true,
                                includeEnd = true
                            )
                        )
                    )
                }
            }
        }

        return result
    }

    private fun samplePoint(left: Double?, right: Double?): Double {
        return when {
            left == null && right == null -> 0.0
            left == null -> right!! - 1.0
            right == null -> left + 1.0
            else -> (left + right) / 2.0
        }
    }
}
