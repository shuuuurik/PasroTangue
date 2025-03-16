package ru.itmo

data class Token(val type: TokenType, val lexeme: String, val line: Int)

sealed class TokenType {
    // Ключевые слова
    data object Var : TokenType()
    data object Func : TokenType()
    data object If : TokenType()
    data object Else : TokenType()

    // Литералы
    data object Number : TokenType()
    data object StringLiteral : TokenType()
    data object Identifier : TokenType()

    // Операторы
    data object Plus : TokenType()
    data object Minus : TokenType()
    data object Multiply : TokenType()
    data object Divide : TokenType()
    data object Reminder : TokenType()

    data object Less : TokenType()
    data object LessOrEqual : TokenType()
    data object Greater : TokenType()
    data object GreaterOrEqual : TokenType()
    data object Equal : TokenType()
    data object EqualEqual : TokenType()
    data object NotEqual : TokenType()

    data object LeftParen : TokenType()
    data object RightParen : TokenType()
    data object LeftBrace : TokenType()
    data object RightBrace : TokenType()

    data object Print : TokenType()

    data object Eof : TokenType()
}