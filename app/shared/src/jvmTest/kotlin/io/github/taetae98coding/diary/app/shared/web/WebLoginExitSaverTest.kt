package io.github.taetae98coding.diary.app.shared.web

import io.github.taetae98coding.diary.domain.web.usecase.SaveWebLoginUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class WebLoginExitSaverTest :
    FunSpec({
        test("TC-37352d53-356e-4e1e-9dc8-fdba8a414ecd 데스크톱 앱이 종료되면 지금의 세션 쿠키를 보관한다") {
            val saveUseCase = mockk<SaveWebLoginUseCase>()
            coEvery { saveUseCase(Unit) } returns Result.success(Unit)

            WebLoginExitSaver(saveWebLoginUseCase = saveUseCase).save()

            coVerify(exactly = 1) { saveUseCase(Unit) }
        }

        test("데스크톱 앱이 종료되면 보관이 끝날 때까지 종료를 미룬다") {
            runTest {
                val saveEnd = CompletableDeferred<Unit>()
                val saveUseCase = mockk<SaveWebLoginUseCase>()
                coEvery { saveUseCase(Unit) } coAnswers {
                    saveEnd.await()
                    Result.success(Unit)
                }
                var isSaved = false

                launch {
                    WebLoginExitSaver(saveWebLoginUseCase = saveUseCase).save()
                    isSaved = true
                }
                runCurrent()
                isSaved shouldBe false

                saveEnd.complete(Unit)
                runCurrent()

                isSaved shouldBe true
                coVerify(exactly = 1) { saveUseCase(Unit) }
            }
        }
    })
