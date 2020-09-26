package com.github.jacksloan.model

import java.time.OffsetDateTime

data class Commit(
        val commitId: String = "",
        val author: String = "",
        val date: OffsetDateTime? = null,
        val message: String = ""
) {

    fun addToCommitBody(partialCommitMessage: String): Commit {
        return this.copy(message = this.message + partialCommitMessage)
    }

}
