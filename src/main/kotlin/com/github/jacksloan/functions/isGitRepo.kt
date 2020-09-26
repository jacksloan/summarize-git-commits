package com.github.jacksloan.functions

import java.io.File
import java.nio.file.Paths

fun isGitRepo(dir: File): Boolean {
    return dir.isDirectory && Paths.get(dir.path, ".git").toFile().isDirectory
}
