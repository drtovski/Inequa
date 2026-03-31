package com.zlo.inequa.domain.parser

import com.zlo.inequa.domain.model.InequalityOperator
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.model.ParsedLinearInequality

class LinearInequalityParser(
    private val partsParser: InequalityPartsParser = InequalityPartsParser(),
    private val expressionParser: LinearExpressionParser = LinearExpressionParser()
) {

    fun parse(
        input: InputExpression,
        variableName: String = "x"
    ): ParsedLinearInequality {
        val parts = partsParser.parse(input.value)

        return ParsedLinearInequality(
            source = InputExpression(parts.normalizedInput),
            left = expressionParser.parse(parts.leftRaw),
            right = expressionParser.parse(parts.rightRaw),
            operator = parts.operator,
            variableName = variableName
        )
    }
}
