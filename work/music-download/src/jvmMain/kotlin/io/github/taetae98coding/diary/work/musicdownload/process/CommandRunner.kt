package io.github.taetae98coding.diary.work.musicdownload.process

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File
import kotlin.coroutines.coroutineContext

/**
 * GUI로 실행한 앱은 사용자의 셸 PATH를 물려받지 않으므로 Homebrew가 설치하는 자리를 직접 확인한다.
 */
private val LOOKUP_DIRECTORY_LIST =
    listOf(
        "/opt/homebrew/bin",
        "/usr/local/bin",
        "/usr/bin",
        "/bin",
    )

@Factory
internal class CommandRunner {
    fun find(command: String): String? =
        LOOKUP_DIRECTORY_LIST
            .asSequence()
            .map { directory -> File(directory, command) }
            .firstOrNull { file -> file.canExecute() }
            ?.absolutePath

    suspend fun run(
        commandList: List<String>,
        onLine: suspend (String) -> Unit = {},
    ): Int =
        withContext(Dispatchers.IO) {
            val process =
                ProcessBuilder(commandList)
                    .redirectErrorStream(true)
                    .start()

            try {
                process.inputStream.bufferedReader().use { reader ->
                    while (true) {
                        coroutineContext.ensureActive()

                        val line = reader.readLine() ?: break
                        onLine(line)
                    }
                }

                process.waitFor()
            } finally {
                // 코루틴이 취소되면 프로세스가 남지 않도록 정리한다.
                if (process.isAlive) process.destroyForcibly()
            }
        }
}
