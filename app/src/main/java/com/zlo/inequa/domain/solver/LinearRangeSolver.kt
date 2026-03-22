package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.LinearExpression
import kotlin.math.abs

class LinearRangeSolver {

    fun solve(
        expression: LinearExpression,
        operator: InequalityOperator,
        rightConstant: Double = 0.0
    ): IntervalSet {
        val coefficient = expression.coefficient
        val constant = expression.constant

        if (isAlmostZero(coefficient)) {
            val value = constant - rightConstant
            return if (operator.evaluate(value)) {
                IntervalSet.allReal()
            } else {
                IntervalSet.empty()
            }
        }

        val root = (rightConstant - constant) / coefficient
        val effectiveOperator = if (coefficient < 0) operator.reversed() else operator

        return when (effectiveOperator) {
            InequalityOperator.GREATER -> IntervalSet.greaterThan(root, inclusive = false)
            InequalityOperator.LESS -> IntervalSet.lessThan(root, inclusive = false)
            InequalityOperator.GREATER_OR_EQUAL -> IntervalSet.greaterThan(root, inclusive = true)
            InequalityOperator.LESS_OR_EQUAL -> IntervalSet.lessThan(root, inclusive = true)
        }
    }

    private fun isAlmostZero(value: Double): Boolean = abs(value) < EPSILON

    private companion object {
        private const val EPSILON = 1e-9
    }
}
