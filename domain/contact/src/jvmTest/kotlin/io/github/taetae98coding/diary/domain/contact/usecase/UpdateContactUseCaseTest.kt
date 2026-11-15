package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.clearMocks
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
            val account = Arb.accountUser().next()
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
            val now = Arb.secondPrecisionInstant().next()
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
                Then("TC-94176a78-3cde-40e5-a765-31ebb560be13 입력한 내용과 수정 시점을 반영한다") {
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

                Then("TC-94176a78-3cde-40e5-a765-31ebb560be13 같은 번호도 합치지 않고 입력한 순서대로 반영한다") {
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

                Then("TC-f3979f2f-35c3-4167-bfe7-6ff671ff044e 비운 값은 없는 값이나 비어 있는 값으로 반영한다") {
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
                                        howWeMet = "",
                                        phoneNumberList = emptyList(),
                                    ),
                            ),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.description shouldBe ""
                    detailSlot.captured.height shouldBe null
                    detailSlot.captured.footSize shouldBe null
                    detailSlot.captured.birthday shouldBe null
                    detailSlot.captured.hometown shouldBe ""
                    detailSlot.captured.howWeMet shouldBe ""
                    detailSlot.captured.phoneNumberList shouldBe emptyList()
                }

                Then("TC-94176a78-3cde-40e5-a765-31ebb560be13 입력한 고향을 그대로 반영한다") {
                    val hometown = "hometown-${Arb.string().next()}"

                    useCase(
                        parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail(hometown = hometown)),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.hometown shouldBe hometown
                }

                Then("TC-94176a78-3cde-40e5-a765-31ebb560be13 입력한 알게 된 경로를 그대로 반영한다") {
                    val howWeMet = "how-we-met-${Arb.string().next()}"

                    useCase(
                        parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail(howWeMet = howWeMet)),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.howWeMet shouldBe howWeMet
                }

                Then("TC-a265a315-496d-4472-81d3-20daceae4119 달력 구분만 바꿔도 그 구분으로 반영한다") {
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

                Then("TC-8e1894ca-ff18-4efe-a31d-90eefe53eeeb 수정한 태어난 연도가 반영된다") {
                    listOf(
                        detail(birthYear = 1991) to 1991,
                        detail(birthYear = null) to null,
                        detail(birthday = ContactBirthday(date = LocalDate(1992, 3, 1), calendar = ContactBirthdayCalendar.SOLAR)) to 1992,
                        detail(birthday = null) to null,
                    ).forEach { (detail, expected) ->
                        useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                        detailSlot.captured.birthYear shouldBe expected
                    }
                }

                Then("생일과 다른 태어난 연도로 수정하면 생일의 연도로 반영한다") {
                    val birthday = ContactBirthday(date = LocalDate(1992, 3, 1), calendar = ContactBirthdayCalendar.LUNAR)

                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail(birthday = birthday, birthYear = 1985))).shouldBeSuccess(1)

                    detailSlot.captured.birthYear shouldBe 1992
                }

                Then("TC-b55bd3d9-08dc-4afb-b09e-d24205aca28e 수정은 삭제 여부와 생성 시각과 계정 연결을 바꾸지 않는다") {
                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess(1)

                    // 저장소는 내용과 수정 시각만 받으므로 삭제 여부·생성 시각·계정 연결은 바뀔 자리가 없다.
                    coVerify(exactly = 0) {
                        accountContactRepository.updateDeleted(account = any(), contactId = any(), isDeleted = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { accountContactRepository.upsert(account = any(), contact = any(), tagIdSet = any()) }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-46698d03-f59c-4489-9bd6-b8911ff63fa5 로컬 저장 결과로 성공을 판단하고 동기화를 한 번 요청한다") {
                    clearMocks(requestSyncUseCase, answers = false)

                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess(1)

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("이름 앞뒤에 공백을 넣어 수정한다") {
                Then("TC-e17ca81f-e623-4ac7-95c2-23e55248187e 앞뒤 공백을 지운 이름으로 반영한다") {
                    listOf(
                        "김철수 " to "김철수",
                        "  김철수" to "김철수",
                        "김 철수" to "김 철수",
                    ).forEach { (input, expected) ->
                        useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail(name = input))).shouldBeSuccess(1)

                        detailSlot.captured.name shouldBe expected
                    }
                }
            }

            When("이름을 비운 채 수정한다") {
                Then("TC-d0baff5e-e6ef-4b37-b9e5-128b36176f79 저장된 기존 이름을 사용하고 나머지는 입력한 대로 반영한다") {
                    listOf("", "   ").forEach { name ->
                        val description = "description-${Arb.string().next()}"
                        val hometown = "hometown-${Arb.string().next()}"
                        val howWeMet = "how-we-met-${Arb.string().next()}"
                        val measured =
                            Arb
                                .contact(
                                    numberList = listOf("010-${Arb.string().next()}"),
                                    hasMeasure = true,
                                    isFavorite = false,
                                    isDeleted = false,
                                ).next()
                                .detail

                        useCase(
                            parameter =
                                UpdateContactUseCase.Parameter(
                                    id = stored.id,
                                    detail =
                                        detail(
                                            name = name,
                                            description = description,
                                            height = measured.height,
                                            footSize = measured.footSize,
                                            birthday = measured.birthday,
                                            hometown = hometown,
                                            howWeMet = howWeMet,
                                            phoneNumberList = measured.phoneNumberList,
                                        ),
                                ),
                        ).shouldBeSuccess(1)

                        detailSlot.captured.name shouldBe stored.detail.name
                        detailSlot.captured.description shouldBe description
                        detailSlot.captured.height shouldBe measured.height
                        detailSlot.captured.footSize shouldBe measured.footSize
                        detailSlot.captured.birthday shouldBe measured.birthday
                        detailSlot.captured.hometown shouldBe hometown
                        detailSlot.captured.howWeMet shouldBe howWeMet
                        detailSlot.captured.phoneNumberList shouldBe measured.phoneNumberList
                    }
                }
            }
        }

        listOf("", "   ").forEach { blankNumber ->
            Given("번호가 `$blankNumber` 인 전화번호 항목이 섞여 있다") {
                val account = Arb.accountUser().next()
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
                    Then("TC-73f1269c-6fb7-4153-839d-fc98d7fb4529 전화번호 공백 예외로 실패하고 아무 내용도 수정하지 않는다") {
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
                    Then("TC-7182b46f-2795-437e-8947-9baa100c0a81 이름을 비워도 전화번호를 먼저 판단해 아무 내용도 수정하지 않는다") {
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
                Then("TC-54110044-b9ea-4ff2-b2c1-29777056471f 게스트 계정 기준으로 기기에만 수정을 반영한다") {
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
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(throwable))
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
            val account = Arb.accountUser().next()
            val throwable = IllegalStateException(Arb.string().next())
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
                Then("TC-1baea3d4-f655-46b8-8d62-64d922ba1d95 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = UpdateContactUseCase.Parameter(id = stored.id, detail = detail()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }
    }) {
    public companion object {
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
            every { useCase() } returns flowOf(Result.success(account))

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

        private fun contact(): Contact =
            Contact(
                id = Uuid.random(),
                detail = detail(name = "stored-${Arb.string().next()}"),
                isFavorite = false,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )

        @Suppress("LongParameterList")
        private fun detail(
            name: String = "name-${Arb.string().next()}-name",
            description: String = "description-${Arb.string().next()}",
            height: io.github.taetae98coding.diary.core.model.measure.Length? = null,
            footSize: io.github.taetae98coding.diary.core.model.measure.Length? = null,
            birthday: ContactBirthday? = null,
            birthYear: Int? = birthday?.date?.year,
            hometown: String = "",
            howWeMet: String = "",
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
                howWeMet = howWeMet,
                phoneNumberList = phoneNumberList,
            )
    }
}
