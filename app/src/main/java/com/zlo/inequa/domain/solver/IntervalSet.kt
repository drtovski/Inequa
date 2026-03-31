package com.zlo.inequa.domain.solver

import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

data class Interval(
    val start: Double?,
    val end: Double?,
    val includeStart: Boolean,
    val includeEnd: Boolean
)

class IntervalSet private constructor(
    private val intervals: List<Interval>
) {

    fun asList(): List<Interval> = intervals.toList()

    fun isEmpty(): Boolean = intervals.isEmpty()

    fun isAllReal(): Boolean {
        return intervals.size == 1 &&
            intervals.first().start == null &&
            intervals.first().end == null
    }

    fun union(other: IntervalSet): IntervalSet {
        val source = (intervals + other.intervals)
            .sortedWith(
                compareBy<Interval> { startValue(it.start) }
                    .thenBy { if (it.includeStart) 0 else 1 }
            )

        if (source.isEmpty()) return empty()

        val merged = mutableListOf<Interval>()
        var current = source.first()

        source.drop(1).forEach { next ->
            if (current.overlapsOrTouches(next)) {
                current = current.mergeWith(next)
            } else {
                merged += current
                current = next
            }
        }

        merged += current
        return IntervalSet(merged)
    }

    fun intersect(other: IntervalSet): IntervalSet {
        if (isEmpty() || other.isEmpty()) return empty()

        val result = mutableListOf<Interval>()
        var leftIndex = 0
        var rightIndex = 0

        while (leftIndex < intervals.size && rightIndex < other.intervals.size) {
            val left = intervals[leftIndex]
            val right = other.intervals[rightIndex]

            intersectIntervals(left, right)?.let { result += it }

            val leftEnd = endValue(left.end)
            val rightEnd = endValue(right.end)
            when {
                leftEnd < rightEnd -> leftIndex += 1
                leftEnd > rightEnd -> rightIndex += 1
                else -> {
                    leftIndex += 1
                    rightIndex += 1
                }
            }
        }

        return IntervalSet(result)
    }

    fun toShortAnswer(variableName: String = "x"): String {
        if (isEmpty()) return "Решений нет"
        if (isAllReal()) return "Любое $variableName"

        return intervals.joinToString(" или ") { interval ->
            intervalToShort(interval, variableName)
        }
    }

    fun toIntervalAnswer(variableName: String = "x"): String {
        if (isEmpty()) return "∅"
        if (isAllReal()) return "$variableName ∈ R"

        val value = intervals.joinToString(" ∪ ") { interval ->
            intervalToNotation(interval)
        }
        return "$variableName ∈ $value"
    }

    private fun intervalToShort(interval: Interval, variableName: String): String {
        val left = interval.start
        val right = interval.end

        return when {
            left == null && right != null -> {
                val sign = if (interval.includeEnd) "≤" else "<"
                "$variableName $sign ${formatNumber(right)}"
            }

            left != null && right == null -> {
                val sign = if (interval.includeStart) "≥" else ">"
                "$variableName $sign ${formatNumber(left)}"
            }

            left != null && right != null && almostEqual(left, right) && interval.includeStart && interval.includeEnd -> {
                "$variableName = ${formatNumber(left)}"
            }

            left != null && right != null -> {
                val leftSign = if (interval.includeStart) "≤" else "<"
                val rightSign = if (interval.includeEnd) "≤" else "<"
                "${formatNumber(left)} $leftSign $variableName $rightSign ${formatNumber(right)}"
            }

            else -> "Любое $variableName"
        }
    }

    private fun intervalToNotation(interval: Interval): String {
        val leftBracket = if (interval.start == null) "(" else if (interval.includeStart) "[" else "("
        val rightBracket = if (interval.end == null) ")" else if (interval.includeEnd) "]" else ")"
        val leftValue = interval.start?.let(::formatNumber) ?: "-∞"
        val rightValue = interval.end?.let(::formatNumber) ?: "+∞"
        return "$leftBracket$leftValue; $rightValue$rightBracket"
    }

    private fun intersectIntervals(left: Interval, right: Interval): Interval? {
        val start = maxStart(left, right)
        val end = minEnd(left, right)

        val startNumeric = startValue(start.first)
        val endNumeric = endValue(end.first)
        val isValid = when {
            startNumeric < endNumeric -> true
            startNumeric > endNumeric -> false
            else -> start.second && end.second
        }

        if (!isValid) return null

        return Interval(
            start = start.first,
            end = end.first,
            includeStart = start.second,
            includeEnd = end.second
        )
    }

    private fun maxStart(left: Interval, right: Interval): Pair<Double?, Boolean> {
        return when {
            left.start == null && right.start == null -> null to false
            left.start == null -> right.start to right.includeStart
            right.start == null -> left.start to left.includeStart
            left.start > right.start -> left.start to left.includeStart
            left.start < right.start -> right.start to right.includeStart
            else -> left.start to (left.includeStart && right.includeStart)
        }
    }

    private fun minEnd(left: Interval, right: Interval): Pair<Double?, Boolean> {
        return when {
            left.end == null && right.end == null -> null to false
            left.end == null -> right.end to right.includeEnd
            right.end == null -> left.end to left.includeEnd
            left.end < right.end -> left.end to left.includeEnd
            left.end > right.end -> right.end to right.includeEnd
            else -> left.end to (left.includeEnd && right.includeEnd)
        }
    }

    private fun Interval.overlapsOrTouches(other: Interval): Boolean {
        if (end == null) return true
        if (other.start == null) return true

        val currentEnd = endValue(end)
        val nextStart = startValue(other.start)
        return when {
            nextStart < currentEnd -> true
            nextStart > currentEnd -> false
            else -> includeEnd || other.includeStart
        }
    }

    private fun Interval.mergeWith(other: Interval): Interval {
        val mergedStart = when {
            start == null && other.start == null -> null
            start == null -> null
            other.start == null -> null
            start < other.start -> start
            start > other.start -> other.start
            else -> start
        }

        val mergedIncludeStart = when {
            mergedStart == null -> false
            start == other.start -> includeStart || other.includeStart
            mergedStart == start -> includeStart
            else -> other.includeStart
        }

        val mergedEnd: Double?
        val mergedIncludeEnd: Boolean
        when {
            end == null || other.end == null -> {
                mergedEnd = null
                mergedIncludeEnd = false
            }

            end > other.end -> {
                mergedEnd = end
                mergedIncludeEnd = includeEnd
            }

            end < other.end -> {
                mergedEnd = other.end
                mergedIncludeEnd = other.includeEnd
            }

            else -> {
                mergedEnd = end
                mergedIncludeEnd = includeEnd || other.includeEnd
            }
        }

        return Interval(
            start = mergedStart,
            end = mergedEnd,
            includeStart = mergedIncludeStart,
            includeEnd = mergedIncludeEnd
        )
    }

    companion object {
        private const val EPSILON = 1e-9

        fun empty(): IntervalSet = IntervalSet(emptyList())

        fun allReal(): IntervalSet = IntervalSet(
            listOf(
                Interval(
                    start = null,
                    end = null,
                    includeStart = false,
                    includeEnd = false
                )
            )
        )

        fun from(interval: Interval): IntervalSet {
            val startNumeric = startValue(interval.start)
            val endNumeric = endValue(interval.end)
            val isValid = when {
                startNumeric < endNumeric -> true
                startNumeric > endNumeric -> false
                else -> interval.includeStart && interval.includeEnd
            }
            return if (isValid) IntervalSet(listOf(interval)) else empty()
        }

        fun greaterThan(value: Double, inclusive: Boolean): IntervalSet {
            return from(
                Interval(
                    start = value,
                    end = null,
                    includeStart = inclusive,
                    includeEnd = false
                )
            )
        }

        fun lessThan(value: Double, inclusive: Boolean): IntervalSet {
            return from(
                Interval(
                    start = null,
                    end = value,
                    includeStart = false,
                    includeEnd = inclusive
                )
            )
        }

        private fun startValue(value: Double?): Double = value ?: Double.NEGATIVE_INFINITY

        private fun endValue(value: Double?): Double = value ?: Double.POSITIVE_INFINITY

        private fun almostEqual(left: Double, right: Double): Boolean = abs(left - right) < EPSILON

        private fun formatNumber(value: Double): String {
            if (almostEqual(value, 0.0)) return "0"
            val rounded = round(value)
            if (almostEqual(value, rounded)) return rounded.toLong().toString()
            return String.format(Locale.US, "%.6f", value)
                .trimEnd('0')
                .trimEnd('.')
                .replace('.', ',')
        }
    }
}
