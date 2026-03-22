package com.zlo.inequa.domain.formatter

import com.zlo.inequa.domain.parser.InequalityInputNormalizer

class MathExpressionFormatter(
    private val normalizer: InequalityInputNormalizer = InequalityInputNormalizer()
) {

    fun format(rawInput: String): String {
        val normalized = normalizer.normalizeForDisplay(rawInput)
        if (normalized.isBlank()) return ""

        val tokens = markUnaryMinus(tokenize(normalized))
        if (tokens.isEmpty()) return ""

        val builder = StringBuilder()
        var previous: DisplayToken? = null

        tokens.forEach { current ->
            if (needsSpaceBefore(previous, current)) {
                builder.append(' ')
            }
            builder.append(toDisplayText(current))
            previous = current
        }

        return builder.toString().trim()
    }

    private fun tokenize(input: String): List<Token> {
        val result = mutableListOf<Token>()
        var index = 0

        while (index < input.length) {
            when {
                input[index].isWhitespace() -> {
                    index += 1
                }

                input.startsWith(">=", index) -> {
                    result += Token(">=", TokenKind.Comparator)
                    index += 2
                }

                input.startsWith("<=", index) -> {
                    result += Token("<=", TokenKind.Comparator)
                    index += 2
                }

                else -> {
                    val current = input[index]
                    when {
                        current == '>' || current == '<' -> {
                            result += Token(current.toString(), TokenKind.Comparator)
                            index += 1
                        }

                        current == '+' || current == '-' || current == '/' || current == '=' || current == '*' -> {
                            result += Token(current.toString(), TokenKind.Operator)
                            index += 1
                        }

                        current == '(' -> {
                            result += Token("(", TokenKind.OpenParenthesis)
                            index += 1
                        }

                        current == ')' -> {
                            result += Token(")", TokenKind.CloseParenthesis)
                            index += 1
                        }

                        current == '|' -> {
                            result += Token("|", TokenKind.AbsoluteBar)
                            index += 1
                        }

                        current == 'x' -> {
                            if (input.startsWith("x^2", index)) {
                                result += Token("x²", TokenKind.Value)
                                index += 3
                            } else if (input.startsWith("x²", index)) {
                                result += Token("x²", TokenKind.Value)
                                index += 2
                            } else {
                                result += Token("x", TokenKind.Value)
                                index += 1
                            }
                        }

                        current.isDigit() || current == '.' || current == ',' -> {
                            val start = index
                            while (index < input.length && (input[index].isDigit() || input[index] == '.' || input[index] == ',')) {
                                index += 1
                            }
                            result += Token(input.substring(start, index), TokenKind.Value)
                        }

                        current.isLetter() -> {
                            val start = index
                            while (index < input.length && input[index].isLetter()) {
                                index += 1
                            }
                            result += Token(input.substring(start, index), TokenKind.Word)
                        }

                        else -> {
                            result += Token(current.toString(), TokenKind.Unknown)
                            index += 1
                        }
                    }
                }
            }
        }

        return result
    }

    private fun markUnaryMinus(tokens: List<Token>): List<DisplayToken> {
        val result = mutableListOf<DisplayToken>()
        var previous: DisplayToken? = null

        tokens.forEach { token ->
            val unaryMinus = token.kind == TokenKind.Operator &&
                token.text == "-" &&
                (previous == null ||
                    previous.isBinaryOperator ||
                    previous.token.kind == TokenKind.OpenParenthesis ||
                    previous.token.kind == TokenKind.AbsoluteBar)

            val marked = DisplayToken(
                token = token,
                isUnaryMinus = unaryMinus
            )
            result += marked
            previous = marked
        }

        return result
    }

    private fun needsSpaceBefore(previous: DisplayToken?, current: DisplayToken): Boolean {
        if (previous == null) return false
        if (current.token.kind == TokenKind.CloseParenthesis) return false
        if (previous.token.kind == TokenKind.OpenParenthesis) return false
        if (current.token.kind == TokenKind.AbsoluteBar || previous.token.kind == TokenKind.AbsoluteBar) return false
        if (current.isBinaryOperator) return true
        if (previous.isBinaryOperator) return true
        if (current.token.kind == TokenKind.Word || previous.token.kind == TokenKind.Word) return true
        return false
    }

    private fun toDisplayText(token: DisplayToken): String {
        return when (token.token.text) {
            ">=" -> "≥"
            "<=" -> "≤"
            else -> token.token.text.replace('.', ',')
        }
    }

    private data class Token(
        val text: String,
        val kind: TokenKind
    )

    private data class DisplayToken(
        val token: Token,
        val isUnaryMinus: Boolean
    ) {
        val isBinaryOperator: Boolean
            get() = when (token.kind) {
                TokenKind.Comparator -> true
                TokenKind.Operator -> !(token.text == "-" && isUnaryMinus)
                else -> false
            }
    }

    private enum class TokenKind {
        Value,
        Comparator,
        Operator,
        OpenParenthesis,
        CloseParenthesis,
        AbsoluteBar,
        Word,
        Unknown
    }
}
