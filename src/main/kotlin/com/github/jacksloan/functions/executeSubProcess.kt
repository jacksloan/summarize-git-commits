package com.github.jacksloan.functions

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @param command the command to run
 * @param workingDir the directory to run the command in
 * @param commandOutputTarget the file that the command will pipe output to, defaults to a temporary file
 */
fun executeSubProcess(
        command: String,
        workingDir: File,
        commandOutputTarget: File = createTempFile()
): File {

    if (commandOutputTarget.exists()) {
        commandOutputTarget.delete()
    }

    ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(commandOutputTarget))
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(5, TimeUnit.MINUTES)

    return commandOutputTarget
}
