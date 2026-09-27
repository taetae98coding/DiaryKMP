@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class QrHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-QR-HOME-FEATURE-015 조회한 QR 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val qrList = listOf(fixtureMonkey.qr(isDeleted = false), fixtureMonkey.qr(isDeleted = false))
                val viewModel = viewModel(pageQrUseCase = pageQrUseCase(qrListFlow = flowOf(Result.success(qrList))))

                flowOf(viewModel.qrPagingData.first()).asSnapshot() shouldBe qrList
            }
        }

        test("TC-QR-HOME-DATA-002 QR 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageQrUseCase = pageQrUseCase(qrListFlow = flowOf(Result.failure(IllegalStateException()))))

                flowOf(viewModel.qrPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-QR-HOME-FEATURE-017 QR을 추가하면 별도 조작 없이 그 QR이 든 목록을 노출한다") {
            runTest(mainDispatcher) {
                val existing = fixtureMonkey.qr(isDeleted = false)
                val added = fixtureMonkey.qr(isDeleted = false)
                val qrListFlow = MutableStateFlow(Result.success(listOf(existing)))
                val viewModel = viewModel(pageQrUseCase = pageQrUseCase(qrListFlow = qrListFlow))

                viewModel.qrPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(existing)

                    qrListFlow.value = Result.success(listOf(added, existing))

                    flowOf(awaitItem()).asSnapshot() shouldBe listOf(added, existing)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-QR-HOME-FEATURE-025 삭제에 성공하면 그 QR의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                coEvery { deleteQrUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(deleteQrUseCase = deleteQrUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe QrHomeEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteQrUseCase(parameter = id) }
            }
        }

        test("TC-QR-HOME-FEATURE-030 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                coEvery { deleteQrUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(deleteQrUseCase = deleteQrUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-QR-HOME-FEATURE-026 실행 취소하면 그 QR의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreQrUseCase = mockk<RestoreQrUseCase>()
                coEvery { restoreQrUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(restoreQrUseCase = restoreQrUseCase)

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restoreQrUseCase(parameter = id) }
            }
        }

        test("TC-QR-HOME-DOMAIN-011 실행 취소를 저장하지 못하면 별도 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreQrUseCase = mockk<RestoreQrUseCase>()
                coEvery { restoreQrUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(restoreQrUseCase = restoreQrUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restoreQrUseCase(parameter = id) }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            pageQrUseCase: PageQrUseCase = pageQrUseCase(qrListFlow = flowOf(Result.success(emptyList()))),
            deleteQrUseCase: DeleteQrUseCase = mockk(),
            restoreQrUseCase: RestoreQrUseCase = mockk(),
        ): QrHomeViewModel =
            QrHomeViewModel(
                pageQrUseCase = pageQrUseCase,
                deleteQrUseCase = deleteQrUseCase,
                restoreQrUseCase = restoreQrUseCase,
            )

        private fun pageQrUseCase(qrListFlow: Flow<Result<List<Qr>>>): PageQrUseCase {
            val pageQrUseCase = mockk<PageQrUseCase>()
            every { pageQrUseCase(parameter = Unit) } returns qrListFlow.map { result -> result.map { qrList -> PagingData.from(qrList) } }

            return pageQrUseCase
        }
    }
}
