package com.zlo.inequa.domain.input

class ExpressionInputEditor {

    fun append(rawExpression: String, token: String): String {
        val compact = compact(rawExpression)
        val normalizedToken = normalizeToken(token)
        if (normalizedToken.isEmpty()) return compact

        return when {
            normalizedToken.length == 1 && normalizedToken[0].isDigit() -> appendDigit(compact, normalizedToken)
            normalizedToken in variableTokens -> appendVariable(compact, normalizedToken)
            normalizedToken == "|" -> appendAbsoluteBar(compact)
            normalizedToken == "sqrt(" -> appendRoot(compact)
            normalizedToken == "^" -> appendPower(compact)
            normalizedToken == "^2" || normalizedToken == "^3" -> appendExponentShortcut(compact, normalizedToken)
            normalizedToken == "(" -> appendOpenParenthesis(compact)
            normalizedToken == ")" -> appendCloseParenthesis(compact)
            normalizedToken == "," || normalizedToken == "." -> appendDecimalSeparator(compact)
            normalizedToken in binaryComparators -> appendComparator(compact, normalizedToken)
            normalizedToken == "+" || normalizedToken == "/" || normalizedToken == "*" -> appendBinaryOperator(compact, normalizedToken)
            normalizedToken == "-" -> appendMinus(compact)
            else -> compact + normalizedToken
        }
    }

    fun backspace(rawExpression: String): String {
        val compact = compact(rawExpression)
        if (compact.isEmpty()) return compact

        val suffix = removableSuffixes
            .firstOrNull { compact.endsWith(it) }

        return if (suffix != null) {
            compact.dropLast(suffix.length)
        } else {
            compact.dropLast(1)
        }
    }

    private fun appendDigit(rawExpression: String, digit: String): String {
        val last = lastToken(rawExpression)
        if (isLastAbsoluteBarClosing(rawExpression, last)) return rawExpression

        return when (last) {
            in variableTokens -> rawExpression
            "^" -> rawExpression + digit
            "," -> rawExpression + digit
            ")" -> rawExpression
            else -> rawExpression + digit
        }
    }

    private fun appendVariable(rawExpression: String, variable: String): String {
        val last = lastToken(rawExpression)
        if (isLastAbsoluteBarClosing(rawExpression, last)) return rawExpression
        if (last == ")" || last in variableTokens || last == "^2" || last == "^3") return rawExpression

        return rawExpression + variable
    }

    private fun appendOpenParenthesis(rawExpression: String): String {
        val last = lastToken(rawExpression)
        return when (last) {
            "," -> rawExpression
            else -> rawExpression + "("
        }
    }

    private fun appendAbsoluteBar(rawExpression: String): String {
        val barsCount = rawExpression.count { it == '|' }
        val isOpeningBar = barsCount % 2 == 0
        val last = lastToken(rawExpression)

        return if (isOpeningBar) {
            if (last == null || last in openingOrOperatorTokens || last in binaryComparators) {
                rawExpression + "|"
            } else {
                rawExpression
            }
        } else {
            if (last == null || last in openingOrOperatorTokens || last in binaryComparators || last == "," || last == "|") {
                rawExpression
            } else {
                rawExpression + "|"
            }
        }
    }

    private fun appendRoot(rawExpression: String): String {
        val last = lastToken(rawExpression)
        return if (last == null || last in openingOrOperatorTokens || last in binaryComparators) {
            rawExpression + "sqrt("
        } else {
            rawExpression
        }
    }

    private fun appendPower(rawExpression: String): String {
        if (rawExpression.isEmpty()) return rawExpression

        val last = lastToken(rawExpression) ?: return rawExpression
        return if (last in variableTokens || last == ")" || isLastAbsoluteBarClosing(rawExpression, last)) {
            rawExpression + "^"
        } else {
            rawExpression
        }
    }

    private fun appendExponentShortcut(rawExpression: String, exponentToken: String): String {
        if (rawExpression.isEmpty()) return rawExpression

        val last = lastToken(rawExpression) ?: return rawExpression
        return when {
            last == "^" -> rawExpression + exponentToken.removePrefix("^")
            last in variableTokens || last == ")" || isLastAbsoluteBarClosing(rawExpression, last) -> rawExpression + exponentToken
            else -> rawExpression
        }
    }

