package io.github.taetae98coding.diary.domain.qr.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant

class UpdateQrUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 연결된 QR이 저장되어 있고 현재 시각이 고정되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = fixtureMonkey.qr(isDeleted = false)
            val now = fixtureMonkey.giveMeOne<Instant>()
            val accountQrRepository = accountQrRepository(account = account, stored = stored)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    account = account,
                    accountQrRepository = accountQrRepository,
                    requestSyncUseCase = requestSyncUseCase,
                    now = now,
                )

            When("테스트 데이터의 입력으로 수정한다") {
                Then("TC-QR-DETAIL-DOMAIN-003 입력한 내용으로 수정하고 수정 시각을 기록한다") {
                    val description = fixtureMonkey.giveMeOne<String>()
                    val value = fixtureMonkey.nonBlankString()
                    val title = " " + fixtureMonkey.nonBlankString() + " "
                    val caseList =
                        listOf(
                            QrDetail(title = title, description = "", value = value) to QrDetail(title = title, description = "", value = value),
                            QrDetail(title = "", description = description, value = stored.detail.value) to
                                QrDetail(title = stored.detail.title, description = description, value = stored.detail.value),
                            QrDetail(title = "  ", description = stored.detail.description, value = "  ") to
                                QrDetail(title = stored.detail.title, description = stored.detail.description, value = "  "),
                        )

                    caseList.forEach { (input, expected) ->
                        useCase(parameter = UpdateQrUseCase.Parameter(id = stored.id, detail = input)).shouldBeSuccess(1)

                        coVerify(exactly = 1) {
                            accountQrRepository.updateDetail(account = account, qrId = stored.id, detail = expected, updatedAt = now)
                        }
                    }
                }
            }
        }

        Given("동기화를 요청할 수 있는 현재 계정과 QR이 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = fixtureMonkey.qr(isDeleted = false)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase = useCase(account = account, accountQrRepository = accountQrRepository(account = account, stored = stored), requestSyncUseCase = requestSyncUseCase)

            When("QR을 수정한다") {
                Then("TC-QR-DETAIL-DATA-003 기기에 저장한 뒤 동기화를 한 번 요청한다") {
                    useCase(parameter = UpdateQrUseCase.Parameter(id = stored.id, detail = fixtureMonkey.qrDetail())).shouldBeSuccess(1)

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("수정할 QR이 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = fixtureMonkey.qr(isDeleted = false)
            val accountQrRepository = accountQrRepository(account = account, stored = stored)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase = useCase(account = account, accountQrRepository = accountQrRepository, requestSyncUseCase = requestSyncUseCase)

            When("빈 QR 값으로 수정한다") {
                Then("TC-QR-DETAIL-DOMAIN-002 QR 내용 미입력으로 판정하고 어떤 내용도 수정하지 않는다") {
                    listOf(fixtureMonkey.qrDetail(value = ""), fixtureMonkey.qrDetail(title = "", value = "")).forEach { detail ->
                        useCase(parameter = UpdateQrUseCase.Parameter(id = stored.id, detail = detail))
                            .shouldBeFailure()
                            .shouldBeInstanceOf<QrValueEmptyException>()
                    }

                    coVerify(exactly = 0) { accountQrRepository.updateDetail(account = any(), qrId = any(), detail = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("게스트 계정과 연결된 QR이 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.Guest>()
            val stored = fixtureMonkey.qr(isDeleted = false)
            val accountQrRepository = accountQrRepository(account = account, stored = stored)
            val useCase = useCase(account = account, accountQrRepository = accountQrRepository)

            When("QR을 수정한다") {
                Then("TC-QR-DETAIL-DATA-004 게스트 계정 기준으로 기기에 반영한다") {
                    val detail = fixtureMonkey.qrDetail()

                    useCase(parameter = UpdateQrUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                    coVerify(exactly = 1) { accountQrRepository.updateDetail(account = account, qrId = stored.id, detail = detail, updatedAt = any()) }
                }
            }
        }

        Given("수정 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery { accountQrRepository.updateDetail(account = any(), qrId = any(), detail = any(), updatedAt = any()) } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase = useCase(account = account, accountQrRepository = accountQrRepository, requestSyncUseCase = requestSyncUseCase)

            When("QR을 수정한다") {
                Then("TC-QR-DETAIL-DATA-002 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = UpdateQrUseCase.Parameter(id = fixtureMonkey.qr(isDeleted = false).id, detail = fixtureMonkey.qrDetail()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("현재 계정과 연결된 QR이 없도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery { accountQrRepository.updateDetail(account = account, qrId = any(), detail = any(), updatedAt = any()) } returns 0
            val useCase = useCase(account = account, accountQrRepository = accountQrRepository)

            When("QR을 수정한다") {
                Then("TC-QR-DETAIL-DATA-001 바뀐 QR이 없음을 전달한다") {
                    useCase(parameter = UpdateQrUseCase.Parameter(id = fixtureMonkey.qr(isDeleted = false).id, detail = fixtureMonkey.qrDetail())).shouldBeSuccess(0)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun accountQrRepository(
            account: Account,
            stored: Qr,
        ): AccountQrRepository {
            val repository = mockk<AccountQrRepository>()
            every { repository.find(account = account, qrId = stored.id) } returns flowOf(stored)
            coEvery { repository.updateDetail(account = account, qrId = stored.id, detail = any(), updatedAt = any()) } returns 1

            return repository
        }

        private fun useCase(
            account: Account,
            accountQrRepository: AccountQrRepository,
            requestSyncUseCase: RequestSyncUseCase = requestSyncUseCase(),
            now: Instant = fixtureMonkey.giveMeOne<Instant>(),
        ): UpdateQrUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            return UpdateQrUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                findQrUseCase = FindQrUseCase(getAccountUseCase = getAccountUseCase, accountQrRepository = accountQrRepository),
                accountQrRepository = accountQrRepository,
                clock = clock,
            )
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }
    }
}
