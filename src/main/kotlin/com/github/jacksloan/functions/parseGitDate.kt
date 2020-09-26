package com.github.jacksloan.functions

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun parseGitDate(line: String): OffsetDateTime? {
    return try {
        OffsetDateTime.parse(
                line,
                DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z"))
    } catch (e: Exception) {
        null
    }
}
