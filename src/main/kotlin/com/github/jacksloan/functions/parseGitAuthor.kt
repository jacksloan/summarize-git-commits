package com.github.jacksloan.functions

fun parseGitAuthor(line: String): String {
    return line.substring(line.indexOf("<") + 1, line.indexOf(">"))
}