    private fun appendCloseParenthesis(rawExpression: String): String {
        if (rawExpression.isEmpty()) return rawExpression
        if (countOpenParenthesis(rawExpression) <= countCloseParenthesis(rawExpression)) return rawExpression

        val last = lastToken(rawExpression) ?: return rawExpression
        if (last in openingOrOperatorTokens || last in binaryComparators || last == ",") return rawExpression

        return rawExpression + ")"
    }

    private fun appendDecimalSeparator(rawExpression: String): String {
        val last = lastToken(rawExpression)
        if (isLastAbsoluteBarClosing(rawExpression, last)) return rawExpression

        val currentNumber = currentNumberPart(rawExpression)
        if (currentNumber.contains(',') || currentNumber.contains('.')) {
            return rawExpression
        }

        if (currentNumber.isNotEmpty()) {
            return rawExpression + ","
        }

        return if (last == null || last in openingOrOperatorTokens || last in binaryComparators) {
            rawExpression + "0,"
        } else {
            rawExpression
        }
    }

    private fun appendComparator(rawExpression: String, comparator: String): String {
        if (rawExpression.isEmpty()) return rawExpression

        val last = lastToken(rawExpression) ?: return rawExpression
        if (isLastAbsoluteBarOpening(rawExpression, last)) return rawExpression
        if (last in openingOrOperatorTokens || last in binaryComparators || last == ",") {
            return rawExpression
        }

        return rawExpression + comparator
    }

    private fun appendBinaryOperator(rawExpression: String, operator: String): String {
        if (rawExpression.isEmpty()) return rawExpression
        val last = lastToken(rawExpression) ?: return rawExpression

        if (isLastAbsoluteBarOpening(rawExpression, last)) return rawExpression
        if (last in openingOrOperatorTokens || last in binaryComparators || last == ",") {
            return rawExpression
        }

        return rawExpression + operator
    }

    private fun appendMinus(rawExpression: String): String {
        if (rawExpression.isEmpty()) return "-"

        val last = lastToken(rawExpression) ?: return rawExpression
        if (last == "," || last == "-" || last == "^") return rawExpression

        return rawExpression + "-"
    }

    private fun currentNumberPart(rawExpression: String): String {
        return rawExpression.takeLastWhile { it.isDigit() || it == ',' || it == '.' }
    }

    private fun countOpenParenthesis(rawExpression: String): Int = rawExpression.count { it == '(' }

    private fun countCloseParenthesis(rawExpression: String): Int = rawExpression.count { it == ')' }

    private fun compact(value: String): String = value.replace(" ", "")

    private fun normalizeToken(token: String): String {
        return when (token.trim()) {
            "≥" -> ">="
            "≤" -> "<="
            "." -> ","
            "√" -> "sqrt("
            "²" -> "^2"
            "³" -> "^3"
            "X" -> "x"
            "Y" -> "y"
            "Z" -> "z"
            else -> token.trim()
        }
    }

    private fun lastToken(rawExpression: String): String? {
        if (rawExpression.isEmpty()) return null

        return removableSuffixes.firstOrNull { rawExpression.endsWith(it) }
            ?: rawExpression.last().toString()
    }

    private fun isLastAbsoluteBarOpening(rawExpression: String, lastToken: String?): Boolean {
        if (lastToken != "|") return false
        return rawExpression.count { it == '|' } % 2 != 0
    }

    private fun isLastAbsoluteBarClosing(rawExpression: String, lastToken: String?): Boolean {
        if (lastToken != "|") return false
        return !isLastAbsoluteBarOpening(rawExpression, lastToken)
    }

    private companion object {
        private val binaryComparators = setOf(">", "<", ">=", "<=")
        private val variableTokens = setOf("x", "y", "z")
        private val openingOrOperatorTokens = setOf("(", "sqrt(", "+", "-", "/", "*", "=", "^")
        private val removableSuffixes = listOf("sqrt(", ">=", "<=", "^2", "^3")
    }
}
