package ru.itmo.ast.nodes

import ru.itmo.tokens.Token

sealed class Stmt {
    data class Expression(val expression: Expr) : Stmt()
    data class Print(val expression: Expr) : Stmt()
    data class VarDecl(val name: Token, val initializer: Expr?) : Stmt()
    data class Block(val statements: List<Stmt>) : Stmt()
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
    data class FunctionDecl(val name: Token, val params: List<Token>,
                            val returnType: Token?, val body: List<Stmt>): Stmt()
    data class Return(val keyword: Token, val value: Expr?) : Stmt()
}