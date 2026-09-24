package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.process.SUCCESS_EXIT_CODE
import org.koin.core.annotation.Factory

@Factory
internal class HomebrewDownloadToolPreparer(
    private val commandRunner: CommandRunner,
) : DownloadToolPreparer {
    override suspend fun prepare(): DownloadToolPrepareResult {
        val missingList = commandRunner.findMissingDownloadToolList()
        val brewPath = if (missingList.isEmpty()) null else commandRunner.find(command = HOMEBREW_COMMAND)

        return when {
            missingList.isEmpty() -> DownloadToolPrepareResult.Prepared
            brewPath == null -> DownloadToolPrepareResult.NotInstalled
            install(brewPath = brewPath, missingList = missingList) -> prepared()
            else -> DownloadToolPrepareResult.Failed
        }
    }

    private suspend fun install(
        brewPath: String,
        missingList: List<DownloadTool>,
    ): Boolean {
        val commandList = listOf(brewPath, "install") + missingList.map { tool -> tool.formula }

        return commandRunner.run(commandList = commandList) == SUCCESS_EXIT_CODE
    }

    // 설치가 성공했다고 보고해도 실제로 실행할 수 있는 자리에 없을 수 있으므로 다시 확인한다.
    private fun prepared(): DownloadToolPrepareResult =
        if (commandRunner.findMissingDownloadToolList().isEmpty()) {
            DownloadToolPrepareResult.Prepared
        } else {
            DownloadToolPrepareResult.Failed
        }
}
