package ru.itmo

import java.io.File

fun main() {
    val inputFile = File("app/src/main/kotlin/input.txt")
    val outputFile = File("app/src/main/kotlin/output.txt")

    outputFile.writeText("")

    val input = try {
        inputFile.readText().trim()
    } catch (e: Exception) {
        println("Ошибка: не удалось прочитать файл $inputFile")
        return
    }

    val lexer = Lexer(input)
    val tokens: List<Token> = try {
        lexer.scanTokens()
    } catch (error: Exception) {
        outputFile.writeText("Лексический анализ завершился с ошибками.")
        return
    }

    val parser = Parser(tokens)
    val output = StringBuilder()

    try {
        val statements = parser.parse()
        output.appendLine("Синтаксический анализ выполнен успешно!")
        printAST(statements, output)
    } catch (error: Exception) {
        output.appendLine("Синтаксический анализ завершился с ошибками.")
    }

    try {
        outputFile.writeText(output.toString())
        println("Результат сохранён в $outputFile")
    } catch (e: Exception) {
        println("Ошибка: не удалось записать в файл $outputFile")
    }
}