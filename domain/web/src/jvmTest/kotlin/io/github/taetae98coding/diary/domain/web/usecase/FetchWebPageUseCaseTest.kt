package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.domain.web.repository.WebPageRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class FetchWebPageUseCaseTest :
    BehaviorSpec({
        Given("URL과 요청 헤더를 가진 웹 항목이 준비되어 있다") {
            val url = "https://developer.android.com/${fixtureMonkey.giveMeOne<Int>()}"
            val headerList =
                listOf(
                    WebHeader(name = "X-Diary", value = "first"),
                    WebHeader(name = "X-Diary", value = "second"),
                )
            val webPage = fixtureMonkey.giveMeOne<WebPage>()
            val webPageRepository = mockk<WebPageRepository>()
            coEvery { webPageRepository.fetch(url = url, headerList = headerList) } returns webPage
            val useCase = FetchWebPageUseCase(webPageRepository = webPageRepository)

            When("웹 페이지를 불러온다") {
                val result = useCase(parameter = FetchWebPageUseCase.Parameter(url = url, headerList = headerList))

                Then("TC-c0f80397-8368-4688-8bda-5f9884bf9f96 저장된 URL과 요청 헤더를 순서 그대로 사용한다") {
                    coVerify(exactly = 1) { webPageRepository.fetch(url = url, headerList = headerList) }
                }

                Then("TC-069417bf-bcdf-4e50-9f52-6337d1805387 받은 웹 페이지를 그대로 전달한다") {
                    result.shouldBeSuccess() shouldBe webPage
                }
            }
        }

        Given("웹 페이지 요청이 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val webPageRepository = mockk<WebPageRepository>()
            coEvery { webPageRepository.fetch(url = any(), headerList = any()) } throws throwable
            val useCase = FetchWebPageUseCase(webPageRepository = webPageRepository)

            When("웹 페이지를 불러온다") {
                Then("TC-78770bfe-3f95-4faf-bfad-b4ad0d438fa5 TC-db6726ec-6a9a-440c-bef1-a36f4c3196ca 실패를 그대로 전달한다") {
                    useCase(parameter = FetchWebPageUseCase.Parameter(url = fixtureMonkey.giveMeOne<String>(), headerList = emptyList()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
