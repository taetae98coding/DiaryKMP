package io.github.taetae98coding.diary.domain.contact.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ContactFavoriteUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 연락처가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val isFavoriteSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery {
                accountContactRepository.updateFavorite(
                    account = account,
                    contactId = contactId,
                    isFavorite = capture(isFavoriteSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = instant()
            val requestSyncUseCase = requestSyncUseCase()

            When("연락처를 즐겨찾기에 추가한다") {
                val useCase =
                    FavoriteContactUseCase(
                        getAccountUseCase = getAccountUseCase(account = account),
                        requestSyncUseCase = requestSyncUseCase,
                        accountContactRepository = accountContactRepository,
                        clock = clock(now = now),
                    )

                Then("TC-CONTACT-DETAIL-DOMAIN-012 즐겨찾기 여부를 즐겨찾기로 바꾸고 수정 시각을 변경 시점으로 기록한다") {
                    useCase(parameter = contactId).shouldBeSuccess(1)

                    isFavoriteSlot.captured shouldBe true
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-CONTACT-DETAIL-DATA-010 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = contactId).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("연락처의 즐겨찾기를 해제한다") {
                val useCase =
                    UnfavoriteContactUseCase(
                        getAccountUseCase = getAccountUseCase(account = account),
                        requestSyncUseCase = requestSyncUseCase,
                        accountContactRepository = accountContactRepository,
                        clock = clock(now = now),
                    )

                Then("TC-CONTACT-DETAIL-DOMAIN-012 즐겨찾기 여부를 즐겨찾기가 아닌 값으로 바꾸고 수정 시각을 변경 시점으로 기록한다") {
                    useCase(parameter = contactId).shouldBeSuccess(1)

                    isFavoriteSlot.captured shouldBe false
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-CONTACT-DETAIL-DATA-010 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = contactId).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("대상 연락처가 없어 아무것도 바뀌지 않도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery {
                accountContactRepository.updateFavorite(account = account, contactId = any(), isFavorite = any(), updatedAt = any())
            } returns 0

            When("연락처의 즐겨찾기를 바꾼다") {
                val useCase =
                    FavoriteContactUseCase(
                        getAccountUseCase = getAccountUseCase(account = account),
                        requestSyncUseCase = requestSyncUseCase(),
                        accountContactRepository = accountContactRepository,
                        clock = Clock.System,
                    )

                Then("TC-CONTACT-DETAIL-DATA-013 아무것도 바꾸지 않은 결과를 그대로 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>()).shouldBeSuccess(0)
                }
            }
        }

        Given("즐겨찾기 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery {
                accountContactRepository.updateFavorite(account = account, contactId = any(), isFavorite = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()

            When("연락처의 즐겨찾기를 바꾼다") {
                val useCase =
                    FavoriteContactUseCase(
                        getAccountUseCase = getAccountUseCase(account = account),
                        requestSyncUseCase = requestSyncUseCase,
                        accountContactRepository = accountContactRepository,
                        clock = Clock.System,
                    )

                Then("TC-CONTACT-DETAIL-DATA-014 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)

            When("연락처의 즐겨찾기를 바꾼다") {
                val useCase =
                    FavoriteContactUseCase(
                        getAccountUseCase = getAccountUseCase(account = Account.Guest),
                        requestSyncUseCase = requestSyncUseCase(),
                        accountContactRepository = accountContactRepository,
                        clock = Clock.System,
                    )

                Then("TC-CONTACT-DETAIL-DATA-011 게스트 계정 기준으로 기기에만 즐겨찾기를 반영한다") {
                    useCase(parameter = contactId).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        accountContactRepository.updateFavorite(
                            account = Account.Guest,
                            contactId = contactId,
                            isFavorite = true,
                            updatedAt = any(),
                        )
                    }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun getAccountUseCase(account: Account): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(Result.success(account))

            return useCase
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }

        private fun clock(now: Instant): Clock = mockk<Clock>().also { clock -> every { clock.now() } returns now }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
