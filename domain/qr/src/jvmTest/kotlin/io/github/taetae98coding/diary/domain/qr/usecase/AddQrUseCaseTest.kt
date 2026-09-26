package io.github.taetae98coding.diary.domain.qr.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AddQrUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
            " \n\t " to "줄바꿈과 탭, 공백으로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountQrRepository = mockk<AccountQrRepository>(relaxed = true)
                val requestSyncUseCase = requestSyncUseCase()
                val useCase =
                    AddQrUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase,
                        accountQrRepository = accountQrRepository,
                        clock = Clock.System,
                    )

                When("QR을 추가한다") {
                    Then("TC-QR-ADD-DOMAIN-007 제목 공백 예외로 실패하고 QR을 저장하지 않으며 동기화를 요청하지 않는다") {
                        val result = useCase(parameter = fixtureMonkey.qrDetail(title = blankTitle))

                        result.shouldBeFailure().shouldBeInstanceOf<QrTitleBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountQrRepository.upsert(account = any(), qr = any()) }
                        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                    }

                    Then("TC-QR-ADD-DOMAIN-008 QR 값도 비어 있으면 제목 공백 예외 하나만 알린다") {
                        val result = useCase(parameter = fixtureMonkey.qrDetail(title = blankTitle, value = ""))

                        result.shouldBeFailure().shouldBeInstanceOf<QrTitleBlankException>()
                        coVerify(exactly = 0) { accountQrRepository.upsert(account = any(), qr = any()) }
                    }
                }
            }
        }

        Given("제목은 있지만 QR 값이 비어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountQrRepository = mockk<AccountQrRepository>(relaxed = true)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 추가한다") {
                Then("TC-QR-ADD-DOMAIN-007 QR 값 미입력 예외로 실패하고 QR을 저장하지 않으며 동기화를 요청하지 않는다") {
                    val result = useCase(parameter = fixtureMonkey.qrDetail(value = ""))

                    result.shouldBeFailure().shouldBeInstanceOf<QrValueEmptyException>()
                    verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                    coVerify(exactly = 0) { accountQrRepository.upsert(account = any(), qr = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val qrSlot = slot<Qr>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery { accountQrRepository.upsert(account = account, qr = capture(qrSlot)) } just Runs
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = clock,
                )

            When("제목과 QR 값이 있는 QR을 추가한다") {
                Then("TC-QR-ADD-DOMAIN-013 현재 계정과 연결된 고유한 QR로 저장하고 그 식별자를 돌려준다") {
                    val firstId = useCase(parameter = fixtureMonkey.qrDetail()).shouldBeSuccess()
                    qrSlot.captured.id shouldBe firstId
                    val secondId = useCase(parameter = fixtureMonkey.qrDetail()).shouldBeSuccess()

                    firstId shouldNotBe Uuid.NIL
                    firstId shouldNotBe secondId
                    coVerify(exactly = 2) { accountQrRepository.upsert(account = account, qr = any()) }
                }

                Then("TC-QR-ADD-DOMAIN-011 제목, 설명과 QR 값이 같아도 서로 다른 QR로 저장한다") {
                    val sameDetail = fixtureMonkey.qrDetail()

                    val firstId = useCase(parameter = sameDetail).shouldBeSuccess()
                    val secondId = useCase(parameter = sameDetail).shouldBeSuccess()

                    firstId shouldNotBe secondId
                }

                Then("TC-QR-ADD-DOMAIN-010 입력한 제목, 설명과 QR 값을 앞뒤 공백과 줄바꿈까지 그대로 저장한다") {
                    val expected =
                        fixtureMonkey.qrDetail(
                            title = "  title-${fixtureMonkey.giveMeOne<String>()}  ",
                            description = "  ${fixtureMonkey.giveMeOne<String>()}\n${fixtureMonkey.giveMeOne<String>()}  ",
                            value = "  ${fixtureMonkey.giveMeOne<String>()}\n${fixtureMonkey.giveMeOne<String>()}  ",
                        )

                    useCase(parameter = expected).shouldBeSuccess()

                    qrSlot.captured.detail shouldBe expected
                }

                listOf(
                    fixtureMonkey.qrDetail(description = "") to "빈 설명",
                    fixtureMonkey.qrDetail(value = " ") to "공백 한 칸뿐인 QR 값",
                ).forEach { (expected, label) ->
                    Then("TC-QR-ADD-DOMAIN-009 ${label}이어도 QR을 저장하고 입력한 설명과 QR 값을 그대로 기록한다") {
                        useCase(parameter = expected).shouldBeSuccess()

                        qrSlot.captured.detail shouldBe expected
                    }
                }

                Then("TC-QR-ADD-DOMAIN-012 미삭제 상태와 추가 시각을 저장한다") {
                    useCase(parameter = fixtureMonkey.qrDetail()).shouldBeSuccess()

                    qrSlot.captured.isDeleted shouldBe false
                    qrSlot.captured.createdAt shouldBe now
                    qrSlot.captured.updatedAt shouldBe now
                }
            }
        }

        Given("QR을 추가할 수 있는 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery { accountQrRepository.upsert(account = account, qr = any()) } just Runs
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR 추가에 성공한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 TC-QR-ADD-DATA-003 기기에 저장한 뒤 서버와 맞추기 위한 동기화를 한 번 요청한다") {
                    useCase(parameter = fixtureMonkey.qrDetail()).shouldBeSuccess()

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("QR 저장은 성공하지만 동기화 요청이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val qrSlot = slot<Qr>()
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery { accountQrRepository.upsert(account = account, qr = capture(qrSlot)) } just Runs
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
            val useCase =
                AddQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 추가한다") {
                Then("TC-QR-ADD-DATA-004 추가는 성공으로 전달되고 저장한 QR을 되돌리지 않는다") {
                    useCase(parameter = fixtureMonkey.qrDetail()).shouldBeSuccess()

                    coVerify(exactly = 1) { accountQrRepository.upsert(account = account, qr = any()) }
                    qrSlot.captured.isDeleted.shouldBeFalse()
                }
            }
        }

        Given("계정이 게스트 상태로 준비되어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val accountQrRepository = mockk<AccountQrRepository>(relaxed = true)
            val useCase =
                AddQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 추가한다") {
                Then("TC-QR-ADD-DATA-006 게스트 계정으로 기기에 저장한다") {
                    useCase(parameter = fixtureMonkey.qrDetail()).shouldBeSuccess()

                    coVerify(exactly = 1) { accountQrRepository.upsert(account = Account.Guest, qr = any()) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountQrRepository = mockk<AccountQrRepository>(relaxed = true)
            val useCase =
                AddQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 추가한다") {
                Then("TC-QR-ADD-DOMAIN-014 계정 조회 실패를 전달하고 QR을 저장하지 않는다") {
                    useCase(parameter = fixtureMonkey.qrDetail()).shouldBeFailure() shouldBeSameInstanceAs throwable

                    coVerify(exactly = 0) { accountQrRepository.upsert(account = any(), qr = any()) }
                }
            }
        }

        Given("QR 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            coEvery { accountQrRepository.upsert(account = account, qr = any()) } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountQrRepository = accountQrRepository,
                    clock = Clock.System,
                )

            When("QR을 추가한다") {
                Then("TC-QR-ADD-DATA-002 추가를 성공으로 다루지 않고 저장 실패를 전달하며 동기화를 요청하지 않는다") {
                    useCase(parameter = fixtureMonkey.qrDetail()).shouldBeFailure() shouldBeSameInstanceAs throwable

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }
    }
}
