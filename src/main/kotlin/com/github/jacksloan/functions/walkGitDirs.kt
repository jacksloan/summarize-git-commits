package com.github.jacksloan.functions

import java.io.File
import java.nio.file.Paths

/**
 * @param parentDir the starting directory
 * @param action the action to perform on every git directory
 */
fun walkGitDirs(
        parentDir: File,
        action: (file: File) -> Unit
) {
    if (isGitRepo(parentDir))
        action(parentDir)
    else
        parentDir.list().forEach {
            walkGitDirs(
                    Paths.get(parentDir.absolutePath, it).toFile(),
                    action
            )
        }
}
