package io.github.taetae98coding.diary.work.mcp.launcher

import io.github.taetae98coding.diary.domain.mcp.usecase.RunMcpServerUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class McpServerLauncherTest :
    FunSpec({
        test("여러 번 시작을 요청해도 연결 관리는 한 번만 시작한다") {
            runTest {
                val useCase = mockk<RunMcpServerUseCase>()
                coEvery { useCase(parameter = Unit) } coAnswers { awaitCancellation() }
                val launcher = McpServerLauncher(scope = backgroundScope, runMcpServerUseCase = useCase)

                launcher.launch()
                launcher.launch()
                runCurrent()

                coVerify(exactly = 1) { useCase(parameter = Unit) }
            }
        }
    })
