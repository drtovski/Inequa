package com.zlo.inequa.domain.parser

class InequalityInputNormalizer {

    fun normalizeForSolve(rawInput: String): String {
        return normalizeBase(rawInput)
            .replace("x²", "x^2")
            .replace(',', '.')
    }

    fun normalizeForDisplay(rawInput: String): String {
        return normalizeBase(rawInput)
            .replace("x^2", "x²")
    }

    private fun normalizeBase(rawInput: String): String {
        return rawInput
            .replace("≥", ">=")
            .replace("≤", "<=")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("X", "x")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
