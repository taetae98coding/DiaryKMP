package io.github.taetae98coding.diary.app.shared.web

import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.web.usecase.SaveWebLoginUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds

private val EXIT_TIMEOUT = 10.seconds

class WebLoginExitTest :
    FunSpec({
        test("TC-37352d53-356e-4e1e-9dc8-fdba8a414ecd 데스크톱 앱이 종료되면 지금의 세션 쿠키를 보관한다") {
            val saveUseCase = mockk<SaveWebLoginUseCase>()
            coEvery { saveUseCase() } returns Result.success(Unit)

            saveWebLoginOnExit(saveWebLoginUseCase = saveUseCase)

            coVerify(exactly = 1) { saveUseCase() }
        }

        test("데스크톱 앱이 종료되면 보관이 끝날 때까지 종료를 미룬다") {
            val saveStart = CompletableDeferred<Unit>()
            val saveEnd = CompletableDeferred<Unit>()
            val saveUseCase = mockk<SaveWebLoginUseCase>()
            coEvery { saveUseCase() } coAnswers {
                saveStart.complete(Unit)
                saveEnd.await()
                Result.success(Unit)
            }

            val exit = thread { saveWebLoginOnExit(saveWebLoginUseCase = saveUseCase) }
            runBlocking { saveStart.await() }
            exit.isAlive shouldBe true

            saveEnd.complete(Unit)
            exit.join(EXIT_TIMEOUT.inWholeMilliseconds)

            exit.isAlive shouldBe false
            coVerify(exactly = 1) { saveUseCase() }
        }
    })
