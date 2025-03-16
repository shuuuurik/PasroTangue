package ru.itmo

sealed class Expr {

    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class Variable(val name: Token) : Expr()
}

sealed class Stmt {

    data class Expression(val expression: Expr) : Stmt()
    data class Print(val expression: Expr) : Stmt()
    data class VarDecl(val name: Token, val initializer: Expr?) : Stmt()
    data class Block(val statements: List<Stmt>) : Stmt()
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
}

fun printAST(statements: List<Stmt>) {
    println("Program")
    val lastIndex = statements.size - 1
    statements.forEachIndexed { index, stmt ->
        printStmtTree(stmt, "", index == lastIndex)
    }
}

private fun printStmtTree(stmt: Stmt, prefix: String, isLast: Boolean) {
    val branch = if (isLast) "└──" else "├──"
    when (stmt) {
        is Stmt.VarDecl -> {
            println("$prefix$branch VarDecl (${stmt.name.lexeme})")
            stmt.initializer?.let {
                printExprTree("Initializer", it, prefix + if (isLast) "    " else "│   ", true)
            }
        }
        is Stmt.Print -> {
            println("$prefix$branch Print")
            printExprTree("Expression", stmt.expression, prefix + if (isLast) "    " else "│   ", true)
        }
        is Stmt.Expression -> {
            println("$prefix$branch Expression")
            printExprTree("", stmt.expression, prefix + if (isLast) "    " else "│   ", true)
        }
        is Stmt.Block -> {
            println("$prefix$branch Block")
            val lastIdx = stmt.statements.size - 1
            stmt.statements.forEachIndexed { idx, s ->
                printStmtTree(s, prefix + if (isLast) "    " else "│   ", idx == lastIdx)
            }
        }
        is Stmt.If -> {
            println("$prefix$branch If")
            printExprTree("Condition", stmt.condition, prefix + if (isLast) "    " else "│   ", true)
            // Then-branch
            println(prefix + if (isLast) "    " else "│   " + "├── Then")
            printStmtTree(stmt.thenBranch, prefix + if (isLast) "    " else "│   " + "│   ", true)
            // Else-branch
            stmt.elseBranch?.let {
                println(prefix + if (isLast) "    " else "│   " + "└── Else")
                printStmtTree(it, prefix + if (isLast) "    " else "│   " + "    ", true)
            }
        }
    }
}

private fun printExprTree(label: String, expr: Expr, prefix: String, isLast: Boolean) {
    val branch = if (label.isNotEmpty()) "├──" else "└──"
    val nodeLabel = if (label.isNotEmpty()) "$label: " else ""
    when (expr) {
        is Expr.Literal -> println("$prefix$branch ${nodeLabel}Literal(${expr.value})")
        is Expr.Variable -> println("$prefix$branch ${nodeLabel}Variable(${expr.name.lexeme})")
        is Expr.Binary -> {
            println("$prefix$branch ${nodeLabel}Binary (${expr.operator.lexeme})")
            printExprTree("Left", expr.left, prefix + if (isLast) "    " else "│   ", false)
            printExprTree("Right", expr.right, prefix + if (isLast) "    " else "│   ", true)
        }
        is Expr.Unary -> {
            println("$prefix$branch ${nodeLabel}Unary (${expr.operator.lexeme})")
            printExprTree("Right", expr.right, prefix + if (isLast) "    " else "│   ", true)
        }
        is Expr.Grouping -> {
            println("$prefix$branch ${nodeLabel}Grouping")
            printExprTree("", expr.expression, prefix + if (isLast) "    " else "│   ", true)
        }
    }
}