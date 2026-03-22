package com.zlo.inequa.domain.parser

import com.zlo.inequa.domain.model.LinearExpression

class LinearExpressionParser {

    fun parse(rawExpression: String): LinearExpression {
        val normalized = rawExpression
            .replace(" ", "")
            .replace(",", ".")
            .replace("X", "x")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("x²", "x^2")

        if (normalized.isBlank()) {
            throw IllegalArgumentException("Пустое выражение.")
        }

        if (normalized.contains("^")) {
            throw IllegalArgumentException("Пока поддерживаются только линейные неравенства без степеней.")
        }

        val tokens = tokenize(normalized)
        val stream = TokenStream(insertImplicitMultiplication(tokens))
        val result = parseExpression(stream)

        if (!stream.isAtEnd()) {
            throw IllegalArgumentException("Некорректный формат выражения.")
        }

        return result.normalized()
    }

    private fun parseExpression(stream: TokenStream): LinearExpression {
        var value = parseTerm(stream)
        while (true) {
            value = when (stream.peek()) {
                Token.Plus -> {
                    stream.consume()
                    value + parseTerm(stream)
                }

                Token.Minus -> {
                    stream.consume()
                    value - parseTerm(stream)
                }

                else -> return value
            }
        }
    }

    private fun parseTerm(stream: TokenStream): LinearExpression {
        var value = parseFactor(stream)
        while (true) {
            value = when (stream.peek()) {
                Token.Multiply -> {
                    stream.consume()
                    value.multiply(parseFactor(stream))
                }

                Token.Divide -> {
                    stream.consume()
                    value.divide(parseFactor(stream))
                }

                else -> return value
            }
        }
    }

    private fun parseFactor(stream: TokenStream): LinearExpression {
        return when (val token = stream.peek()) {
            Token.Plus -> {
                stream.consume()
                parseFactor(stream)
            }

            Token.Minus -> {
                stream.consume()
                LinearExpression.ZERO - parseFactor(stream)
            }

            is Token.Number -> {
                stream.consume()
                LinearExpression.constant(token.value)
            }

            Token.Variable -> {
                stream.consume()
                LinearExpression.X
            }

            Token.LeftParenthesis -> {
                stream.consume()
                val value = parseExpression(stream)
                stream.consumeExpected(Token.RightParenthesis)
                value
            }

            else -> throw IllegalArgumentException("Ожидалось число, x или скобка.")
        }
    }

    private fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0

        while (index < expression.length) {
            when (val current = expression[index]) {
                '+' -> {
                    tokens += Token.Plus
                    index += 1
                }

                '-' -> {
                    tokens += Token.Minus
                    index += 1
                }

                '*' -> {
                    tokens += Token.Multiply
                    index += 1
                }

                '/' -> {
                    tokens += Token.Divide
                    index += 1
                }

                '(' -> {
                    tokens += Token.LeftParenthesis
                    index += 1
                }

                ')' -> {
                    tokens += Token.RightParenthesis
                    index += 1
                }

                'x' -> {
                    tokens += Token.Variable
                    index += 1
                }

                else -> {
                    if (current.isDigit() || current == '.') {
                        val start = index
                        var dots = 0
                        while (index < expression.length && (expression[index].isDigit() || expression[index] == '.')) {
                            if (expression[index] == '.') {
                                dots += 1
                            }
                            index += 1
                        }

                        if (dots > 1) {
                            throw IllegalArgumentException("Некорректное десятичное число.")
                        }

                        val numberRaw = expression.substring(start, index)
                        val value = numberRaw.toDoubleOrNull()
                            ?: throw IllegalArgumentException("Не удалось прочитать число: $numberRaw")
                        tokens += Token.Number(value)
                    } else {
                        throw IllegalArgumentException("Недопустимый символ: $current")
                    }
                }
            }
        }

        return tokens
    }

    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) return emptyList()

        val result = mutableListOf<Token>()
        var previous: Token? = null

        tokens.forEach { current ->
            if (previous != null && shouldInsertMultiply(previous!!, current)) {
                result += Token.Multiply
            }
            result += current
            previous = current
        }

        return result
    }

    private fun shouldInsertMultiply(previous: Token, current: Token): Boolean {
        val previousCanEndValue = previous is Token.Number || previous == Token.Variable || previous == Token.RightParenthesis
        val currentCanStartValue = current is Token.Number || current == Token.Variable || current == Token.LeftParenthesis
        return previousCanEndValue && currentCanStartValue
    }

    private sealed interface Token {
        data class Number(val value: Double) : Token
        data object Variable : Token
        data object Plus : Token
        data object Minus : Token
        data object Multiply : Token
        data object Divide : Token
        data object LeftParenthesis : Token
        data object RightParenthesis : Token
        data object End : Token
    }

    private class TokenStream(tokens: List<Token>) {
        private val items = tokens + Token.End
        private var index = 0

        fun peek(): Token = items[index]

        fun consume(): Token {
            return items[index++]
        }

        fun consumeExpected(expected: Token) {
            val current = consume()
            if (current != expected) {
                throw IllegalArgumentException("Ошибка со скобками в выражении.")
            }
        }

        fun isAtEnd(): Boolean = peek() == Token.End
    }
}
