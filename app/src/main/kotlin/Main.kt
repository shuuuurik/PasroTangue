package ru.itmo

fun main() {
    val input = """
        var a = 5
        var b = 10
        var s
        {
            var x = 10
            print(-x)
            var y = 20
            print(y % x)
        }
        if (a < b) {
            print("a < b")
        } else {
            print("a >= b")
        }
        var n = 1 + 3 * 2
        print(n)
        var m = (1 + 3) * 2
        print(m)
    """.trimIndent()
    val lexer = Lexer(input)
    val tokens: List<Token>
    try {
        tokens = lexer.scanTokens()
    } catch (error: Exception) {
        println("Лексический анализ завершился с ошибками.")
        return
    }

    val parser = Parser(tokens)
    try {
        val statements = parser.parse()
        println("Синтаксический анализ выполнен успешно!")
        printAST(statements)
    } catch (error: Exception) {
        println("Синтаксический анализ завершился с ошибками.")
    }
}