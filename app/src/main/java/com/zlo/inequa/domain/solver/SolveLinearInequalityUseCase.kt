package com.zlo.inequa.domain.solver

import com.zlo.inequa.domain.model.InequalitySolution
import com.zlo.inequa.domain.model.InequalityType
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.parser.InequalityInputNormalizer
import com.zlo.inequa.domain.parser.LinearInequalityParser

class SolveLinearInequalityUseCase(
    private val normalizer: InequalityInputNormalizer = InequalityInputNormalizer(),
    private val typeDetector: InequalityTypeDetector = InequalityTypeDetector(),
    private val parser: LinearInequalityParser = LinearInequalityParser(),
    private val solver: LinearInequalitySolver = LinearInequalitySolver()
) {
    operator fun invoke(inputExpression: InputExpression): InequalitySolution {
        val normalizedInput = normalizer.normalizeForSolve(inputExpression.value)
        val inequalityType = typeDetector.detect(normalizedInput)

        if (inequalityType != InequalityType.LINEAR) {
            throw IllegalArgumentException(messageForUnsupportedType(inequalityType))
        }

        val parsed = parser.parse(InputExpression(normalizedInput))
        return solver.solve(parsed)
    }

    private fun messageForUnsupportedType(type: InequalityType): String {
        return when (type) {
            InequalityType.LINEAR -> ""
            InequalityType.QUADRATIC -> "Квадратные неравенства пока не поддерживаются. Сейчас доступно решение линейных."
            InequalityType.RATIONAL -> "Дробно-рациональные неравенства пока не поддерживаются. Сейчас доступно решение линейных."
            InequalityType.ABSOLUTE -> "Неравенства с модулем пока не поддерживаются. Сейчас доступно решение линейных."
            InequalityType.COMPOUND -> "Составные неравенства пока не поддерживаются. Сейчас доступно решение линейных."
            InequalityType.SYSTEM -> "Системы неравенств пока не поддерживаются. Сейчас доступно решение линейных."
        }
    }
}
