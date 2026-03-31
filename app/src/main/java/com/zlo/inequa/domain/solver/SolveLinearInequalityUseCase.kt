package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalitySolution
import com.zlo.inequa.domain.model.InequalityType
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.parser.InequalityInputNormalizer
import com.zlo.inequa.domain.parser.LinearInequalityParser
import com.zlo.inequa.domain.parser.SingleVariableContextResolver

class SolveLinearInequalityUseCase(
    private val normalizer: InequalityInputNormalizer = InequalityInputNormalizer(),
    private val variableContextResolver: SingleVariableContextResolver = SingleVariableContextResolver(),
    private val typeDetector: InequalityTypeDetector = InequalityTypeDetector(),
    private val parser: LinearInequalityParser = LinearInequalityParser(),
    private val solver: LinearInequalitySolver = LinearInequalitySolver(),
    private val quadraticInequalitySolver: QuadraticInequalitySolver = QuadraticInequalitySolver(),
    private val rationalInequalitySolver: RationalInequalitySolver = RationalInequalitySolver(),
    private val absoluteInequalitySolver: AbsoluteInequalitySolver = AbsoluteInequalitySolver(),
    private val rootInequalitySolver: RootInequalitySolver = RootInequalitySolver()
) {
    operator fun invoke(inputExpression: InputExpression): InequalitySolution {
        val variableContext = variableContextResolver.resolve(inputExpression.value)
        val normalizedInput = normalizer.normalizeForSolve(variableContext.canonicalExpression)
        val inequalityType = typeDetector.detect(normalizedInput)

        return when (inequalityType) {
            InequalityType.LINEAR -> {
                val parsed = parser.parse(
                    input = InputExpression(normalizedInput),
                    variableName = variableContext.variableName
                )
                solver.solve(parsed)
            }

            InequalityType.QUADRATIC -> {
                quadraticInequalitySolver.solve(
                    input = InputExpression(normalizedInput),
                    variableName = variableContext.variableName
                )
            }

            InequalityType.RATIONAL -> {
                rationalInequalitySolver.solve(
                    input = InputExpression(normalizedInput),
                    variableName = variableContext.variableName
                )
            }

            InequalityType.ABSOLUTE -> {
                absoluteInequalitySolver.solve(
                    input = InputExpression(normalizedInput),
                    variableName = variableContext.variableName
                )
            }

            InequalityType.ROOT -> {
                rootInequalitySolver.solve(
                    input = InputExpression(normalizedInput),
                    variableName = variableContext.variableName
                )
            }

            else -> {
                throw IllegalArgumentException(messageForUnsupportedType(inequalityType))
            }
        }
    }

    private fun messageForUnsupportedType(type: InequalityType): String {
        return when (type) {
            InequalityType.LINEAR -> ""
            InequalityType.ROOT -> ""
            InequalityType.QUADRATIC -> ""
            InequalityType.POWER -> "Пока поддерживаются степени не выше второй."
            InequalityType.RATIONAL -> ""
            InequalityType.ABSOLUTE -> ""
            InequalityType.COMPOUND -> "Составные неравенства пока не поддерживаются. Сейчас доступно решение линейных."
            InequalityType.SYSTEM -> "Системы неравенств пока не поддерживаются. Сейчас доступно решение линейных."
        }
    }
}
