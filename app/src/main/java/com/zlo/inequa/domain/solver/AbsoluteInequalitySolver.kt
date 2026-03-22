package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.InequalitySolution
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.model.LinearExpression
import com.zlo.inequa.domain.model.NormalizedExpression
import com.zlo.inequa.domain.model.SolutionStep
import com.zlo.inequa.domain.parser.InequalityInputNormalizer
import com.zlo.inequa.domain.parser.LinearExpressionParser
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class AbsoluteInequalitySolver(
    private val normalizer: InequalityInputNormalizer = InequalityInputNormalizer(),
    private val expressionParser: LinearExpressionParser = LinearExpressionParser(),
    private val rangeSolver: LinearRangeSolver = LinearRangeSolver()
) {

    fun solve(input: InputExpression): InequalitySolution {
        val normalized = normalizer.normalizeForSolve(input.value)
        val parsed = parse(normalized)

        val innerExpression = expressionParser.parse(parsed.absoluteInner).normalized()
        val boundary = parseConstant(parsed.boundaryRaw)
        val normalizedExpression = "|${formatLinearExpression(innerExpression)}| ${parsed.operator.symbol} ${formatNumber(boundary)}"
        val steps = mutableListOf<SolutionStep>()

        steps += SolutionStep(
            title = "Модульное неравенство",
            description = "Приводим выражение к виду $normalizedExpression."
        )

        val resultSet = solveAbsolute(
            expression = innerExpression,
            operator = parsed.operator,
            boundary = boundary,
            steps = steps
        )

        val shortAnswer = resultSet.toShortAnswer()
        val intervalAnswer = resultSet.toIntervalAnswer()

        steps += SolutionStep(
            title = "Ответ",
            description = "$shortAnswer, то есть $intervalAnswer."
        )

        return InequalitySolution(
            inputExpression = InputExpression(normalized),
            normalizedExpression = NormalizedExpression(normalizedExpression),
            shortAnswer = shortAnswer,
            intervalAnswer = intervalAnswer,
            steps = steps
        )
    }

    private fun solveAbsolute(
        expression: LinearExpression,
        operator: InequalityOperator,
        boundary: Double,
        steps: MutableList<SolutionStep>
    ): IntervalSet {
        if (boundary < 0) {
            steps += SolutionStep(
                title = "Проверка границы",
                description = "Правая часть отрицательна: ${formatNumber(boundary)}."
            )

            return when (operator) {
                InequalityOperator.GREATER,
                InequalityOperator.GREATER_OR_EQUAL -> IntervalSet.allReal()

                InequalityOperator.LESS,
                InequalityOperator.LESS_OR_EQUAL -> IntervalSet.empty()
            }
        }

        return when (operator) {
            InequalityOperator.LESS,
            InequalityOperator.LESS_OR_EQUAL -> {
                val rightSign = if (operator == InequalityOperator.LESS) "<" else "≤"
                steps += SolutionStep(
                    title = "Раскрытие модуля",
                    description = "-${formatNumber(boundary)} $rightSign ${formatLinearExpression(expression)} $rightSign ${formatNumber(boundary)}."
                )
                val upper = rangeSolver.solve(expression, operator, boundary)
                val lowerOperator = if (operator == InequalityOperator.LESS) {
                    InequalityOperator.GREATER
                } else {
                    InequalityOperator.GREATER_OR_EQUAL
                }
                val lower = rangeSolver.solve(expression, lowerOperator, -boundary)
                upper.intersect(lower)
            }

            InequalityOperator.GREATER,
            InequalityOperator.GREATER_OR_EQUAL -> {
                val leftSign = if (operator == InequalityOperator.GREATER) ">" else "≥"
                steps += SolutionStep(
                    title = "Раскрытие модуля",
                    description = "${formatLinearExpression(expression)} $leftSign ${formatNumber(boundary)} или ${formatLinearExpression(expression)} ${if (leftSign == ">") "<" else "≤"} -${formatNumber(boundary)}."
                )
                val high = rangeSolver.solve(expression, operator, boundary)
                val oppositeOperator = if (operator == InequalityOperator.GREATER) {
                    InequalityOperator.LESS
                } else {
                    InequalityOperator.LESS_OR_EQUAL
                }
                val low = rangeSolver.solve(expression, oppositeOperator, -boundary)
                high.union(low)
            }
        }
    }

    private fun parse(value: String): ParsedAbsolute {
        val operatorOccurrence = findOperatorOccurrence(value)
            ?: throw IllegalArgumentException("Не найден знак неравенства.")

        val leftRaw = value.substring(0, operatorOccurrence.index).trim()
        val rightRaw = value.substring(operatorOccurrence.index + operatorOccurrence.token.length).trim()

        if (leftRaw.isBlank() || rightRaw.isBlank()) {
            throw IllegalArgumentException("С обеих сторон знака неравенства должны быть выражения.")
        }

        val leftInner = extractAbsoluteInner(leftRaw)
        val rightInner = extractAbsoluteInner(rightRaw)

        if (leftInner == null && rightInner == null) {
            throw IllegalArgumentException("Не найден модуль. Используйте формат |...|.")
        }
        if (leftInner != null && rightInner != null) {
            throw IllegalArgumentException("Пока поддерживается только один модуль в неравенстве.")
        }

        return if (leftInner != null) {
            ParsedAbsolute(
                absoluteInner = leftInner,
                operator = operatorOccurrence.operator,
                boundaryRaw = rightRaw
            )
        } else {
            ParsedAbsolute(
                absoluteInner = rightInner!!,
                operator = operatorOccurrence.operator.reversed(),
                boundaryRaw = leftRaw
            )
        }
    }

    private fun extractAbsoluteInner(side: String): String? {
        val trimmed = side.trim()
        if (trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.length >= 2) {
            return trimmed.substring(1, trimmed.length - 1)
        }

        val lower = trimmed.lowercase()
        if (lower.startsWith("abs(") && trimmed.endsWith(")") && trimmed.length >= 5) {
            return trimmed.substring(4, trimmed.length - 1)
        }

        return null
    }

    private fun parseConstant(raw: String): Double {
        val parsed = expressionParser.parse(raw)
        if (!isAlmostZero(parsed.coefficient)) {
            throw IllegalArgumentException("В модульном неравенстве вторая часть должна быть числом.")
        }
        return parsed.constant
    }

    private fun findOperatorOccurrence(input: String): OperatorOccurrence? {
        val options = listOf(
            OperatorOccurrenceToken(">=", InequalityOperator.GREATER_OR_EQUAL),
            OperatorOccurrenceToken("<=", InequalityOperator.LESS_OR_EQUAL),
            OperatorOccurrenceToken(">", InequalityOperator.GREATER),
            OperatorOccurrenceToken("<", InequalityOperator.LESS)
        )

        return options
            .mapNotNull { option ->
                input.indexOf(option.token)
                    .takeIf { it >= 0 }
                    ?.let { index ->
                        OperatorOccurrence(
                            index = index,
                            token = option.token,
                            operator = option.operator
                        )
                    }
            }
            .minByOrNull { it.index }
    }

    private fun formatLinearExpression(expression: LinearExpression): String {
        val parts = mutableListOf<String>()

        if (!isAlmostZero(expression.coefficient)) {
            val absCoefficient = abs(expression.coefficient)
            val value = if (isAlmostEqual(absCoefficient, 1.0)) {
                "x"
            } else {
                "${formatNumber(absCoefficient)}x"
            }

            parts += if (expression.coefficient < 0) "-$value" else value
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

    private data class ParsedAbsolute(
        val absoluteInner: String,
        val operator: InequalityOperator,
        val boundaryRaw: String
    )

    private data class OperatorOccurrenceToken(
        val token: String,
        val operator: InequalityOperator
    )

    private data class OperatorOccurrence(
        val index: Int,
        val token: String,
        val operator: InequalityOperator
    )

    private companion object {
        private const val EPSILON = 1e-9
    }
}
