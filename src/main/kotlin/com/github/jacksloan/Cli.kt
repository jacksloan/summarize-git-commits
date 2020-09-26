package com.github.jacksloan

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.jacksloan.functions.commitCountByAuthor
import com.github.jacksloan.functions.findAllMatchingCommits
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) = Cli().main(args)

class Cli : CliktCommand() {
    private val parentPath: String by option(help = "Path to parent directory containing git repositories").prompt("Path to parent directory containing git repositories")
    private val regexPattern: String by option(help = "Regex string to summarize").prompt("Regex string to summarize")

    @Throws(IllegalArgumentException::class)
    override fun run() {
        val parentDir: File = Paths.get(parentPath).toFile()

        if (parentDir.isFile) {
            throw IllegalArgumentException("Provided path is not a directory")
        }

        val commits = findAllMatchingCommits(parentDir, regexPattern)

        commitCountByAuthor(commits).forEach { println("Name: ${it.author}, Matches: ${it.count}") }
    }
}
