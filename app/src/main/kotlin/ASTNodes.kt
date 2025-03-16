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
    data class FunctionDecl(val name: Token, val params: List<Token>,
                            val returnType: Token?, val body: List<Stmt>): Stmt()
    data class Return(val keyword: Token, val value: Expr?) : Stmt()
}

fun printAST(statements: List<Stmt>, output: Appendable) {
    output.append("Program\n")
    val lastIndex = statements.size - 1
    statements.forEachIndexed { index, stmt ->
        printStmtTree(stmt, "", index == lastIndex, output)
    }
}

private fun printStmtTree(stmt: Stmt, prefix: String, isLast: Boolean, output: Appendable) {
    val branch = if (isLast) "└──" else "├──"
    when (stmt) {
        is Stmt.VarDecl -> {
            output.append("$prefix$branch VarDecl (${stmt.name.lexeme})\n")
            stmt.initializer?.let {
                printExprTree("Initializer", it, prefix + if (isLast) "    " else "│   ", true, output)
            }
        }
        is Stmt.Print -> {
            output.append("$prefix$branch Print\n")
            printExprTree("Expression", stmt.expression, prefix + if (isLast) "    " else "│   ", true, output)
        }
        is Stmt.Expression -> {
            output.append("$prefix$branch Expression\n")
            printExprTree("", stmt.expression, prefix + if (isLast) "    " else "│   ", true, output)
        }
        is Stmt.Block -> {
            output.append("$prefix$branch Block\n")
            val lastIdx = stmt.statements.size - 1
            stmt.statements.forEachIndexed { idx, s ->
                printStmtTree(s, prefix + if (isLast) "    " else "│   ", idx == lastIdx, output)
            }
        }
        is Stmt.If -> {
            output.append("$prefix$branch If\n")
            printExprTree("Condition", stmt.condition, prefix + (if (isLast) "    " else "│   "), false, output)
            // Then-branch
            output.append(prefix + (if (isLast) "    " else "│   ") + "├── Then\n")
            printStmtTree(stmt.thenBranch, prefix + (if (isLast) "    " else "│   ") + "│   ", true, output)
            // Else-branch
            stmt.elseBranch?.let {
                output.append(prefix + (if (isLast) "    " else "│   ") + "└── Else\n")
                printStmtTree(it, prefix + (if (isLast) "    " else "│   ") + "    ", true, output)
            }
        }
        is Stmt.FunctionDecl -> {
            output.append("$prefix$branch FunctionDecl (${stmt.name.lexeme})\n")

            // Параметры
            if (stmt.params.isNotEmpty()) {
                output.append("$prefix${if (isLast) "    " else "│   "}├── Parameters\n")
                val lastParamIdx = stmt.params.size - 1
                stmt.params.forEachIndexed { idx, param ->
                    output.append("$prefix${if (isLast) "    " else "│   "}│   ${if (idx == lastParamIdx) "└──" else "├──"} ${param.lexeme}\n")
                }
            }

            // Возвращаемый тип
            output.append("$prefix${if (isLast) "    " else "│   "}├── ReturnType: ${stmt.returnType?.lexeme ?: "void"}\n")

            // Тело функции
            output.append("$prefix${if (isLast) "    " else "│   "}└── Body\n")
            val lastStmtIdx = stmt.body.size - 1
            stmt.body.forEachIndexed { idx, bodyStmt ->
                printStmtTree(bodyStmt, prefix + (if (isLast) "    " else "│   ") + "    ", idx == lastStmtIdx, output)
            }
        }
        is Stmt.Return -> {
            output.append("$prefix$branch Return\n")
            stmt.value?.let {
                printExprTree("Value", it, prefix + (if (isLast) "    " else "│   "), true, output)
            }
        }
    }
}

private fun printExprTree(label: String, expr: Expr, prefix: String, isLast: Boolean, output: Appendable) {
    val branch = if (isLast) "└──" else "├──"
    val nodeLabel = if (label.isNotEmpty()) "$label: " else ""
    when (expr) {
        is Expr.Literal -> output.append("$prefix$branch ${nodeLabel}Literal(${expr.value})\n")
        is Expr.Variable -> output.append("$prefix$branch ${nodeLabel}Variable(${expr.name.lexeme})\n")
        is Expr.Binary -> {
            output.append("$prefix$branch ${nodeLabel}Binary (${expr.operator.lexeme})\n")
            printExprTree("Left", expr.left, prefix + if (isLast) "    " else "│   ", false, output)
            printExprTree("Right", expr.right, prefix + if (isLast) "    " else "│   ", true, output)
        }
        is Expr.Unary -> {
            output.append("$prefix$branch ${nodeLabel}Unary (${expr.operator.lexeme})\n")
            printExprTree("Right", expr.right, prefix + if (isLast) "    " else "│   ", true, output)
        }
        is Expr.Grouping -> {
            output.append("$prefix$branch ${nodeLabel}Grouping\n")
            printExprTree("", expr.expression, prefix + if (isLast) "    " else "│   ", true, output)
        }
    }
}