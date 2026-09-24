package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

private const val YT_DLP_PATH = "/opt/homebrew/bin/yt-dlp"
private const val FFMPEG_PATH = "/opt/homebrew/bin/ffmpeg"
private const val BREW_PATH = "/opt/homebrew/bin/brew"
private const val SUCCESS_EXIT_CODE = 0
private const val FAILURE_EXIT_CODE = 1

class HomebrewDownloadToolPreparerTest :
    BehaviorSpec({
        Given("두 도구가 모두 있다") {
            When("준비한다") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-011 설치하지 않고 준비된 것으로 본다") {
                    val commandRunner = commandRunner(installedList = listOf(DownloadTool.YT_DLP, DownloadTool.FFMPEG))
                    val preparer = HomebrewDownloadToolPreparer(commandRunner = commandRunner)

                    preparer.prepare() shouldBe DownloadToolPrepareResult.Prepared(ytDlpPath = YT_DLP_PATH)

                    coVerify(exactly = 0) { commandRunner.run(commandList = any(), onLine = any()) }
                }
            }
        }

        Given("도구가 없고 Homebrew는 있으며 설치가 성공한다") {
            When("준비한다") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-012 없는 도구만 설치하고 준비된 것으로 본다") {
                    val caseList =
                        listOf(
                            listOf(DownloadTool.YT_DLP) to listOf(DownloadTool.FFMPEG),
                            listOf(DownloadTool.FFMPEG) to listOf(DownloadTool.YT_DLP),
                            emptyList<DownloadTool>() to listOf(DownloadTool.YT_DLP, DownloadTool.FFMPEG),
                        )

                    caseList.forEach { (installedList, missingList) ->
                        val commandRunner = commandRunner(installedList = installedList, hasBrew = true, installedAfterRun = true)
                        val preparer = HomebrewDownloadToolPreparer(commandRunner = commandRunner)

                        preparer.prepare().shouldBeInstanceOf<DownloadToolPrepareResult.Prepared>()

                        coVerify(exactly = 1) {
                            commandRunner.run(
                                commandList = listOf(BREW_PATH, "install") + missingList.map { tool -> tool.formula },
                                onLine = any(),
                            )
                        }
                    }
                }
            }
        }

        Given("도구가 없고 Homebrew도 없다") {
            When("준비한다") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-013 직접 설치해야 함을 알린다") {
                    val commandRunner = commandRunner(installedList = emptyList(), hasBrew = false)
                    val preparer = HomebrewDownloadToolPreparer(commandRunner = commandRunner)

                    preparer.prepare() shouldBe DownloadToolPrepareResult.NotInstalled

                    coVerify(exactly = 0) { commandRunner.run(commandList = any(), onLine = any()) }
                }
            }
        }

        Given("도구가 없고 Homebrew는 있지만 설치가 실패한다") {
            When("준비한다") {
                Then("TC-MUSIC-DOWNLOAD-FEATURE-014 준비 실패로 본다") {
                    val commandRunner = commandRunner(installedList = emptyList(), hasBrew = true, exitCode = FAILURE_EXIT_CODE)
                    val preparer = HomebrewDownloadToolPreparer(commandRunner = commandRunner)

                    preparer.prepare() shouldBe DownloadToolPrepareResult.Failed
                }
            }
        }

        Given("설치가 성공했다고 보고하지만 도구가 자리에 없다") {
            When("준비한다") {
                Then("준비 실패로 본다") {
                    val commandRunner = commandRunner(installedList = emptyList(), hasBrew = true, installedAfterRun = false)
                    val preparer = HomebrewDownloadToolPreparer(commandRunner = commandRunner)

                    preparer.prepare() shouldBe DownloadToolPrepareResult.Failed
                }
            }
        }
    }) {
    public companion object {
        private fun commandRunner(
            installedList: List<DownloadTool>,
            hasBrew: Boolean = false,
            exitCode: Int = SUCCESS_EXIT_CODE,
            installedAfterRun: Boolean = false,
        ): CommandRunner {
            val commandRunner = mockk<CommandRunner>()
            var isRun = false

            every { commandRunner.find(command = any()) } answers
                {
                    when (val command = firstArg<String>()) {
                        HOMEBREW_COMMAND -> BREW_PATH.takeIf { hasBrew }

                        DownloadTool.YT_DLP.command ->
                            YT_DLP_PATH.takeIf { DownloadTool.YT_DLP in installedList || (isRun && installedAfterRun) }

                        DownloadTool.FFMPEG.command ->
                            FFMPEG_PATH.takeIf { DownloadTool.FFMPEG in installedList || (isRun && installedAfterRun) }

                        else -> error("unexpected command $command")
                    }
                }

            coEvery { commandRunner.run(commandList = any(), onLine = any()) } answers
                {
                    isRun = true
                    exitCode
                }

            return commandRunner
        }
    }
}
