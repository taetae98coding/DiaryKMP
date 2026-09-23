package io.github.taetae98coding.diary.domain.contact.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UpdateContactUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 연락처가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = contact()
            val detailSlot = slot<ContactDetail>()
            val updatedAtSlot = slot<Instant>()
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery {
                accountContactRepository.updateDetail(
                    account = account,
                    contactId = stored.id,
                    detail = capture(detailSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = instant()
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase,
                    findContactUseCase = findContactUseCase(contact = stored),
                    accountContactRepository = accountContactRepository,
                    now = now,
                )

            When("이름, 설명, 키, 신발 사이즈, 생일과 전화번호를 모두 바꿔 수정한다") {
                Then("TC-CONTACT-DETAIL-DOMAIN-006 입력한 내용과 수정 시점을 반영한다") {
                    val detail =
                        detail(
                            name = "김철수",
                            height = 180.5.centimeter,
                            footSize = 270.millimeter,
                            birthday =
                                ContactBirthday(
                                    date = LocalDate(1994, 3, 21),
                                    calendar = ContactBirthdayCalendar.LUNAR,
                                ),
                            phoneNumberList = listOf(ContactPhoneNumber(number = "010-1234-5678")),
                        )

                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                    detailSlot.captured shouldBe detail
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-CONTACT-DETAIL-DOMAIN-006 같은 번호도 합치지 않고 입력한 순서대로 반영한다") {
                    val first = ContactPhoneNumber(number = "010-1234-5678")
                    val second = ContactPhoneNumber(number = "010-1234-5678")
                    val third = ContactPhoneNumber(number = "02-123-4567")

                    useCase(
                        parameter =
                            UpdateContactUseCase.Parameter(
                                id = stored.id,
                                detail = detail(phoneNumberList = listOf(first, second, third)),
                            ),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.phoneNumberList shouldBe listOf(first, second, third)
                }

                Then("TC-CONTACT-DETAIL-DOMAIN-007 비운 값은 없는 값이나 비어 있는 값으로 반영한다") {
                    useCase(
                        parameter =
                            UpdateContactUseCase.Parameter(
                                id = stored.id,
                                detail =
                                    detail(
                                        description = "",
                                        height = null,
                                        footSize = null,
                                        birthday = null,
                                        hometown = "",
                                        phoneNumberList = emptyList(),
                                    ),
                            ),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.description shouldBe ""
                    detailSlot.captured.height shouldBe null
                    detailSlot.captured.footSize shouldBe null
                    detailSlot.captured.birthday shouldBe null
                    detailSlot.captured.hometown shouldBe ""
                    detailSlot.captured.phoneNumberList shouldBe emptyList()
                }

                Then("TC-CONTACT-DETAIL-DOMAIN-006 입력한 고향을 그대로 반영한다") {
                    val hometown = "hometown-${fixtureMonkey.giveMeOne<String>()}"

                    useCase(
                        parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail(hometown = hometown)),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.hometown shouldBe hometown
                }

                Then("TC-CONTACT-DETAIL-DOMAIN-012 달력 구분만 바꿔도 그 구분으로 반영한다") {
                    val date = LocalDate(1994, 3, 21)

                    useCase(
                        parameter =
                            UpdateContactUseCase.Parameter(
                                id = stored.id,
                                detail = detail(birthday = ContactBirthday(date = date, calendar = ContactBirthdayCalendar.LUNAR)),
                            ),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.birthday shouldBe ContactBirthday(date = date, calendar = ContactBirthdayCalendar.LUNAR)
                }

                Then("TC-CONTACT-DETAIL-DOMAIN-008 수정은 삭제 여부와 생성 시각과 계정 연결을 바꾸지 않는다") {
                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess(1)

                    // 저장소는 내용과 수정 시각만 받으므로 삭제 여부·생성 시각·계정 연결은 바뀔 자리가 없다.
                    coVerify(exactly = 0) {
                        accountContactRepository.updateDeleted(account = any(), contactId = any(), isDeleted = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { accountContactRepository.upsert(account = any(), contact = any()) }
                }

                Then("TC-CONTACT-DETAIL-DATA-010 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("이름을 비운 채 수정한다") {
                Then("TC-CONTACT-DETAIL-DOMAIN-005 저장된 기존 이름을 사용하고 나머지는 입력한 대로 반영한다") {
                    listOf("", "   ").forEach { name ->
                        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
                        val phoneNumber = ContactPhoneNumber(number = "010-0000-0000")

                        useCase(
                            parameter =
                                UpdateContactUseCase.Parameter(
                                    id = stored.id,
                                    detail = detail(name = name, description = description, phoneNumberList = listOf(phoneNumber)),
                                ),
                        ).shouldBeSuccess(1)

                        detailSlot.captured.name shouldBe stored.detail.name
                        detailSlot.captured.description shouldBe description
                        detailSlot.captured.phoneNumberList shouldBe listOf(phoneNumber)
                    }
                }
            }
        }

        listOf("", "   ").forEach { blankNumber ->
            Given("번호가 `$blankNumber` 인 전화번호 항목이 섞여 있다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val stored = contact()
                val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
                val requestSyncUseCase = requestSyncUseCase()
                val useCase =
                    useCase(
                        getAccountUseCase = getAccountUseCase(account = account),
                        requestSyncUseCase = requestSyncUseCase,
                        findContactUseCase = findContactUseCase(contact = stored),
                        accountContactRepository = accountContactRepository,
                    )

                When("이름도 함께 바꿔 수정한다") {
                    Then("TC-CONTACT-DETAIL-DOMAIN-004 전화번호 공백 예외로 실패하고 아무 내용도 수정하지 않는다") {
                        val phoneNumberList =
                            listOf(
                                ContactPhoneNumber(number = "010-1234-5678"),
                                ContactPhoneNumber(number = blankNumber),
                            )

                        val result =
                            useCase(
                                parameter =
                                    UpdateContactUseCase.Parameter(
                                        id = stored.id,
                                        detail = detail(phoneNumberList = phoneNumberList),
                                    ),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<ContactPhoneNumberBlankException>()
                        coVerify(exactly = 0) {
                            accountContactRepository.updateDetail(
                                account = any(),
                                contactId = any(),
                                detail = any(),
                                updatedAt = any(),
                            )
                        }
                        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                    }
                }

                When("이름을 비우고 번호가 없는 전화번호 항목과 함께 수정한다") {
                    Then("TC-CONTACT-DETAIL-DOMAIN-004 전화번호를 먼저 판단해 아무 내용도 수정하지 않는다") {
                        val result =
                            useCase(
                                parameter =
                                    UpdateContactUseCase.Parameter(
                                        id = stored.id,
                                        detail =
                                            detail(
                                                name = "",
                                                phoneNumberList = listOf(ContactPhoneNumber(number = blankNumber)),
                                            ),
                                    ),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<ContactPhoneNumberBlankException>()
                        coVerify(exactly = 0) {
                            accountContactRepository.updateDetail(
                                account = any(),
                                contactId = any(),
                                detail = any(),
                                updatedAt = any(),
                            )
                        }
                    }
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val stored = contact()
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = Account.Guest),
                    requestSyncUseCase = requestSyncUseCase(),
                    findContactUseCase = findContactUseCase(contact = stored),
                    accountContactRepository = accountContactRepository,
                )

            When("연락처를 수정한다") {
                Then("TC-CONTACT-DETAIL-DATA-011 게스트 계정 기준으로 기기에만 수정을 반영한다") {
                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        accountContactRepository.updateDetail(
                            account = Account.Guest,
                            contactId = stored.id,
                            detail = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    findContactUseCase = findContactUseCase(contact = contact()),
                    accountContactRepository = accountContactRepository,
                )

            When("연락처를 수정한다") {
                Then("계정 조회 실패를 전달하고 수정을 저장하지 않는다") {
                    useCase(parameter = UpdateContactUseCase.Parameter(id = Uuid.random(), detail = detail()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountContactRepository.updateDetail(account = any(), contactId = any(), detail = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("수정 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val stored = contact()
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery {
                accountContactRepository.updateDetail(account = account, contactId = any(), detail = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase,
                    findContactUseCase = findContactUseCase(contact = stored),
                    accountContactRepository = accountContactRepository,
                )

            When("연락처를 수정한다") {
                Then("TC-CONTACT-DETAIL-DATA-009 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun useCase(
            getAccountUseCase: GetAccountUseCase,
            requestSyncUseCase: RequestSyncUseCase,
            findContactUseCase: FindContactUseCase,
            accountContactRepository: AccountContactRepository,
            now: Instant? = null,
        ): UpdateContactUseCase {
            val clock =
                if (now == null) {
                    Clock.System
                } else {
                    mockk<Clock>().also { clock -> every { clock.now() } returns now }
                }

            return UpdateContactUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                findContactUseCase = findContactUseCase,
                accountContactRepository = accountContactRepository,
                clock = clock,
            )
        }

        private fun getAccountUseCase(account: Account): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(Result.success(account))

            return useCase
        }

        private fun findContactUseCase(contact: Contact): FindContactUseCase {
            val useCase = mockk<FindContactUseCase>()
            every { useCase(parameter = contact.id) } returns flowOf(Result.success(contact))

            return useCase
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun contact(): Contact =
            Contact(
                id = Uuid.random(),
                detail = detail(name = "stored-${fixtureMonkey.giveMeOne<String>()}"),
                isFavorite = false,
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        @Suppress("LongParameterList")
        private fun detail(
            name: String = "name-${fixtureMonkey.giveMeOne<String>()}",
            description: String = "description-${fixtureMonkey.giveMeOne<String>()}",
            height: io.github.taetae98coding.diary.core.model.measure.Length? = null,
            footSize: io.github.taetae98coding.diary.core.model.measure.Length? = null,
            birthday: ContactBirthday? = null,
            hometown: String = "",
            phoneNumberList: List<ContactPhoneNumber> = emptyList(),
        ): ContactDetail =
            ContactDetail(
                name = name,
                description = description,
                height = height,
                footSize = footSize,
                birthday = birthday,
                hometown = hometown,
                phoneNumberList = phoneNumberList,
            )
    }
}
