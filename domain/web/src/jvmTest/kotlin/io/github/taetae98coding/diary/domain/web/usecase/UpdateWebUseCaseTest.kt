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
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
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
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UpdateWebUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = web()
            val detailSlot = slot<WebDetail>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = getAccountUseCase(account = account)
            val findWebUseCase = findWebUseCase(web = stored)
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDetail(
                    account = account,
                    webId = stored.id,
                    detail = capture(detailSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = instant()
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    findWebUseCase = findWebUseCase,
                    accountWebRepository = accountWebRepository,
                    now = now,
                )

            When("제목, 설명, URL과 요청 헤더를 모두 바꿔 수정한다") {
                Then("TC-WEB-DETAIL-DOMAIN-023 입력한 내용과 수정 시점을 반영한다") {
                    val detail = detail(headerList = listOf(WebHeader(name = "Authorization", value = "Bearer token")))

                    useCase(parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                    detailSlot.captured shouldBe detail
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-WEB-DETAIL-DOMAIN-026 이름이 같은 헤더도 합치지 않고 입력한 순서대로 반영한다") {
                    val first = WebHeader(name = "Accept", value = "text/html")
                    val second = WebHeader(name = "Accept", value = "application/json")
                    val third = WebHeader(name = "X-Region", value = "")

                    useCase(
                        parameter =
                            UpdateWebUseCase.Parameter(
                                id = stored.id,
                                detail = detail(headerList = listOf(first, second, third)),
                            ),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.headerList shouldBe listOf(first, second, third)
                }

                Then("TC-WEB-DETAIL-DOMAIN-023 설명과 요청 헤더를 비운 수정도 그대로 반영한다") {
                    useCase(
                        parameter =
                            UpdateWebUseCase.Parameter(
                                id = stored.id,
                                detail = detail(description = "", headerList = emptyList()),
                            ),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.description shouldBe ""
                    detailSlot.captured.headerList shouldBe emptyList()
                }

                Then("TC-WEB-DETAIL-DATA-011 TC-WEB-DETAIL-DATA-012 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("제목이나 URL을 비운 채 수정한다") {
                Then("TC-WEB-DETAIL-DOMAIN-025 저장된 기존 제목과 URL을 사용한다") {
                    listOf(
                        Triple("", "https://changed.example.com", "빈 제목"),
                        Triple("   ", "https://changed.example.com", "공백 제목"),
                    ).forEach { (title, url, _) ->
                        useCase(
                            parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail(title = title, url = url)),
                        ).shouldBeSuccess(1)

                        detailSlot.captured.title shouldBe stored.detail.title
                        detailSlot.captured.url shouldBe url
                    }

                    listOf("", "   ").forEach { url ->
                        val title = "changed-${fixtureMonkey.giveMeOne<String>()}"

                        useCase(
                            parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail(title = title, url = url)),
                        ).shouldBeSuccess(1)

                        detailSlot.captured.title shouldBe title
                        detailSlot.captured.url shouldBe stored.detail.url
                    }

                    useCase(
                        parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail(title = "", url = "")),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.title shouldBe stored.detail.title
                    detailSlot.captured.url shouldBe stored.detail.url
                }

                Then("TC-WEB-DETAIL-DOMAIN-025 설명과 요청 헤더는 입력한 대로 반영한다") {
                    val header = WebHeader(name = "Accept", value = "text/html")
                    val description = "description-${fixtureMonkey.giveMeOne<String>()}"

                    useCase(
                        parameter =
                            UpdateWebUseCase.Parameter(
                                id = stored.id,
                                detail = detail(title = "", url = "", description = description, headerList = listOf(header)),
                            ),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.description shouldBe description
                    detailSlot.captured.headerList shouldBe listOf(header)
                }
            }
        }

        listOf("", "   ").forEach { blankName ->
            Given("이름이 `$blankName` 인 헤더 항목이 섞여 있다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val stored = web()
                val getAccountUseCase = getAccountUseCase(account = account)
                val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
                val requestSyncUseCase = requestSyncUseCase()
                val useCase =
                    useCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase,
                        findWebUseCase = findWebUseCase(web = stored),
                        accountWebRepository = accountWebRepository,
                    )

                When("제목과 URL도 함께 바꿔 수정한다") {
                    Then("TC-WEB-DETAIL-DOMAIN-024 헤더 이름 공백 예외로 실패하고 아무 내용도 수정하지 않는다") {
                        val headerList =
                            listOf(
                                WebHeader(name = "Authorization", value = "Bearer token"),
                                WebHeader(name = blankName, value = "value"),
                            )

                        val result =
                            useCase(
                                parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail(headerList = headerList)),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<WebHeaderNameBlankException>()
                        coVerify(exactly = 0) {
                            accountWebRepository.updateDetail(account = any(), webId = any(), detail = any(), updatedAt = any())
                        }
                        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val stored = web()
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = Account.Guest),
                    requestSyncUseCase = requestSyncUseCase(),
                    findWebUseCase = findWebUseCase(web = stored),
                    accountWebRepository = accountWebRepository,
                )

            When("웹 항목을 수정한다") {
                Then("TC-WEB-DETAIL-DATA-013 게스트 계정 기준으로 기기에만 수정을 반영한다") {
                    useCase(parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        accountWebRepository.updateDetail(account = Account.Guest, webId = stored.id, detail = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    findWebUseCase = findWebUseCase(web = web()),
                    accountWebRepository = accountWebRepository,
                )

            When("웹 항목을 수정한다") {
                Then("계정 조회 실패를 전달하고 수정을 저장하지 않는다") {
                    useCase(parameter = UpdateWebUseCase.Parameter(id = Uuid.random(), detail = detail()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountWebRepository.updateDetail(account = any(), webId = any(), detail = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("수정 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val stored = web()
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDetail(account = account, webId = any(), detail = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase,
                    findWebUseCase = findWebUseCase(web = stored),
                    accountWebRepository = accountWebRepository,
                )

            When("웹 항목을 수정한다") {
                Then("TC-WEB-DETAIL-DATA-010 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = UpdateWebUseCase.Parameter(id = stored.id, detail = detail()))
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
            findWebUseCase: FindWebUseCase,
            accountWebRepository: AccountWebRepository,
            now: Instant? = null,
        ): UpdateWebUseCase {
            val clock =
                if (now == null) {
                    Clock.System
                } else {
                    mockk<Clock>().also { clock -> every { clock.now() } returns now }
                }

            return UpdateWebUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                findWebUseCase = findWebUseCase,
                accountWebRepository = accountWebRepository,
                clock = clock,
            )
        }

        private fun getAccountUseCase(account: Account): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(Result.success(account))

            return useCase
        }

        private fun findWebUseCase(web: Web): FindWebUseCase {
            val useCase = mockk<FindWebUseCase>()
            every { useCase(parameter = web.id) } returns flowOf(Result.success(web))

            return useCase
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun web(): Web =
            Web(
                id = Uuid.random(),
                detail = detail(),
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun detail(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            description: String = "description-${fixtureMonkey.giveMeOne<String>()}",
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
