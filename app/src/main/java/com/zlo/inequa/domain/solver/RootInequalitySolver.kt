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

class RootInequalitySolver(
    private val normalizer: InequalityInputNormalizer = InequalityInputNormalizer(),
    private val expressionParser: LinearExpressionParser = LinearExpressionParser(),
    private val rangeSolver: LinearRangeSolver = LinearRangeSolver()
) {

    fun solve(input: InputExpression): InequalitySolution {
        return solve(input, "x")
    }

    fun solve(
        input: InputExpression,
        variableName: String
    ): InequalitySolution {
        val normalized = normalizer.normalizeForSolve(input.value)
        val parsed = parse(normalized)

        val rootExpression = expressionParser.parse(parsed.rootInner).normalized()
        val boundary = parseConstant(parsed.boundaryRaw)
        val normalizedExpression = "√(${formatLinearExpression(rootExpression, variableName)}) ${displaySymbol(parsed.operator)} ${formatNumber(boundary)}"
        val steps = mutableListOf<SolutionStep>()

        steps += SolutionStep(
            title = "Корневое неравенство",
            description = "Приводим выражение к виду $normalizedExpression."
        )

        val domain = rangeSolver.solve(rootExpression, InequalityOperator.GREATER_OR_EQUAL, 0.0)
        steps += SolutionStep(
            title = "Область определения",
            description = "Подкоренное выражение должно быть неотрицательным: ${formatLinearExpression(rootExpression, variableName)} ≥ 0."
        )

        val resultSet = solveRoot(
            expression = rootExpression,
            operator = parsed.operator,
            boundary = boundary,
            domain = domain,
            variableName = variableName,
            steps = steps
        )

        val shortAnswer = resultSet.toShortAnswer(variableName)
        val intervalAnswer = resultSet.toIntervalAnswer(variableName)

        steps += SolutionStep(
            title = "Ответ",
            description = "$shortAnswer, то есть $intervalAnswer."
        )

        return InequalitySolution(
            inputExpression = InputExpression(normalized),
            normalizedExpression = NormalizedExpression(normalizedExpression),
            shortAnswer = shortAnswer,
            intervalAnswer = intervalAnswer,
            steps = steps,
            variableName = variableName,
            graphIntervals = resultSet.asList()
        )
    }

    private fun solveRoot(
        expression: LinearExpression,
        operator: InequalityOperator,
        boundary: Double,
        domain: IntervalSet,
        variableName: String,
        steps: MutableList<SolutionStep>
    ): IntervalSet {
        if (boundary < 0) {
            steps += SolutionStep(
                title = "Проверка правой части",
                description = "Правая часть отрицательная: ${formatNumber(boundary)}."
            )

            return when (operator) {
                InequalityOperator.GREATER,
                InequalityOperator.GREATER_OR_EQUAL -> domain

                InequalityOperator.LESS,
                InequalityOperator.LESS_OR_EQUAL -> IntervalSet.empty()
            }
        }

        val squared = boundary * boundary
        steps += SolutionStep(
            title = "Переход без корня",
            description = "Так как правая часть неотрицательна, переходим к ${formatLinearExpression(expression, variableName)} ${displaySymbol(operator)} ${formatNumber(squared)} с учётом области определения."
        )

        val rawSet = when (operator) {
            InequalityOperator.GREATER -> rangeSolver.solve(expression, InequalityOperator.GREATER, squared)
            InequalityOperator.GREATER_OR_EQUAL -> rangeSolver.solve(expression, InequalityOperator.GREATER_OR_EQUAL, squared)
            InequalityOperator.LESS -> rangeSolver.solve(expression, InequalityOperator.LESS, squared)
            InequalityOperator.LESS_OR_EQUAL -> rangeSolver.solve(expression, InequalityOperator.LESS_OR_EQUAL, squared)
        }

        return rawSet.intersect(domain)
    }

    private fun parse(value: String): ParsedRoot {
        val operatorOccurrence = findOperatorOccurrence(value)
            ?: throw IllegalArgumentException("Не найден знак неравенства.")

        val leftRaw = value.substring(0, operatorOccurrence.index).trim()
        val rightRaw = value.substring(operatorOccurrence.index + operatorOccurrence.token.length).trim()

        if (leftRaw.isBlank() || rightRaw.isBlank()) {
            throw IllegalArgumentException("С обеих сторон знака неравенства должны быть выражения.")
        }

        val leftInner = extractRootInner(leftRaw)
        val rightInner = extractRootInner(rightRaw)

        if (leftInner == null && rightInner == null) {
            throw IllegalArgumentException("Не найден корень. Используйте формат √(ax + b).")
        }
        if (leftInner != null && rightInner != null) {
            throw IllegalArgumentException("Пока поддерживается только один корень в неравенстве.")
        }

        return if (leftInner != null) {
            ParsedRoot(
                rootInner = leftInner,
                operator = operatorOccurrence.operator,
                boundaryRaw = rightRaw
            )
        } else {
            ParsedRoot(
                rootInner = rightInner!!,
                operator = operatorOccurrence.operator.reversed(),
                boundaryRaw = leftRaw
            )
        }
    }

    private fun extractRootInner(side: String): String? {
        val trimmed = side.trim()
        if (trimmed.startsWith("sqrt(") && trimmed.endsWith(")") && trimmed.length >= 6) {
            return trimmed.substring(5, trimmed.length - 1)
        }
        if (trimmed.startsWith("√(") && trimmed.endsWith(")") && trimmed.length >= 4) {
            return trimmed.substring(2, trimmed.length - 1)
        }
        return null
    }

    private fun parseConstant(raw: String): Double {
        val parsed = expressionParser.parse(raw)
        if (!isAlmostZero(parsed.coefficient)) {
            throw IllegalArgumentException("В корневом неравенстве вторая часть должна быть числом.")
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

    private fun formatLinearExpression(expression: LinearExpression, variableName: String): String {
        val parts = mutableListOf<String>()

        if (!isAlmostZero(expression.coefficient)) {
            val absCoefficient = abs(expression.coefficient)
            val value = if (isAlmostEqual(absCoefficient, 1.0)) {
                variableName
            } else {
                "${formatNumber(absCoefficient)}$variableName"
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

    private data class ParsedRoot(
        val rootInner: String,
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

    private fun displaySymbol(operator: InequalityOperator): String {
        return when (operator) {
            InequalityOperator.GREATER -> ">"
            InequalityOperator.LESS -> "<"
            InequalityOperator.GREATER_OR_EQUAL -> "≥"
            InequalityOperator.LESS_OR_EQUAL -> "≤"
        }
    }

    private companion object {
        private const val EPSILON = 1e-9
    }
}
