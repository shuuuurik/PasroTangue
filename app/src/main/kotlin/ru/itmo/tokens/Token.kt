package ru.itmo.tokens

data class Token(val type: TokenType, val lexeme: String, val line: Int)