package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.InequalitySolution
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.model.NormalizedExpression
import com.zlo.inequa.domain.model.PolynomialExpression
import com.zlo.inequa.domain.model.SolutionStep
import com.zlo.inequa.domain.parser.InequalityPartsParser
import com.zlo.inequa.domain.parser.PolynomialExpressionParser

class QuadraticInequalitySolver(
    private val partsParser: InequalityPartsParser = InequalityPartsParser(),
    private val polynomialParser: PolynomialExpressionParser = PolynomialExpressionParser()
) {

    fun solve(
        input: InputExpression,
        variableName: String
    ): InequalitySolution {
        val parts = partsParser.parse(input.value)
        val reduced = polynomialParser.parse(parts.leftRaw)
            .minus(polynomialParser.parse(parts.rightRaw))
            .normalized()

        if (reduced.degree > 2) {
            throw IllegalArgumentException("Пока поддерживаются степени не выше второй.")
        }

        val normalizedValue = "${PolynomialSupport.formatPolynomial(reduced, variableName)} ${displaySymbol(parts.operator)} 0"
        val steps = mutableListOf<SolutionStep>()

        steps += SolutionStep(
            title = "Стандартная форма",
            description = "Переносим все члены влево: $normalizedValue."
        )

        val resultSet = when (reduced.degree) {
            0, 1 -> {
                steps += SolutionStep(
                    title = "Упрощение",
                    description = "После сокращения неравенство стало линейным."
                )
                PolynomialSupport.solveBySignChart(
                    numerator = reduced,
                    denominator = PolynomialExpression.constant(1.0),
                    operator = parts.operator
                )
            }

            else -> solveQuadratic(reduced, parts.operator, variableName, steps)
        }

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

    private fun solveQuadratic(
        expression: PolynomialExpression,
        operator: InequalityOperator,
        variableName: String,
        steps: MutableList<SolutionStep>
    ): IntervalSet {
        val a = expression.coefficientOf(2)
        val b = expression.coefficientOf(1)
        val c = expression.coefficientOf(0)
        val discriminant = b * b - 4.0 * a * c

        steps += SolutionStep(
            title = "Дискриминант",
            description = "Для ${PolynomialSupport.formatPolynomial(expression, variableName)} вычисляем D = ${PolynomialSupport.formatNumber(discriminant)}."
        )

        val roots = PolynomialSupport.realRoots(expression)
        when (roots.size) {
            0 -> {
                steps += SolutionStep(
                    title = "Корни",
                    description = "Действительных корней нет. Знак выражения определяется коэффициентом при ${variableName}²."
                )
            }

            1 -> {
                steps += SolutionStep(
                    title = "Корень",
                    description = "Критическая точка: $variableName = ${PolynomialSupport.formatNumber(roots.first())}."
                )
            }

            else -> {
                steps += SolutionStep(
                    title = "Корни",
                    description = "Критические точки: $variableName₁ = ${PolynomialSupport.formatNumber(roots[0])}, $variableName₂ = ${PolynomialSupport.formatNumber(roots[1])}."
                )
            }
        }

        val direction = if (a > 0) {
            "ветви параболы направлены вверх"
        } else {
            "ветви параболы направлены вниз"
        }
        steps += SolutionStep(
            title = "Знаки на промежутках",
            description = "Используем метод интервалов: $direction."
        )

        return PolynomialSupport.solveBySignChart(
            numerator = expression,
            denominator = PolynomialExpression.constant(1.0),
            operator = operator
        )
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
