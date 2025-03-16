package ru.itmo

class Lexer(private val input: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    private val tokens = mutableListOf<Token>()
    private var hasErrors = false

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            try {
                scanToken()
            } catch (e: Exception) {
                hasErrors = true
            }
        }
        if (hasErrors) {
            throw RuntimeException()
        }
        tokens.add(Token(TokenType.Eof, "", line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '+' -> addToken(TokenType.Plus)
            '-' -> {
                if (match('>'))
                    addToken(TokenType.Arrow)
                else
                    addToken(TokenType.Minus)
            }
            '*' -> addToken(TokenType.Multiply)
            '/' -> addToken(TokenType.Divide)
            '%' -> addToken(TokenType.Reminder)
            '<' -> addToken(if (match('=')) TokenType.LessOrEqual else TokenType.Less)
            '>' -> addToken(if (match('=')) TokenType.GreaterOrEqual else TokenType.Greater)
            '=' -> addToken(if (match('=')) TokenType.EqualEqual else TokenType.Equal)
            '!' -> {
                if (match('=')) {
                    addToken(TokenType.NotEqual)
                } else {
                    throw error("Ожидался символ = после !")
                }
            }
            '(' -> addToken(TokenType.LeftParen)
            ')' -> addToken(TokenType.RightParen)
            '{' -> addToken(TokenType.LeftBrace)
            '}' -> addToken(TokenType.RightBrace)
            ',' -> addToken(TokenType.Comma)
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            // остальные случаи: числа, идентификаторы, строковые литералы
            else -> {
                if (c.isDigit()) {
                    number()
                } else if (c.isLetter() || c == '_') {
                    identifier()
                } else if (c == '"') {
                    stringLiteral()
                }
                else {
                    throw error("Неизвестный символ: $c")
                }
            }
        }
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else input[current]
    }

    private fun advance(): Char = input[current++]

    private fun match(expected: Char): Boolean {
        if (isAtEnd())
            return false
        if (peek() != expected)
            return false
        current++
        return true
    }

    private fun addToken(type: TokenType) {
        val text = input.substring(start, current)
        tokens.add(Token(type, text, line))
    }

    private fun isAtEnd(): Boolean = current >= input.length

    private fun number() {
        // обработка числовых литералов
            while (!isAtEnd() && peek().isDigit()) {
            advance()
        }
        addToken(TokenType.Number)
    }

    private val keywords = mapOf(
        "var" to TokenType.Var,
        "func" to TokenType.Func,
        "if" to TokenType.If,
        "else" to TokenType.Else,
        "print" to TokenType.Print,
        "void" to TokenType.Void,
        "return" to TokenType.Return
    )

    private fun identifier() {
        while (!isAtEnd() && (peek().isLetterOrDigit() || peek() == '_')) {
            advance()
        }
        val text = input.substring(start, current)
        val type = keywords[text] ?: TokenType.Identifier
        addToken(type)
    }

    private fun stringLiteral() {
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                throw error("Ожидалась \"")
            }
            advance()
        }
        if (isAtEnd()) {
            throw error("Ожидалась \"")
        }
        advance() // пропускаем закрывающую кавычку
        addToken(TokenType.StringLiteral)
    }

    private fun error(message: String): TokenizeError {
        println("Ошибка лексического анализа на строке ${line}: $message")
        return TokenizeError()
    }

    private class TokenizeError : RuntimeException()
}