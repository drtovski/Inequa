package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalityType

class InequalityTypeDetector {

    fun detect(normalizedExpression: String): InequalityType {
        val value = normalizedExpression.lowercase()

        if (systemPattern.containsMatchIn(value)) {
            return InequalityType.SYSTEM
        }

        if (absolutePattern.containsMatchIn(value)) {
            return InequalityType.ABSOLUTE
        }

        if (quadraticPattern.containsMatchIn(value)) {
            return InequalityType.QUADRATIC
        }

        val comparatorCount = comparatorPattern.findAll(value).count()
        if (comparatorCount > 1) {
            return InequalityType.COMPOUND
        }

        if (hasVariableDenominator(value)) {
            return InequalityType.RATIONAL
        }

        return InequalityType.LINEAR
    }

    private fun hasVariableDenominator(value: String): Boolean {
        val comparatorMatch = comparatorPattern.find(value) ?: return false
        val left = value.substring(0, comparatorMatch.range.first)
        val right = value.substring(comparatorMatch.range.last + 1)
        return hasVariableDenominatorInPart(left) || hasVariableDenominatorInPart(right)
    }

    private fun hasVariableDenominatorInPart(part: String): Boolean {
        var index = 0
        while (index < part.length) {
            if (part[index] == '/') {
                var nextIndex = index + 1
                while (nextIndex < part.length && part[nextIndex].isWhitespace()) {
                    nextIndex += 1
                }
                if (nextIndex >= part.length) return false

                val denominator = if (part[nextIndex] == '(') {
                    readParenthesizedSegment(part, nextIndex)
                } else {
                    readSimpleSegment(part, nextIndex)
                }

                if (denominator.contains('x')) {
                    return true
                }
            }
            index += 1
        }
        return false
    }

    private fun readParenthesizedSegment(value: String, startIndex: Int): String {
        var index = startIndex
        var depth = 0
        while (index < value.length) {
            when (value[index]) {
                '(' -> depth += 1
                ')' -> {
                    depth -= 1
                    if (depth == 0) {
                        return value.substring(startIndex, index + 1)
                    }
                }
            }
            index += 1
        }
        return value.substring(startIndex)
    }

    private fun readSimpleSegment(value: String, startIndex: Int): String {
        var index = startIndex
        while (index < value.length && value[index] !in stopCharacters) {
            index += 1
        }
        return value.substring(startIndex, index)
    }

    private companion object {
        private val comparatorPattern = Regex("(>=|<=|>|<)")
        private val quadraticPattern = Regex("x\\^2")
        private val absolutePattern = Regex("\\|")
        private val systemPattern = Regex("(^|\\s)и(\\s|$)|&&|;")
        private val stopCharacters = setOf('+', '-', '*', '/', '>', '<', '=', ' ')
    }
}
