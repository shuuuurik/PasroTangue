package ru.itmo

fun main() {
    val input = """
        var a = 5
        var b = 10
        if (a < b) {
            print("a < b")
        } else {
            print("a >= b")
        }
        var s = "Hello, world!
        var c = a ! b
    """.trimIndent()
    val lexer = Lexer(input)
    val tokens = lexer.scanTokens()
    tokens.forEach {
        println(it)
    }

}