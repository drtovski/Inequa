package com.zlo.inequa.domain.parser

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.model.ParsedLinearInequality

class LinearInequalityParser(
    private val normalizer: InequalityInputNormalizer = InequalityInputNormalizer(),
    private val expressionParser: LinearExpressionParser = LinearExpressionParser()
) {

    fun parse(input: InputExpression): ParsedLinearInequality {
        val normalized = normalizer.normalizeForSolve(input.value)

        if (normalized.isBlank()) {
            throw IllegalArgumentException("Введите неравенство.")
        }

        val operatorOccurrence = findOperatorOccurrence(normalized)
            ?: throw IllegalArgumentException("Не найден знак неравенства. Используйте >, <, >=, <=, ≥, ≤.")

        val leftRaw = normalized.substring(0, operatorOccurrence.index).trim()
        val rightRaw = normalized.substring(operatorOccurrence.index + operatorOccurrence.token.length).trim()

        if (leftRaw.isBlank() || rightRaw.isBlank()) {
            throw IllegalArgumentException("С обеих сторон знака неравенства должны быть выражения.")
        }

        if (containsAnyOperator(rightRaw)) {
            throw IllegalArgumentException("Обнаружено несколько знаков неравенства.")
        }

        return ParsedLinearInequality(
            source = InputExpression(normalized),
            left = expressionParser.parse(leftRaw),
            right = expressionParser.parse(rightRaw),
            operator = operatorOccurrence.operator
        )
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

    private fun containsAnyOperator(value: String): Boolean {
        return value.contains(">=") ||
            value.contains("<=") ||
            value.contains('>') ||
            value.contains('<')
    }

    private data class OperatorOccurrenceToken(
        val token: String,
        val operator: InequalityOperator
    )

    private data class OperatorOccurrence(
        val index: Int,
        val token: String,
        val operator: InequalityOperator
    )
}
