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
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
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
import io.mockk.coVerifyOrder
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
                    Then("TC-8e809ab1-a0cc-4a31-82da-db63c6090a87 이름 공백 예외로 실패하고 연락처를 저장하지 않는다") {
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
                    Then("TC-f521335f-3177-4383-aa16-58f24c70f2e3 전화번호 공백 예외로 실패하고 연락처를 저장하지 않는다") {
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
                Then("TC-d4cfda06-81f8-4fa0-8bb0-0273e6e18ce9 이름 공백 예외만 전달한다") {
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
            val now = fixtureMonkey.giveMeOne<Instant>()
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
                Then("TC-94d69dd9-1da3-4891-bfdc-ec5a148ab86b 현재 계정과 연결된 고유한 연락처로 저장한다") {
                    val firstId = useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()
                    val secondId = useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    firstId shouldNotBe secondId
                    coVerify(exactly = 2) { accountContactRepository.upsert(account = account, contact = any()) }
                }

                Then("TC-94d69dd9-1da3-4891-bfdc-ec5a148ab86b 입력한 이름, 설명, 키, 신발 사이즈, 생일, 전화번호를 그대로 저장한다") {
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

                Then("TC-94d69dd9-1da3-4891-bfdc-ec5a148ab86b 미삭제 상태와 추가 시각을 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    contactSlot.captured.isDeleted shouldBe false
                    contactSlot.captured.createdAt shouldBe now
                    contactSlot.captured.updatedAt shouldBe now
                }

                Then("TC-4171dffb-3602-41d0-9431-eed5d61415b1 설명을 입력하지 않으면 빈 설명으로 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(description = ""))).shouldBeSuccess()

                    contactSlot.captured.detail.description shouldBe ""
                }

                Then("TC-54d47d79-d19d-4b62-9477-2f87590574a5 설명과 전화번호가 비어 있어도 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(description = ""))).shouldBeSuccess()

                    contactSlot.captured.detail.description shouldBe ""
                    contactSlot.captured.detail.phoneNumberList
                        .shouldBeEmpty()
                }

                Then("TC-1d6e49b3-91dc-4c6b-9121-13799d6b234e 같은 번호를 여러 개 입력해도 배치한 순서대로 모두 저장한다") {
                    val phoneNumberList =
                        listOf(
                            ContactPhoneNumber(number = "010-1234-5678"),
                            ContactPhoneNumber(number = "02-987-6543"),
                            ContactPhoneNumber(number = "010-1234-5678"),
                        )

                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(phoneNumberList = phoneNumberList))).shouldBeSuccess()

                    contactSlot.captured.detail.phoneNumberList shouldBe phoneNumberList
                }

                Then("TC-8df70bbc-e460-4a06-916f-a72e1bfd665d 키, 신발 사이즈, 생일은 없는 값으로, 고향은 비어 있는 값으로 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    contactSlot.captured.detail.height
                        .shouldBeNull()
                    contactSlot.captured.detail.footSize
                        .shouldBeNull()
                    contactSlot.captured.detail.birthday
                        .shouldBeNull()
                    contactSlot.captured.detail.birthYear
                        .shouldBeNull()
                    contactSlot.captured.detail.hometown shouldBe ""
                }

                Then("TC-c311e3f7-c458-47be-8b7a-f7372b578093 고향은 형식을 검사하지 않고 입력한 글자 그대로 저장한다") {
                    listOf("서울", "강원도 춘천시", "Seoul, Korea", "123-!@#").forEach { hometown ->
                        useCase(parameter = AddContactUseCase.Parameter(detail = detail(hometown = hometown))).shouldBeSuccess()

                        contactSlot.captured.detail.hometown shouldBe hometown
                    }
                }

                Then("TC-f657c761-b6b2-4766-926b-d5371703eddf 새 연락처는 즐겨찾기가 아닌 상태로 저장된다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    contactSlot.captured.isFavorite shouldBe false
                }

                Then("TC-42bcbb00-4eb9-4f6b-a50f-13ba0b425d6f 고른 달력 구분을 함께 저장한다") {
                    ContactBirthdayCalendar.entries.forEach { calendar ->
                        val birthday = ContactBirthday(date = LocalDate(year = 1998, month = 5, day = 12), calendar = calendar)

                        useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthday = birthday))).shouldBeSuccess()

                        contactSlot.captured.detail.birthday shouldBe birthday
                    }
                }

                Then("TC-7b219051-bda5-42dc-b846-bb5a0e9152a5 음력 생일의 날짜를 환산하지 않고 저장한다") {
                    val date = LocalDate(year = 1998, month = 5, day = 12)
                    val birthday = ContactBirthday(date = date, calendar = ContactBirthdayCalendar.LUNAR)

                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthday = birthday))).shouldBeSuccess()

                    contactSlot.captured.detail.birthday
                        ?.date shouldBe date
                }

                Then("TC-48688c58-82fc-404e-893a-8190e37f70c1 생일 없이 입력한 태어난 연도를 그대로 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthYear = 1990))).shouldBeSuccess()

                    contactSlot.captured.detail.birthYear shouldBe 1990
                    contactSlot.captured.detail.birthday
                        .shouldBeNull()
                }

                Then("TC-8ebf383b-e4a3-41c2-bcc2-e7e738291051 생일을 고르면 태어난 연도를 생일의 연도로 저장한다") {
                    listOf(
                        ContactBirthday(date = LocalDate(year = 1990, month = 5, day = 5), calendar = ContactBirthdayCalendar.SOLAR),
                        ContactBirthday(date = LocalDate(year = 1990, month = 12, day = 20), calendar = ContactBirthdayCalendar.LUNAR),
                    ).forEach { birthday ->
                        // 생일과 다른 연도가 함께 넘어와도 생일의 연도가 기록되는지 확인한다.
                        useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthday = birthday, birthYear = 1985))).shouldBeSuccess()

                        contactSlot.captured.detail.birthYear shouldBe 1990
                    }
                }

                Then("TC-c074e07c-8da6-4786-ad83-580d513d87eb 태어난 연도는 크기를 검사하지 않고 그대로 저장한다") {
                    listOf(2999, 990, 0).forEach { birthYear ->
                        useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthYear = birthYear))).shouldBeSuccess()

                        contactSlot.captured.detail.birthYear shouldBe birthYear
                    }
                }

                Then("TC-c0766bfb-2a2e-4bf6-89b1-dc3092ffbc50 생일을 고르지 않으면 달력 구분도 없는 값으로 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail(birthday = null))).shouldBeSuccess()

                    contactSlot.captured.detail.birthday
                        .shouldBeNull()
                }
            }
        }

        Given("연락처를 추가할 수 있는 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountContactRepository = accountContactRepository,
                    clock = Clock.System,
                )

            When("연락처 추가에 성공한다") {
                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-6dd12398-c5b0-4e7d-b36c-c8a61031b697 기기에 저장한 뒤 서버와 맞추기 위한 동기화를 한 번 요청한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    coVerifyOrder {
                        accountContactRepository.upsert(account = account, contact = any())
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("연락처 저장은 성공하지만 서버 반영이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountContactRepository = accountContactRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 이름으로 연락처를 추가한다") {
                Then("TC-c2430dea-6fab-43a7-95a1-9e5a7d58909c 추가는 성공으로 전달되고 저장한 연락처를 되돌리지 않는다") {
                    val contactSlot = slot<Contact>()
                    coEvery { accountContactRepository.upsert(account = account, contact = capture(contactSlot)) } just Runs

                    val contactId = useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    contactSlot.captured.id shouldBe contactId
                    contactSlot.captured.isDeleted shouldBe false
                    coVerify(exactly = 0) { accountContactRepository.updateDeleted(account = any(), contactId = any(), isDeleted = any(), updatedAt = any()) }
                }
            }
        }

        Given("계정이 게스트 상태로 준비되어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountContactRepository = accountContactRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 이름으로 연락처를 추가한다") {
                Then("TC-4a5232dc-0a4d-4973-a88c-bf54c2ec94dd 게스트 계정으로 기기에 저장한다") {
                    useCase(parameter = AddContactUseCase.Parameter(detail = detail())).shouldBeSuccess()

                    coVerify(exactly = 1) { accountContactRepository.upsert(account = Account.Guest, contact = any()) }
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
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
                Then("TC-cf596357-e696-4282-aebd-1d4001dc97ec 계정 조회 실패를 전달하고 연락처를 저장하지 않는다") {
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
                Then("TC-05a1f42a-bfb1-4a3d-b30b-7594415bdedf 추가를 성공으로 다루지 않고 저장 실패를 전달한다") {
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
            birthYear: Int? = birthday?.date?.year,
            hometown: String = "",
            phoneNumberList: List<ContactPhoneNumber> = emptyList(),
        ): ContactDetail =
            ContactDetail(
                name = name,
                description = description,
                height = height,
                footSize = footSize,
                birthday = birthday,
                birthYear = birthYear,
                hometown = hometown,
                phoneNumberList = phoneNumberList,
            )
    }
}
