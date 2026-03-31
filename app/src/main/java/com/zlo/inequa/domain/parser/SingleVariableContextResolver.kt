package com.zlo.inequa.domain.parser

import com.zlo.inequa.domain.model.SingleVariableContext

class SingleVariableContextResolver {

    fun resolve(rawExpression: String): SingleVariableContext {
        val variables = variableRegex.findAll(rawExpression)
            .map { it.value.lowercase() }
            .distinct()
            .toList()

        if (variables.size > 1) {
            throw IllegalArgumentException("Пока поддерживается одно неизвестное: x, y или z.")
        }

        val variableName = variables.singleOrNull() ?: "x"
        val canonicalExpression = rawExpression.replace(variableRegex, "x")

        return SingleVariableContext(
            variableName = variableName,
            canonicalExpression = canonicalExpression
        )
    }

    private companion object {
        private val variableRegex = Regex("[xXyYzZ]")
    }
}
