package ru.itmo

class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            statements.add(statement())
        }
        return statements
    }

    private fun statement(): Stmt {
        if (match(TokenType.Var)) return varDeclaration()
        if (match(TokenType.Print)) return printStatement()
        if (match(TokenType.If)) return ifStatement()
        if (match(TokenType.LeftBrace)) return Stmt.Block(block())
        if (match(TokenType.Func)) return functionDeclaration()
        if (match(TokenType.Return)) return returnStatement()
        return expressionStatement()
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.Identifier, "Ожидался идентификатор.")
        val initializer = if (match(TokenType.Equal)) {
            expression()
        } else {
            null
        }
        return Stmt.VarDecl(name, initializer)
    }

    private fun printStatement(): Stmt {
        consume(TokenType.LeftParen, "Ожидалась '(' после 'print'.")
        val value = expression()
        consume(TokenType.RightParen, "Ожидалась ')' после выражения.")
        return Stmt.Print(value)
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LeftParen, "Ожидалась '(' после 'if'.")
        val condition = expression()
        consume(TokenType.RightParen, "Ожидалась ')' после условия.")
        val thenBranch = statement()
        val elseBranch = if (match(TokenType.Else)) statement() else null
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RightBrace) && !isAtEnd()) {
            statements.add(statement())
        }
        consume(TokenType.RightBrace, "Ожидалась '}' после блока.")
        return statements
    }

    private fun functionDeclaration(): Stmt {
        val name = consume(TokenType.Identifier, "Ожидался идентификатор имени функции.")
        consume(TokenType.LeftParen, "Ожидалась '(' после имени функции.")

        val params = mutableListOf<Token>()
        if (!check(TokenType.RightParen)) {
            do {
                consume(TokenType.Var, "Ожидалось 'var' перед именем параметра.")
                val paramName = consume(TokenType.Identifier, "Ожидался идентификатор параметра.")
                params.add(paramName)
            } while (match(TokenType.Comma))
        }
        consume(TokenType.RightParen, "Ожидалась ')' после параметров.")
        val returnType: Token = if (match(TokenType.Arrow)) {
            // Допускаются только два варианта: var или void
            if (match(TokenType.Var)) {
                previous()
            } else if (match(TokenType.Void)) {
                previous()
            } else {
                throw error(peek(), "Ожидался тип возвращаемого значения (var или void).")
            }
        } else {
            Token(TokenType.Void, "void", -1)
        }

        consume(TokenType.LeftBrace, "Ожидалась '{' перед телом функции.")
        val body = block()
        return Stmt.FunctionDecl(name, params, returnType, body)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        // Если после return есть выражение, его разбираем, иначе оставляем value = null
        val value: Expr? = if (!check(TokenType.RightBrace) && peek().line == keyword.line) {
            expression()
        } else {
            null
        }
        return Stmt.Return(keyword, value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        return Stmt.Expression(expr)
    }

    private fun expression(): Expr {
        return equality()
    }

    // ==, !=
    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.EqualEqual, TokenType.NotEqual)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // <, <=, >, >=
    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.Less, TokenType.LessOrEqual, TokenType.Greater, TokenType.GreaterOrEqual)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // +, -
    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.Plus, TokenType.Minus)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // *, /, %
    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.Multiply, TokenType.Divide, TokenType.Reminder)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // - (unary)
    private fun unary(): Expr {
        if (match(TokenType.Minus)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    // Числа, строки, переменные, группировки
    private fun primary(): Expr {
        if (match(TokenType.Number, TokenType.StringLiteral)) {
            return Expr.Literal(previous().lexeme)
        }
        if (match(TokenType.Identifier)) {
            return Expr.Variable(previous())
        }
        if (match(TokenType.LeftParen)) {
            val expr = expression()
            consume(TokenType.RightParen, "Ожидалась ')' после выражения.")
            return Expr.Grouping(expr)
        }
        throw error(peek(), "Ожидалось выражение.")
    }

    // --- utils ---

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false else peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.Eof

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): ParseError {
        println("Ошибка синтаксического анализа у токена ${token.lexeme} на строке ${token.line}: $message")
        return ParseError()
    }

    private class ParseError : RuntimeException()
}