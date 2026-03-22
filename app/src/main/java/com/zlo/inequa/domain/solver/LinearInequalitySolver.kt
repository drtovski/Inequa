package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.InequalitySolution
import com.zlo.inequa.domain.model.LinearExpression
import com.zlo.inequa.domain.model.NormalizedExpression
import com.zlo.inequa.domain.model.ParsedLinearInequality
import com.zlo.inequa.domain.model.SolutionStep
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class LinearInequalitySolver {

    fun solve(parsed: ParsedLinearInequality): InequalitySolution {
        val reduced = (parsed.left - parsed.right).normalized()
        val normalizedValue = "${formatLinearExpression(reduced)} ${parsed.operator.symbol} 0"
        val steps = mutableListOf<SolutionStep>()

        steps += SolutionStep(
            title = "Стандартная форма",
            description = "Переносим все члены влево: $normalizedValue"
        )

        val a = reduced.coefficient
        val b = reduced.constant
        val answer = if (isAlmostZero(a)) {
            solveConstantCase(
                constant = b,
                operator = parsed.operator,
                steps = steps
            )
        } else {
            solveLinearCase(
                coefficient = a,
                constant = b,
                operator = parsed.operator,
                steps = steps
            )
        }

        return InequalitySolution(
            inputExpression = parsed.source,
            normalizedExpression = NormalizedExpression(normalizedValue),
            shortAnswer = answer.shortAnswer,
            intervalAnswer = answer.intervalAnswer,
            steps = steps
        )
    }

    private fun solveConstantCase(
        constant: Double,
        operator: InequalityOperator,
        steps: MutableList<SolutionStep>
    ): SolverAnswer {
        val expression = "${formatNumber(constant)} ${operator.symbol} 0"
        val isTrue = operator.evaluate(constant)

        steps += SolutionStep(
            title = "Проверка выражения",
            description = "После сокращения получаем числовое неравенство: $expression"
        )

        return if (isTrue) {
            steps += SolutionStep(
                title = "Ответ",
                description = "Неравенство истинно при любом x, поэтому x ∈ R."
            )
            SolverAnswer(
                shortAnswer = "Любое x",
                intervalAnswer = "x ∈ R"
            )
        } else {
            steps += SolutionStep(
                title = "Ответ",
                description = "Неравенство ложно, решений нет."
            )
            SolverAnswer(
                shortAnswer = "Решений нет",
                intervalAnswer = "∅"
            )
        }
    }

    private fun solveLinearCase(
        coefficient: Double,
        constant: Double,
        operator: InequalityOperator,
        steps: MutableList<SolutionStep>
    ): SolverAnswer {
        val root = -constant / coefficient
        val effectiveOperator = if (coefficient < 0) operator.reversed() else operator
        val equation = "${formatLinearExpression(LinearExpression(coefficient, constant))} = 0"

        steps += SolutionStep(
            title = "Критическая точка",
            description = "Решаем уравнение $equation. Критическая точка: x = ${formatNumber(root)}."
        )

        if (coefficient < 0) {
            steps += SolutionStep(
                title = "Смена знака",
                description = "Коэффициент при x отрицательный, поэтому знак неравенства меняется на противоположный."
            )
        }

        val shortAnswer = when (effectiveOperator) {
            InequalityOperator.GREATER -> "x > ${formatNumber(root)}"
            InequalityOperator.LESS -> "x < ${formatNumber(root)}"
            InequalityOperator.GREATER_OR_EQUAL -> "x ≥ ${formatNumber(root)}"
            InequalityOperator.LESS_OR_EQUAL -> "x ≤ ${formatNumber(root)}"
        }

        val interval = when (effectiveOperator) {
            InequalityOperator.GREATER -> "x ∈ (${formatNumber(root)}; +∞)"
            InequalityOperator.LESS -> "x ∈ (-∞; ${formatNumber(root)})"
            InequalityOperator.GREATER_OR_EQUAL -> "x ∈ [${formatNumber(root)}; +∞)"
            InequalityOperator.LESS_OR_EQUAL -> "x ∈ (-∞; ${formatNumber(root)}]"
        }

        steps += SolutionStep(
            title = "Выбор интервала",
            description = intervalHint(effectiveOperator, root)
        )

        steps += SolutionStep(
            title = "Ответ",
            description = "$shortAnswer, то есть $interval."
        )

        return SolverAnswer(
            shortAnswer = shortAnswer,
            intervalAnswer = interval
        )
    }

    private fun formatLinearExpression(expression: LinearExpression): String {
        val parts = mutableListOf<String>()

        if (!isAlmostZero(expression.coefficient)) {
            val absCoefficient = abs(expression.coefficient)
            val value = when {
                isAlmostEqual(absCoefficient, 1.0) -> "x"
                else -> "${formatNumber(absCoefficient)}x"
            }

            parts += when {
                expression.coefficient < 0 -> "-$value"
                else -> value
            }
        }

        if (!isAlmostZero(expression.constant)) {
            val absConstant = formatNumber(abs(expression.constant))
            parts += when {
                parts.isEmpty() && expression.constant < 0 -> "-$absConstant"
                parts.isEmpty() -> absConstant
                expression.constant < 0 -> "- $absConstant"
                else -> "+ $absConstant"
            }
        }

        return if (parts.isEmpty()) "0" else parts.joinToString(" ")
    }

    private fun formatNumber(value: Double): String {
        if (isAlmostZero(value)) return "0"
        val rounded = round(value)
        if (isAlmostEqual(value, rounded)) return rounded.toLong().toString()
        return String.format(Locale.US, "%.6f", value)
            .trimEnd('0')
            .trimEnd('.')
            .replace('.', ',')
    }

    private fun isAlmostZero(value: Double): Boolean = abs(value) < EPSILON

    private fun isAlmostEqual(left: Double, right: Double): Boolean = abs(left - right) < EPSILON

    private fun intervalHint(operator: InequalityOperator, root: Double): String {
        val rootValue = formatNumber(root)
        return when (operator) {
            InequalityOperator.GREATER -> "Берём значения правее точки $rootValue: x > $rootValue."
            InequalityOperator.LESS -> "Берём значения левее точки $rootValue: x < $rootValue."
            InequalityOperator.GREATER_OR_EQUAL -> "Берём точку $rootValue и значения правее: x ≥ $rootValue."
            InequalityOperator.LESS_OR_EQUAL -> "Берём точку $rootValue и значения левее: x ≤ $rootValue."
        }
    }

    private data class SolverAnswer(
        val shortAnswer: String,
        val intervalAnswer: String
    )

    private companion object {
        private const val EPSILON = 1e-9
    }
}
