package com.zlo.inequa.domain.parser

import com.zlo.inequa.domain.model.PolynomialExpression

class PolynomialExpressionParser {

    fun parse(rawExpression: String): PolynomialExpression {
        val normalized = normalize(rawExpression)
        if (normalized.isBlank()) {
            throw IllegalArgumentException("Пустое выражение.")
        }

        val tokens = tokenize(normalized)
        val stream = TokenStream(insertImplicitMultiplication(tokens))
        val result = parseExpression(stream)

        if (!stream.isAtEnd()) {
            throw IllegalArgumentException("Некорректный формат выражения.")
        }

        return result.normalized()
    }

    private fun parseExpression(stream: TokenStream): PolynomialExpression {
        var value = parseTerm(stream)

        while (true) {
            value = when (stream.peek()) {
                Token.Plus -> {
                    stream.consume()
                    value.plus(parseTerm(stream))
                }

                Token.Minus -> {
                    stream.consume()
                    value.minus(parseTerm(stream))
                }

                else -> return value
            }
        }
    }

    private fun parseTerm(stream: TokenStream): PolynomialExpression {
        var value = parseSignedPower(stream)

        while (true) {
            value = when (stream.peek()) {
                Token.Multiply -> {
                    stream.consume()
                    value.multiply(parseSignedPower(stream))
                }

                Token.Divide -> {
                    stream.consume()
                    val denominator = parseSignedPower(stream)
                    if (!denominator.isConstant()) {
                        throw IllegalArgumentException("Деление на выражение с переменной здесь не поддерживается.")
                    }
                    value.divideByConstant(denominator.constantTerm())
                }

                else -> return value
            }
        }
    }

    private fun parseSignedPower(stream: TokenStream): PolynomialExpression {
        return when (stream.peek()) {
            Token.Plus -> {
                stream.consume()
                parseSignedPower(stream)
            }

            Token.Minus -> {
                stream.consume()
                PolynomialExpression.constant(-1.0).multiply(parseSignedPower(stream))
            }

            else -> parsePower(stream)
        }
    }

    private fun parsePower(stream: TokenStream): PolynomialExpression {
        var value = parsePrimary(stream)

        while (stream.peek() == Token.Caret) {
            stream.consume()
            val exponentToken = stream.consume()
            val exponent = when (exponentToken) {
                is Token.Number -> exponentToken.value
                else -> throw IllegalArgumentException("После ^ ожидается целый показатель степени.")
            }

            if (exponent < 0 || exponent != exponent.toInt().toDouble()) {
                throw IllegalArgumentException("Поддерживаются только неотрицательные целые показатели степени.")
            }

            value = value.pow(exponent.toInt())
        }

        return value
    }

    private fun parsePrimary(stream: TokenStream): PolynomialExpression {
        return when (val token = stream.peek()) {
            is Token.Number -> {
                stream.consume()
                PolynomialExpression.constant(token.value)
            }

            Token.Variable -> {
                stream.consume()
                PolynomialExpression.X
            }

            Token.LeftParenthesis -> {
                stream.consume()
                val value = parseExpression(stream)
                stream.consumeExpected(Token.RightParenthesis)
                value
            }

            else -> throw IllegalArgumentException("Ожидалось число, переменная или скобка.")
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

                '^' -> {
                    tokens += Token.Caret
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

                        val rawNumber = expression.substring(start, index)
                        val value = rawNumber.toDoubleOrNull()
                            ?: throw IllegalArgumentException("Не удалось прочитать число: $rawNumber")
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
        val previousCanEndValue = previous is Token.Number ||
            previous == Token.Variable ||
            previous == Token.RightParenthesis

        val currentCanStartValue = current is Token.Number ||
            current == Token.Variable ||
            current == Token.LeftParenthesis

        return previousCanEndValue && currentCanStartValue
    }

    private fun normalize(rawExpression: String): String {
        return rawExpression
            .replace(" ", "")
            .replace(",", ".")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("⁴", "^4")
            .replace("⁵", "^5")
            .replace("⁶", "^6")
            .replace("⁷", "^7")
            .replace("⁸", "^8")
            .replace("⁹", "^9")
            .replace("X", "x")
        }

    private sealed interface Token {
        data class Number(val value: Double) : Token
        data object Variable : Token
        data object Plus : Token
        data object Minus : Token
        data object Multiply : Token
        data object Divide : Token
        data object Caret : Token
        data object LeftParenthesis : Token
        data object RightParenthesis : Token
        data object End : Token
    }

    private class TokenStream(tokens: List<Token>) {
        private val items = tokens + Token.End
        private var index = 0

        fun peek(): Token = items[index]

        fun consume(): Token = items[index++]

        fun consumeExpected(expected: Token) {
            val current = consume()
            if (current != expected) {
                throw IllegalArgumentException("Ошибка со скобками в выражении.")
            }
        }

        fun isAtEnd(): Boolean = peek() == Token.End
    }
}
