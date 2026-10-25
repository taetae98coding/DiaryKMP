package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebTitleBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebUrlBlankException
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
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

class AddWebUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
                val useCase =
                    AddWebUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountWebRepository = accountWebRepository,
                        clock = Clock.System,
                    )

                When("웹 항목을 추가한다") {
                    Then("TC-f5faa20d-3491-4730-be44-e425c1e9935c 제목 공백 예외로 실패하고 웹 항목을 저장하지 않는다") {
                        val result = useCase(parameter = AddWebUseCase.Parameter(detail = detail(title = blankTitle), tagIdSet = emptySet()))

                        result.shouldBeFailure().shouldBeInstanceOf<WebTitleBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountWebRepository.upsert(account = any(), web = any(), tagIdSet = any()) }
                    }

                    Then("TC-4a1e7304-67de-44a4-9048-9a5689b164f0 URL도 성립하지 않으면 제목 공백 예외를 먼저 알린다") {
                        val result = useCase(parameter = AddWebUseCase.Parameter(detail = detail(title = blankTitle, url = "  "), tagIdSet = emptySet()))

                        result.shouldBeFailure().shouldBeInstanceOf<WebTitleBlankException>()
                        coVerify(exactly = 0) { accountWebRepository.upsert(account = any(), web = any(), tagIdSet = any()) }
                    }

                    Then("TC-4a1e7304-67de-44a4-9048-9a5689b164f0 이름 없는 헤더도 있으면 제목 공백 예외를 먼저 알린다") {
                        val result =
                            useCase(
                                parameter =
                                    AddWebUseCase.Parameter(
                                        detail = detail(title = blankTitle, headerList = listOf(WebHeader(name = "  ", value = "value"))),
                                        tagIdSet = emptySet(),
                                    ),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<WebTitleBlankException>()
                        coVerify(exactly = 0) { accountWebRepository.upsert(account = any(), web = any(), tagIdSet = any()) }
                    }
                }
            }
        }

        listOf(
            "" to "빈 URL",
            "   " to "공백 문자로만 이루어진 URL",
        ).forEach { (blankUrl, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
                val useCase =
                    AddWebUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountWebRepository = accountWebRepository,
                        clock = Clock.System,
                    )

                When("공백이 아닌 제목으로 웹 항목을 추가한다") {
                    Then("TC-f5faa20d-3491-4730-be44-e425c1e9935c URL 공백 예외로 실패하고 웹 항목을 저장하지 않는다") {
                        val result = useCase(parameter = AddWebUseCase.Parameter(detail = detail(url = blankUrl), tagIdSet = emptySet()))

                        result.shouldBeFailure().shouldBeInstanceOf<WebUrlBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountWebRepository.upsert(account = any(), web = any(), tagIdSet = any()) }
                    }
                }
            }
        }

        listOf(
            "" to "이름이 빈 헤더 항목",
            "   " to "이름이 공백 문자로만 이루어진 헤더 항목",
        ).forEach { (blankName, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
                val useCase =
                    AddWebUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountWebRepository = accountWebRepository,
                        clock = Clock.System,
                    )

                When("공백이 아닌 제목과 URL로 웹 항목을 추가한다") {
                    Then("TC-f5faa20d-3491-4730-be44-e425c1e9935c TC-32ad9e2d-7003-4f6e-b473-e4ae70ffe9db 헤더 이름 공백 예외로 실패하고 이름 있는 헤더만 남긴 웹 항목도 저장하지 않는다") {
                        val headerList =
                            listOf(
                                WebHeader(name = "Authorization", value = "Bearer token"),
                                WebHeader(name = blankName, value = "value"),
                            )

                        val result = useCase(parameter = AddWebUseCase.Parameter(detail = detail(headerList = headerList), tagIdSet = emptySet()))

                        result.shouldBeFailure().shouldBeInstanceOf<WebHeaderNameBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountWebRepository.upsert(account = any(), web = any(), tagIdSet = any()) }
                    }

                    Then("TC-4a1e7304-67de-44a4-9048-9a5689b164f0 URL도 성립하지 않으면 URL 공백 예외를 먼저 알린다") {
                        val result =
                            useCase(
                                parameter =
                                    AddWebUseCase.Parameter(
                                        detail =
                                            detail(
                                                url = "  ",
                                                headerList = listOf(WebHeader(name = blankName, value = "value")),
                                            ),
                                        tagIdSet = emptySet(),
                                    ),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<WebUrlBlankException>()
                        coVerify(exactly = 0) { accountWebRepository.upsert(account = any(), web = any(), tagIdSet = any()) }
                    }
                }
            }
        }

        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webSlot = slot<Web>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery { accountWebRepository.upsert(account = account, web = capture(webSlot), tagIdSet = any()) } just Runs
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목과 URL로 웹 항목을 추가한다") {
                Then("TC-74d4a2ac-0065-4a70-ad58-dbc468f985c2 TC-8ba2d9a1-8f33-429a-96a4-02bebea22ce2 현재 계정과 연결된 고유한 웹 항목으로 저장한다") {
                    val firstId = useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()
                    val secondId = useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    firstId shouldNotBe Uuid.NIL
                    secondId shouldNotBe Uuid.NIL
                    firstId shouldNotBe secondId
                    coVerify(exactly = 2) { accountWebRepository.upsert(account = account, web = any(), tagIdSet = any()) }
                }

                Then("TC-11189620-33ae-445d-9656-dd423b06803a TC-4923195f-f274-4a60-aa56-53f9bef6cde9 입력한 제목, 설명, URL과 요청 헤더를 그대로 저장한다") {
                    val expected = detail(headerList = listOf(WebHeader(name = "Authorization", value = "Bearer token")))

                    val result = useCase(parameter = AddWebUseCase.Parameter(detail = expected, tagIdSet = emptySet()))

                    result.shouldBeSuccess(webSlot.captured.id)
                    webSlot.captured.detail shouldBe expected
                }

                Then("TC-a5fc06fe-a481-403c-9f17-056a028e49b9 미삭제 상태와 추가 시각을 저장한다") {
                    useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    webSlot.captured.isDeleted shouldBe false
                    webSlot.captured.createdAt shouldBe now
                    webSlot.captured.updatedAt shouldBe now
                }

                Then("TC-8a31aa2e-8927-4d2d-87ee-584f01b3d108 설명과 요청 헤더가 비어 있어도 웹 항목을 저장한다") {
                    useCase(parameter = AddWebUseCase.Parameter(detail = detail(description = "", headerList = emptyList()), tagIdSet = emptySet())).shouldBeSuccess()

                    webSlot.captured.detail.description shouldBe ""
                    webSlot.captured.detail.headerList shouldBe emptyList()
                }
            }

            When("연결할 태그를 골라 웹 항목을 추가한다") {
                Then("TC-8b85f5ae-dd5e-4598-b6e8-78d9a31ec096 고른 태그를 그대로 새 웹 항목의 연결로 넘긴다") {
                    val tagIdSet = List(2) { fixtureMonkey.giveMeOne<Uuid>() }.toSet()
                    val repository = mockk<AccountWebRepository>(relaxed = true)
                    val taggedUseCase =
                        AddWebUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase(),
                            accountWebRepository = repository,
                            clock = clock,
                        )

                    taggedUseCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = tagIdSet)).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        repository.upsert(account = account, web = any(), tagIdSet = tagIdSet)
                    }
                }

                Then("TC-2ec01e81-8160-458c-9f9f-53201cd8e8db 태그를 하나도 고르지 않아도 웹 항목을 추가하고 연결을 만들지 않는다") {
                    val repository = mockk<AccountWebRepository>(relaxed = true)
                    val tagLessUseCase =
                        AddWebUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase(),
                            accountWebRepository = repository,
                            clock = clock,
                        )

                    tagLessUseCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        repository.upsert(account = account, web = any(), tagIdSet = emptySet())
                    }
                }
            }

            When("값이 비어 있는 요청 헤더를 함께 추가한다") {
                Then("TC-1d9e9574-84bb-4875-bb88-0968ac76966f TC-11189620-33ae-445d-9656-dd423b06803a 값이 비어 있어도 배치한 순서대로 저장한다") {
                    val first = WebHeader(name = "Authorization", value = "Bearer token")
                    val second = WebHeader(name = "Accept", value = "")

                    useCase(parameter = AddWebUseCase.Parameter(detail = detail(headerList = listOf(first, second)), tagIdSet = emptySet())).shouldBeSuccess()

                    webSlot.captured.detail.headerList shouldBe listOf(first, second)
                }

                Then("TC-db2e3d1a-4f4c-462a-9ba3-84fc3377c15d 이름이 같은 헤더를 합치지 않고 모두 저장한다") {
                    val first = WebHeader(name = "Accept", value = "text/html")
                    val second = WebHeader(name = "Accept", value = "application/json")

                    useCase(parameter = AddWebUseCase.Parameter(detail = detail(headerList = listOf(first, second)), tagIdSet = emptySet())).shouldBeSuccess()

                    webSlot.captured.detail.headerList shouldBe listOf(first, second)
                }
            }
        }

        Given("웹 항목을 추가할 수 있는 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("웹 항목 추가에 성공한다") {
                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-861e6069-e620-46f7-82d9-4be6e396b1c1 추가한 웹 항목을 서버와 맞추기 위한 동기화를 요청한다") {
                    useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("웹 항목 저장은 성공하지만 서버 반영이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
            val useCase =
                AddWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 URL로 웹 항목을 추가한다") {
                Then("TC-eb88071a-e798-4c8c-ab13-4d338b33baf1 추가는 성공으로 전달되고 저장한 웹 항목을 되돌리지 않는다") {
                    val webSlot = slot<Web>()
                    coEvery { accountWebRepository.upsert(account = account, web = capture(webSlot), tagIdSet = any()) } just Runs

                    useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    coVerify(exactly = 1) { accountWebRepository.upsert(account = account, web = any(), tagIdSet = any()) }
                    coVerify(exactly = 0) {
                        accountWebRepository.updateDeleted(
                            account = any(),
                            webId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                    webSlot.captured.isDeleted.shouldBeFalse()
                }
            }
        }

        Given("계정이 게스트 상태로 준비되어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                AddWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 URL로 웹 항목을 추가한다") {
                Then("TC-2d672161-9914-4650-ae19-2f77010ba903 게스트 계정으로 기기에 저장한다") {
                    useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    coVerify(exactly = 1) { accountWebRepository.upsert(account = Account.Guest, web = any(), tagIdSet = any()) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                AddWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 URL로 웹 항목을 추가한다") {
                Then("TC-017cf0e9-bcfc-4b80-98ea-2fb9860c082c 계정 조회 실패를 전달하고 웹 항목을 저장하지 않는다") {
                    val result = useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountWebRepository.upsert(account = any(), web = any(), tagIdSet = any()) }
                }
            }
        }

        Given("웹 항목 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery { accountWebRepository.upsert(account = account, web = any(), tagIdSet = any()) } throws throwable
            val useCase =
                AddWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 URL로 웹 항목을 추가한다") {
                Then("TC-5920ffe6-d055-49a0-9b3a-83743cf16050 추가를 성공으로 다루지 않고 저장 실패를 전달한다") {
                    val result = useCase(parameter = AddWebUseCase.Parameter(detail = detail(), tagIdSet = emptySet()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun requestSyncUseCase(): RequestSyncUseCase = mockk(relaxed = true)

        private fun detail(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            description: String = fixtureMonkey.giveMeOne<String>(),
            url: String = "https://example.com/${fixtureMonkey.giveMeOne<String>()}",
            headerList: List<WebHeader> = emptyList(),
        ): WebDetail =
            WebDetail(
                title = title,
                description = description,
                url = url,
                headerList = headerList,
            )
    }
}
