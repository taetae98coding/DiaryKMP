@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.chrome.session

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import io.github.taetae98coding.diary.domain.browser.repository.InAppBrowserCookieRepository
import io.github.taetae98coding.diary.domain.browser.usecase.FindChromeSessionImportProfileUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.ImportChromeSessionUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

private val profileA = ChromeProfile(directory = "Default", name = "TaeJong")
private val profileB = ChromeProfile(directory = "Profile 1", name = "Work")

class ChromeSessionImportManagerImplTest :
    FunSpec({
        test("TC-CHROME-SESSION-IMPORT-DOMAIN-001 TC-CHROME-SESSION-IMPORT-DOMAIN-011 가져올 프로필이 없으면 상태를 바꾸지 않고 아무것도 넘기거나 지우지 않는다") {
            runTest {
                val importUseCase = importUseCase()
                val repository = repository()
                val manager = manager(scope = backgroundScope, profile = null, importUseCase = importUseCase, repository = repository)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 0) { importUseCase(any()) }
                coVerify(exactly = 0) { repository.deleteAll() }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-014 프로필 목록을 읽지 못하면 가져오지 않고 실패가 된다") {
            runTest {
                val findUseCase = mockk<FindChromeSessionImportProfileUseCase>()
                coEvery { findUseCase(Unit) } returns Result.failure(IllegalStateException("local state unreadable"))
                val importUseCase = importUseCase()
                val manager = manager(scope = backgroundScope, findUseCase = findUseCase, importUseCase = importUseCase)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)

                    awaitItem() shouldBe ChromeSessionImportState.FAILED
                }

                coVerify(exactly = 0) { importUseCase(any()) }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-018 앱이 보이게 되면 지우지 않고 가져오며 가져오는 중을 거쳐 성공이 된다") {
            runTest {
                val importUseCase = importUseCase()
                val repository = repository()
                val manager = manager(scope = backgroundScope, profile = profileA, importUseCase = importUseCase, repository = repository)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    listOf(ChromeSessionImportState.IDLE, ChromeSessionImportState.IMPORTED).forEach { _ ->
                        manager.requestImport(clearsBefore = false)

                        awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                        awaitItem() shouldBe ChromeSessionImportState.IMPORTED
                    }
                }

                coVerify(exactly = 2) { importUseCase(profileA) }
                coVerify(exactly = 0) { repository.deleteAll() }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-018 실패 뒤에 앱이 보이게 되면 다시 가져온다") {
            runTest {
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                coEvery { importUseCase(profileA) } returnsMany listOf(Result.failure(IllegalStateException("first")), Result.success(Unit))
                val manager = manager(scope = backgroundScope, profile = profileA, importUseCase = importUseCase)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe ChromeSessionImportState.FAILED

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTED
                }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-016 다른 프로필을 고르면 지운 뒤 새 프로필을 가져온다") {
            runTest {
                val importUseCase = importUseCase()
                val repository = repository()
                val manager = manager(scope = backgroundScope, profile = profileA, importUseCase = importUseCase, repository = repository)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTED

                    manager.requestImport(clearsBefore = true)
                    awaitItem() shouldBe ChromeSessionImportState.IDLE
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTED
                }

                coVerifyOrder {
                    importUseCase(profileA)
                    repository.deleteAll()
                    importUseCase(profileA)
                }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-017 선택 안 함으로 되돌리면 지우기만 하고 가져온 적 없음이 된다") {
            runTest {
                listOf(Result.success(Unit), Result.failure(IllegalStateException("import failed"))).forEach { firstResult ->
                    val findUseCase = mockk<FindChromeSessionImportProfileUseCase>()
                    coEvery { findUseCase(Unit) } returnsMany listOf(Result.success(profileA), Result.success(null))
                    val importUseCase = mockk<ImportChromeSessionUseCase>()
                    coEvery { importUseCase(profileA) } returns firstResult
                    val repository = repository()
                    val manager = manager(scope = backgroundScope, findUseCase = findUseCase, importUseCase = importUseCase, repository = repository)

                    manager.state.test {
                        awaitItem() shouldBe ChromeSessionImportState.IDLE

                        manager.requestImport(clearsBefore = false)
                        awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                        awaitItem()

                        manager.requestImport(clearsBefore = true)
                        awaitItem() shouldBe ChromeSessionImportState.IDLE
                        advanceUntilIdle()
                        expectNoEvents()
                    }

                    coVerify(exactly = 1) { repository.deleteAll() }
                    coVerify(exactly = 1) { importUseCase(any()) }
                }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-019 가져오는 중에 새 계기가 오면 취소하고 새로 시작하며 취소된 결과는 반영하지 않는다") {
            runTest {
                val pending = CompletableDeferred<Result<Unit>>()
                var callCount = 0
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                coEvery { importUseCase(profileA) } coAnswers { if (callCount++ == 0) pending.await() else Result.success(Unit) }
                val manager = manager(scope = backgroundScope, profile = profileA, importUseCase = importUseCase)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTED
                    advanceUntilIdle()
                    expectNoEvents()
                }

                pending.isCompleted shouldBe false
                coVerify(exactly = 2) { importUseCase(profileA) }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-023 가져오는 중에 선택 안 함으로 되돌리면 진행 중인 가져오기를 취소하고 지운 뒤 가져온 적 없음이 된다") {
            runTest {
                val pending = CompletableDeferred<Result<Unit>>()
                val findUseCase = mockk<FindChromeSessionImportProfileUseCase>()
                coEvery { findUseCase(Unit) } returnsMany listOf(Result.success(profileA), Result.success(null))
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                coEvery { importUseCase(profileA) } coAnswers { pending.await() }
                val repository = repository()
                val manager = manager(scope = backgroundScope, findUseCase = findUseCase, importUseCase = importUseCase, repository = repository)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING

                    manager.requestImport(clearsBefore = true)
                    awaitItem() shouldBe ChromeSessionImportState.IDLE
                    advanceUntilIdle()
                    expectNoEvents()
                }

                pending.isCompleted shouldBe false
                coVerify(exactly = 1) { repository.deleteAll() }
                coVerify(exactly = 1) { importUseCase(any()) }
            }
        }

        test("가져오는 중에 취소된 뒤 가져올 프로필이 없으면 가져오기 전 상태로 돌아간다") {
            runTest {
                val pending = CompletableDeferred<Result<Unit>>()
                val findUseCase = mockk<FindChromeSessionImportProfileUseCase>()
                coEvery { findUseCase(Unit) } returnsMany listOf(Result.success(profileA), Result.success(profileA), Result.success(null))
                var callCount = 0
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                coEvery { importUseCase(profileA) } coAnswers { if (callCount++ == 0) Result.success(Unit) else pending.await() }
                val manager = manager(scope = backgroundScope, findUseCase = findUseCase, importUseCase = importUseCase)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTED

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING

                    manager.requestImport(clearsBefore = false)
                    awaitItem() shouldBe ChromeSessionImportState.IMPORTED
                }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-020 지우기에 실패하면 가져오지 않고 실패가 된다") {
            runTest {
                val findUseCase = mockk<FindChromeSessionImportProfileUseCase>()
                coEvery { findUseCase(Unit) } returns Result.success(profileB)
                val importUseCase = importUseCase()
                val repository = mockk<InAppBrowserCookieRepository>()
                coEvery { repository.deleteAll() } throws IllegalStateException("clear failed")
                val manager = manager(scope = backgroundScope, findUseCase = findUseCase, importUseCase = importUseCase, repository = repository)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = true)

                    awaitItem() shouldBe ChromeSessionImportState.FAILED
                }

                coVerify(exactly = 0) { findUseCase(Unit) }
                coVerify(exactly = 0) { importUseCase(any()) }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-022 선택 안 함으로 되돌릴 때 지우기에 실패하면 실패가 된다") {
            runTest {
                val findUseCase = mockk<FindChromeSessionImportProfileUseCase>()
                coEvery { findUseCase(Unit) } returns Result.success(null)
                val importUseCase = importUseCase()
                val repository = mockk<InAppBrowserCookieRepository>()
                coEvery { repository.deleteAll() } throws IllegalStateException("clear failed")
                val manager = manager(scope = backgroundScope, findUseCase = findUseCase, importUseCase = importUseCase, repository = repository)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = true)

                    awaitItem() shouldBe ChromeSessionImportState.FAILED
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 0) { importUseCase(any()) }
            }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-021 가져오기에 실패해도 남아 있던 로그인 정보를 지우지 않는다") {
            runTest {
                val importUseCase = mockk<ImportChromeSessionUseCase>()
                coEvery { importUseCase(profileA) } returns Result.failure(IllegalStateException("cookie store unreadable"))
                val repository = repository()
                val manager = manager(scope = backgroundScope, profile = profileA, importUseCase = importUseCase, repository = repository)

                manager.state.test {
                    awaitItem() shouldBe ChromeSessionImportState.IDLE

                    manager.requestImport(clearsBefore = false)

                    awaitItem() shouldBe ChromeSessionImportState.IMPORTING
                    awaitItem() shouldBe ChromeSessionImportState.FAILED
                }

                coVerify(exactly = 0) { repository.deleteAll() }
            }
        }
    })

