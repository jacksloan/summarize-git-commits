package com.github.jacksloan.functions

import com.github.jacksloan.model.Commit
import java.io.File

/**
 * Walks through the provided directory returning all found matches
 * @param parentDir the directory to start in containing git repositories
 * @param regexMatch the returned list will only contain messages matching this pattern
 */
fun findAllMatchingCommits(
        parentDir: File,
        regexMatch: String
): List<Commit> {

    val commits = arrayListOf<Commit>()

    walkGitDirs(parentDir) { gitDirectory ->
        val temp = executeSubProcess("git log --all --branches=* --remotes=*", gitDirectory)

        val commitsByCommitHash = hashMapOf<String, Commit>()

        temp.readLines().foldIndexed("") { index, lastCommitHash, currentLine ->
            val isNewCommitLine = currentLine.startsWith("commit")
            val isDoneWithLastCommitHash = isNewCommitLine && index > 0

            if (isDoneWithLastCommitHash) {
                // if the commit message doesn't match our regex
                // remove it from the map of commits
                commitsByCommitHash[lastCommitHash]
                        ?.message
                        ?.matches(Regex(regexMatch))
                        ?.also { isMatch ->
                            if (!isMatch) {
                                commitsByCommitHash.remove(lastCommitHash)
                            }
                        }
            }

            val lineParts = currentLine.split(regex = "\\s".toRegex(), limit = 2)
            val lineIdentifier = if (lineParts.isNotEmpty()) lineParts[0] else "" // "commit" | "author" | "date" | string
            val commitIdAuthorOrDate = if (lineParts.size >= 2) lineParts[1] else ""

            commitsByCommitHash[lineIdentifier] =
                    if (isNewCommitLine) Commit(commitId = commitIdAuthorOrDate)
                    else when (lineIdentifier) {
                        "Author:" -> commitsByCommitHash[lineIdentifier]!!.copy(author = parseGitAuthor(commitIdAuthorOrDate))
                        "Date:" -> commitsByCommitHash[lineIdentifier]!!.copy(date = parseGitDate(commitIdAuthorOrDate))
                        else -> commitsByCommitHash[lineIdentifier]!!.addToCommitBody(currentLine)
                    }

            if (isNewCommitLine) commitIdAuthorOrDate
            else lastCommitHash
        }

        commits.addAll(commitsByCommitHash.values.toTypedArray())
    }

    return commits
}
