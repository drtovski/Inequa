package com.zlo.inequa.domain.model

import kotlin.math.abs

data class RationalExpression(
    val numerator: PolynomialExpression,
    val denominator: PolynomialExpression
) {

    init {
        if (denominator.isZero()) {
            throw IllegalArgumentException("Деление на ноль невозможно.")
        }
    }

    fun plus(other: RationalExpression): RationalExpression {
        return RationalExpression(
            numerator = numerator.multiply(other.denominator)
                .plus(other.numerator.multiply(denominator)),
            denominator = denominator.multiply(other.denominator)
        ).normalized()
    }

    fun minus(other: RationalExpression): RationalExpression {
        return RationalExpression(
            numerator = numerator.multiply(other.denominator)
                .minus(other.numerator.multiply(denominator)),
            denominator = denominator.multiply(other.denominator)
        ).normalized()
    }

    fun multiply(other: RationalExpression): RationalExpression {
        return RationalExpression(
            numerator = numerator.multiply(other.numerator),
            denominator = denominator.multiply(other.denominator)
        ).normalized()
    }

    fun divide(other: RationalExpression): RationalExpression {
        if (other.numerator.isZero()) {
            throw IllegalArgumentException("Деление на ноль невозможно.")
        }

        return RationalExpression(
            numerator = numerator.multiply(other.denominator),
            denominator = denominator.multiply(other.numerator)
        ).normalized()
    }

    fun pow(exponent: Int): RationalExpression {
        if (exponent < 0) {
            throw IllegalArgumentException("Отрицательные степени не поддерживаются.")
        }

        return RationalExpression(
            numerator = numerator.pow(exponent),
            denominator = denominator.pow(exponent)
        ).normalized()
    }

    fun normalized(): RationalExpression {
        val normalizedNumerator = numerator.normalized()
        val normalizedDenominator = denominator.normalized()
        if (normalizedNumerator.isZero()) {
            return constant(0.0)
        }

        val denominatorLeading = normalizedDenominator.coefficientOf(normalizedDenominator.degree)
        return if (denominatorLeading < -EPSILON) {
            RationalExpression(
                numerator = normalizedNumerator.divideByConstant(-1.0),
                denominator = normalizedDenominator.divideByConstant(-1.0)
            )
        } else {
            RationalExpression(normalizedNumerator, normalizedDenominator)
        }
    }

    fun isPolynomial(): Boolean {
        return denominator.degree == 0 && abs(denominator.constantTerm() - 1.0) < EPSILON
    }

    companion object {
        private const val EPSILON = 1e-9

        fun constant(value: Double): RationalExpression {
            return RationalExpression(
                numerator = PolynomialExpression.constant(value),
                denominator = PolynomialExpression.constant(1.0)
            )
        }

        fun polynomial(value: PolynomialExpression): RationalExpression {
            return RationalExpression(
                numerator = value,
                denominator = PolynomialExpression.constant(1.0)
            )
        }
    }
}
