package io.github.taetae98coding.diary.domain.contact.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.exception.ContactNameBlankException
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
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
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant

class AddContactUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 이름",
            "   " to "공백 문자로만 이루어진 이름",
        ).forEach { (blankName, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
                val useCase =
                    AddContactUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountContactRepository = accountContactRepository,
                        clock = Clock.System,
                    )

                When("연락처를 추가한다") {
                    Then("TC-CONTACT-ADD-DOMAIN-001 이름 공백 예외로 실패하고 연락처를 저장하지 않는다") {
                        val result = useCase(parameter = AddContactUseCase.Parameter(detail = detail(name = blankName)))

                        result.shouldBeFailure().shouldBeInstanceOf<ContactNameBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountContactRepository.upsert(account = any(), contact = any()) }
                    }
                }
            }
        }

        listOf(
            "" to "번호가 빈 전화번호 항목",
            "   " to "번호가 공백 문자로만 이루어진 전화번호 항목",
        ).forEach { (blankNumber, label) ->
            Given("공백이 아닌 이름과 $label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
                val useCase =
                    AddContactUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountContactRepository = accountContactRepository,
                        clock = Clock.System,
                    )

                When("연락처를 추가한다") {
                    Then("TC-CONTACT-ADD-DOMAIN-002 전화번호 공백 예외로 실패하고 연락처를 저장하지 않는다") {
                        val phoneNumberList =
                            listOf(
                                ContactPhoneNumber(number = "010-1234-5678"),
                                ContactPhoneNumber(number = blankNumber),
                            )

                        val result = useCase(parameter = AddContactUseCase.Parameter(detail = detail(phoneNumberList = phoneNumberList)))

                        result.shouldBeFailure().shouldBeInstanceOf<ContactPhoneNumberBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountContactRepository.upsert(account = any(), contact = any()) }
                    }
                }
            }
        }

        Given("이름이 비어 있고 번호가 비어 있는 전화번호 항목도 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountContactRepository = accountContactRepository,
                    clock = Clock.System,
                )

            When("연락처를 추가한다") {
                Then("TC-CONTACT-ADD-DOMAIN-003 이름 공백 예외만 전달한다") {
                    val detail = detail(name = "", phoneNumberList = listOf(ContactPhoneNumber(number = "")))

                    val result = useCase(parameter = AddContactUseCase.Parameter(detail = detail))

                    result.shouldBeFailure().shouldBeInstanceOf<ContactNameBlankException>()
                }
            }
        }

        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactSlot = slot<Contact>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery { accountContactRepository.upsert(account = account, contact = capture(contactSlot)) } just Runs
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountContactRepository = accountContactRepository,
                    clock = clock,
                )

            When("공백이 아닌 이름으로 연락처를 추가한다") {
                Then("TC-CONTACT-ADD-DOMAIN-009 현재 계정과 연결된 고유한 연락처로 저장한다") {
                    val firstId = useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()
                    val secondId = useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    firstId shouldNotBe secondId
                    coVerify(exactly = 2) { accountContactRepository.upsert(account = account, contact = any()) }
                }

                Then("TC-CONTACT-ADD-DOMAIN-009 입력한 이름, 설명, 키, 신발 사이즈, 생일, 전화번호를 그대로 저장한다") {
                    val expected =
                        detail(
                            height = 175.5.centimeter,
                            footSize = 250.millimeter,
                            birthday =
                                ContactBirthday(
                                    date = LocalDate(year = 1998, month = 5, day = 12),
                                    calendar = ContactBirthdayCalendar.LUNAR,
                                ),
                            phoneNumberList =
                                listOf(
                                    ContactPhoneNumber(number = "010-1234-5678"),
                                    ContactPhoneNumber(number = "02-987-6543"),
                                ),
                        )

                    val result = useCase(parameter = AddContactUseCase.Parameter(detail = expected))

                    result.shouldBeSuccess(contactSlot.captured.id)
                    contactSlot.captured.detail shouldBe expected
                }

                Then("TC-CONTACT-ADD-DOMAIN-009 미삭제 상태와 추가 시각을 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    contactSlot.captured.isDeleted shouldBe false
                    contactSlot.captured.createdAt shouldBe now
                    contactSlot.captured.updatedAt shouldBe now
                }

                Then("TC-CONTACT-ADD-DOMAIN-010 설명을 입력하지 않으면 빈 설명으로 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(description = ""))).shouldBeSuccess()

                    contactSlot.captured.detail.description shouldBe ""
                }

                Then("TC-CONTACT-ADD-DOMAIN-004 설명과 전화번호가 비어 있어도 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(description = ""))).shouldBeSuccess()

                    contactSlot.captured.detail.description shouldBe ""
                    contactSlot.captured.detail.phoneNumberList
                        .shouldBeEmpty()
                }

                Then("TC-CONTACT-ADD-DOMAIN-005 같은 번호를 여러 개 입력해도 배치한 순서대로 모두 저장한다") {
                    val phoneNumberList =
                        listOf(
                            ContactPhoneNumber(number = "010-1234-5678"),
                            ContactPhoneNumber(number = "02-987-6543"),
                            ContactPhoneNumber(number = "010-1234-5678"),
                        )

                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(phoneNumberList = phoneNumberList))).shouldBeSuccess()

                    contactSlot.captured.detail.phoneNumberList shouldBe phoneNumberList
                }

                Then("TC-CONTACT-ADD-DOMAIN-012 키, 신발 사이즈, 생일을 비워도 없는 값으로 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    contactSlot.captured.detail.height
                        .shouldBeNull()
                    contactSlot.captured.detail.footSize
                        .shouldBeNull()
                    contactSlot.captured.detail.birthday
                        .shouldBeNull()
                }

                Then("TC-CONTACT-ADD-DOMAIN-013 고른 달력 구분을 함께 저장한다") {
                    ContactBirthdayCalendar.entries.forEach { calendar ->
                        val birthday = ContactBirthday(date = LocalDate(year = 1998, month = 5, day = 12), calendar = calendar)

                        useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthday = birthday))).shouldBeSuccess()

                        contactSlot.captured.detail.birthday shouldBe birthday
                    }
                }

                Then("TC-CONTACT-ADD-DOMAIN-014 음력 생일의 날짜를 환산하지 않고 저장한다") {
                    val date = LocalDate(year = 1998, month = 5, day = 12)
                    val birthday = ContactBirthday(date = date, calendar = ContactBirthdayCalendar.LUNAR)

                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthday = birthday))).shouldBeSuccess()

                    contactSlot.captured.detail.birthday
                        ?.date shouldBe date
                }

                Then("TC-CONTACT-ADD-DOMAIN-015 생일을 고르지 않으면 달력 구분도 없는 값으로 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthday = null))).shouldBeSuccess()

                    contactSlot.captured.detail.birthday
                        .shouldBeNull()
                }
            }
        }

        Given("계정이 게스트 상태로 준비되어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountContactRepository = accountContactRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 이름으로 연락처를 추가한다") {
                Then("게스트 계정으로 기기에 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    coVerify(exactly = 1) { accountContactRepository.upsert(account = Account.Guest, contact = any()) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountContactRepository = accountContactRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 이름으로 연락처를 추가한다") {
                Then("TC-CONTACT-ADD-DOMAIN-011 계정 조회 실패를 전달하고 연락처를 저장하지 않는다") {
                    val result = useCase(parameter = AddContactUseCase.Parameter(detail = detail()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountContactRepository.upsert(account = any(), contact = any()) }
                }
            }
        }

        Given("연락처 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery { accountContactRepository.upsert(account = account, contact = any()) } throws throwable
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountContactRepository = accountContactRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 이름으로 연락처를 추가한다") {
                Then("TC-CONTACT-ADD-DATA-003 저장 실패를 그대로 전달한다") {
                    val result = useCase(parameter = AddContactUseCase.Parameter(detail = detail()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
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

        private fun detail(
            name: String = "name-${fixtureMonkey.giveMeOne<String>()}",
            description: String = fixtureMonkey.giveMeOne<String>(),
            height: Length? = null,
            footSize: Length? = null,
            birthday: ContactBirthday? = null,
            phoneNumberList: List<ContactPhoneNumber> = emptyList(),
        ): ContactDetail =
            ContactDetail(
                name = name,
                description = description,
                height = height,
                footSize = footSize,
                birthday = birthday,
                phoneNumberList = phoneNumberList,
            )
    }
}