private fun importUseCase(): ImportChromeSessionUseCase = mockk<ImportChromeSessionUseCase>().also { useCase -> coEvery { useCase(any()) } returns Result.success(Unit) }

private fun repository(): InAppBrowserCookieRepository = mockk<InAppBrowserCookieRepository>().also { repository -> coEvery { repository.deleteAll() } just runs }

private fun manager(
    scope: CoroutineScope,
    profile: ChromeProfile?,
    importUseCase: ImportChromeSessionUseCase = importUseCase(),
    repository: InAppBrowserCookieRepository = repository(),
): ChromeSessionImportManagerImpl {
    val findUseCase = mockk<FindChromeSessionImportProfileUseCase>()
    coEvery { findUseCase(Unit) } returns Result.success(profile)

    return manager(scope = scope, findUseCase = findUseCase, importUseCase = importUseCase, repository = repository)
}

private fun manager(
    scope: CoroutineScope,
    findUseCase: FindChromeSessionImportProfileUseCase,
    importUseCase: ImportChromeSessionUseCase = importUseCase(),
    repository: InAppBrowserCookieRepository = repository(),
): ChromeSessionImportManagerImpl =
    ChromeSessionImportManagerImpl(
        findChromeSessionImportProfileUseCase = findUseCase,
        importChromeSessionUseCase = importUseCase,
        inAppBrowserCookieRepository = repository,
        scope = scope,
    )
