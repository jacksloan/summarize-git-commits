package com.github.jacksloan

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import java.io.File
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

fun main(args: Array<String>) = Cli().main(args)

class Cli : CliktCommand() {
    private val parentPath: String by option(help = "The parent directory containing your git repos").prompt("Path to directory containing repos")
    private val regexPattern: String by option(help = "The regex pattern to match in all git logs").prompt("Regex string to summarize")

    override fun run() {
        val parentDir: File = Paths.get(parentPath).toFile()

        if (!parentDir.isDirectory) throw IllegalArgumentException("Provided path is not a directory")

        doSummary(parentDir, regexPattern)
                .sortedByDescending { it.count }
                .forEach { println(it) }
    }
}


data class Commit(
        var commit: String = "",
        var author: String = "",
        var date: OffsetDateTime = OffsetDateTime.MIN,
        var message: String = "") {

}

data class CommitSummary(
        val author: String,
        val regexString: String,
        var count: Int)

fun doSummary(file: File, matchString: String): List<CommitSummary> {
    val summaries = mutableMapOf<String, ArrayList<CommitSummary>>()
    summarizeReposRecursive(file, matchString, summaries)

    return summaries.keys.map {
        CommitSummary(it, summaries[it]?.first()?.regexString ?: "UNKNOWN",
                summaries[it]?.sumBy { it.count } ?: 0)
    }
}

/**
 * A recursive function that walks a provided [fileOrDirectory].
 * When the [fileOrDirectory] is a git directory,
 * summarize the git messages by author using the [regexString]
 * and add the results to the [combinedMap].
 * Escape the recursion when the [depth] reaches the [maxDepth]
 */
fun summarizeReposRecursive(fileOrDirectory: File, regexString: String, combinedMap: MutableMap<String, ArrayList<CommitSummary>>, depth: Int = 0, maxDepth: Int = 6) {

    if (depth > maxDepth || fileOrDirectory.isFile) {
        return
    }

    if (Paths.get(fileOrDirectory.path, ".git").toFile().isDirectory) {
        // found a repo -> summarize it
        val log = "git log --all --branches=* --remotes=*".run(fileOrDirectory)
        val commits = log.parseGitLog()
        val summarizedByAuthor = commits.summarizeGitMessages(regexString)
        summarizedByAuthor.forEach {
            val list: ArrayList<CommitSummary> = combinedMap.getOrPut(it.key) { arrayListOf() }
            list.add(it.value)
        }
    } else {
        // go deeper!
        fileOrDirectory.list().forEach {
            summarizeReposRecursive(Paths.get(fileOrDirectory.absolutePath, it).toFile(), regexString, combinedMap, depth.plus(1))
        }
    }
}

fun File.parseGitLog(): ArrayList<Commit> {
    return this.readLines().fold(arrayListOf()) { accumulated, current ->
        val splitBySpace = current.split("\\s".toRegex(), 2)

        when (splitBySpace[0]) {
            "commit" -> accumulated.add(Commit(splitBySpace[1]))
            "Author:" -> accumulated.last().author = current.substring(current.indexOf("<") + 1, current.indexOf(">"))
            "Date:" -> accumulated.last().date = current.split("Date:")[1].trim().parseGitDateString()
            else -> accumulated.last().message += current.trim()
        }

        accumulated
    }
}

fun ArrayList<Commit>.summarizeGitMessages(regexString: String): MutableMap<String, CommitSummary> {
    val pattern = Pattern.compile(regexString, Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

    return this.fold(mutableMapOf()) { accumulated, commit ->
        val summary: CommitSummary = accumulated.getOrPut(commit.author) { CommitSummary(commit.author, regexString, 0) }
        val match = pattern.matcher(commit.message)
        while (match.find()) {
            summary.count++
        }
        accumulated
    }
}

// run a string as a command
fun String.run(workingDir: File, targetFile: File = createTempFile()): File {

    if (targetFile.exists()) {
        targetFile.delete()
    }

    ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(targetFile))
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)

    return targetFile
}

fun String.parseGitDateString(format: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z")): OffsetDateTime {
    return OffsetDateTime.parse(this, format)
}
