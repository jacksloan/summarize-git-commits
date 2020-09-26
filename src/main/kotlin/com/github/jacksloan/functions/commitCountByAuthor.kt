package com.github.jacksloan.functions

import com.github.jacksloan.model.Commit
import com.github.jacksloan.model.CommitSummary

fun commitCountByAuthor(commits: List<Commit>): List<CommitSummary> {
    return commits
            .groupBy { it.author }
            .map { CommitSummary(author = it.key, count = it.value.size) }
}
