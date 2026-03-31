package com.zlo.inequa.domain.parser

class InequalityInputNormalizer {

    fun normalizeForSolve(rawInput: String): String {
        return normalizeBase(rawInput)
            .replace("√", "sqrt")
            .replace(Regex("(?i)sqrt\\s*\\("), "sqrt(")
            .let(::replaceSuperscriptsWithPowers)
            .replace(',', '.')
    }

    fun normalizeForDisplay(rawInput: String): String {
        return normalizeBase(rawInput)
            .replace(Regex("(?i)sqrt\\s*\\("), "√(")
            .let(::replacePowersWithSuperscripts)
    }

    private fun normalizeBase(rawInput: String): String {
        return rawInput
            .replace("≥", ">=")
            .replace("≤", "<=")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("X", "x")
            .replace("Y", "y")
            .replace("Z", "z")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun replaceSuperscriptsWithPowers(input: String): String {
        val builder = StringBuilder()
        var index = 0

        while (index < input.length) {
            val current = input[index]
            if (!superscriptToDigit.containsKey(current)) {
                builder.append(current)
                index += 1
                continue
            }

            builder.append('^')
            while (index < input.length) {
                val digit = superscriptToDigit[input[index]] ?: break
                builder.append(digit)
                index += 1
            }
        }

        return builder.toString()
    }

    private fun replacePowersWithSuperscripts(input: String): String {
        val builder = StringBuilder()
        var index = 0

        while (index < input.length) {
            val current = input[index]
            if (current == '^') {
                val digitsStart = index + 1
                var digitsEnd = digitsStart
                while (digitsEnd < input.length && input[digitsEnd].isDigit()) {
                    digitsEnd += 1
                }

                if (digitsEnd > digitsStart) {
                    input.substring(digitsStart, digitsEnd).forEach { digit ->
                        builder.append(digitToSuperscript[digit] ?: digit)
                    }
                    index = digitsEnd
                    continue
                }
            }

            builder.append(current)
            index += 1
        }

        return builder.toString()
    }

    private companion object {
        private val superscriptToDigit = mapOf(
            '⁰' to '0',
            '¹' to '1',
            '²' to '2',
            '³' to '3',
            '⁴' to '4',
            '⁵' to '5',
            '⁶' to '6',
            '⁷' to '7',
            '⁸' to '8',
            '⁹' to '9'
        )

        private val digitToSuperscript = mapOf(
            '0' to '⁰',
            '1' to '¹',
            '2' to '²',
            '3' to '³',
            '4' to '⁴',
            '5' to '⁵',
            '6' to '⁶',
            '7' to '⁷',
            '8' to '⁸',
            '9' to '⁹'
        )
    }
}
