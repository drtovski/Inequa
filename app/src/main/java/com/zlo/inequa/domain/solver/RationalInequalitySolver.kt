package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.InequalitySolution
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.model.NormalizedExpression
import com.zlo.inequa.domain.model.SolutionStep
import com.zlo.inequa.domain.parser.InequalityPartsParser
import com.zlo.inequa.domain.parser.RationalExpressionParser

class RationalInequalitySolver(
    private val partsParser: InequalityPartsParser = InequalityPartsParser(),
    private val rationalExpressionParser: RationalExpressionParser = RationalExpressionParser()
) {

    fun solve(
        input: InputExpression,
        variableName: String
    ): InequalitySolution {
        val parts = partsParser.parse(input.value)
        val reduced = rationalExpressionParser.parse(parts.leftRaw)
            .minus(rationalExpressionParser.parse(parts.rightRaw))
            .normalized()

        if (reduced.numerator.degree > 2 || reduced.denominator.degree > 2) {
            throw IllegalArgumentException("Пока поддерживаются дробные неравенства со степенью не выше второй.")
        }

        val normalizedValue = "${formatFraction(reduced, variableName)} ${displaySymbol(parts.operator)} 0"
        val steps = mutableListOf<SolutionStep>()

        steps += SolutionStep(
            title = "Стандартная форма",
            description = "Переносим всё влево и приводим к дроби: $normalizedValue."
        )

        val numeratorRoots = PolynomialSupport.realRoots(reduced.numerator)
        val denominatorRoots = PolynomialSupport.realRoots(reduced.denominator)
        steps += SolutionStep(
            title = "Критические точки",
            description = buildCriticalPointsDescription(
                numeratorRoots = numeratorRoots,
                denominatorRoots = denominatorRoots,
                variableName = variableName
            )
        )

        steps += SolutionStep(
            title = "Метод интервалов",
            description = "Отмечаем нули числителя и исключаем нули знаменателя, затем проверяем знак дроби на промежутках."
        )

        val resultSet = PolynomialSupport.solveBySignChart(
            numerator = reduced.numerator,
            denominator = reduced.denominator,
            operator = parts.operator
        )

        val shortAnswer = resultSet.toShortAnswer(variableName)
        val intervalAnswer = resultSet.toIntervalAnswer(variableName)
        steps += SolutionStep(
            title = "Ответ",
            description = "$shortAnswer, то есть $intervalAnswer."
        )

        return InequalitySolution(
            inputExpression = InputExpression(parts.normalizedInput),
            normalizedExpression = NormalizedExpression(normalizedValue),
            shortAnswer = shortAnswer,
            intervalAnswer = intervalAnswer,
            steps = steps,
            variableName = variableName,
            graphIntervals = resultSet.asList()
        )
    }

    private fun formatFraction(
        expression: com.zlo.inequa.domain.model.RationalExpression,
        variableName: String
    ): String {
        return if (expression.isPolynomial()) {
            PolynomialSupport.formatPolynomial(expression.numerator, variableName)
        } else {
            "(${PolynomialSupport.formatPolynomial(expression.numerator, variableName)}) / (${PolynomialSupport.formatPolynomial(expression.denominator, variableName)})"
        }
    }

    private fun buildCriticalPointsDescription(
        numeratorRoots: List<Double>,
        denominatorRoots: List<Double>,
        variableName: String
    ): String {
        val numeratorPart = if (numeratorRoots.isEmpty()) {
            "у числителя действительных нулей нет"
        } else {
            "нули числителя: ${numeratorRoots.joinToString(", ") { "$variableName = ${PolynomialSupport.formatNumber(it)}" }}"
        }

        val denominatorPart = if (denominatorRoots.isEmpty()) {
            "знаменатель не обращается в ноль"
        } else {
            "нули знаменателя: ${denominatorRoots.joinToString(", ") { "$variableName = ${PolynomialSupport.formatNumber(it)}" }}"
        }

        return "$numeratorPart; $denominatorPart."
    }

    private fun displaySymbol(operator: InequalityOperator): String {
        return when (operator) {
            InequalityOperator.GREATER -> ">"
            InequalityOperator.LESS -> "<"
            InequalityOperator.GREATER_OR_EQUAL -> "≥"
            InequalityOperator.LESS_OR_EQUAL -> "≤"
        }
    }
}
