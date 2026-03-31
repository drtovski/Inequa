package com.zlo.inequa.domain.parser

import com.zlo.inequa.domain.model.InequalityOperator

class InequalityPartsParser(
    private val normalizer: InequalityInputNormalizer = InequalityInputNormalizer()
) {

    fun parse(rawInput: String): ParsedInequalityParts {
        val normalized = normalizer.normalizeForSolve(rawInput)

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

        return ParsedInequalityParts(
            normalizedInput = normalized,
            leftRaw = leftRaw,
            rightRaw = rightRaw,
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

    data class ParsedInequalityParts(
        val normalizedInput: String,
        val leftRaw: String,
        val rightRaw: String,
        val operator: InequalityOperator
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
}
