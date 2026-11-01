package io.github.taetae98coding.diary.domain.web.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.WebLogin
import io.github.taetae98coding.diary.core.model.web.WebLoginCookie
import io.github.taetae98coding.diary.core.model.web.WebLoginRestoreState
import io.github.taetae98coding.diary.domain.web.repository.WebLoginRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk

class RestoreWebLoginUseCaseTest :
    BehaviorSpec({
        Given("세션 쿠키가 보관되어 있다") {
            val savedLogin = WebLogin(sessionCookieList = listOf(fixtureMonkey.giveMeOne<WebLoginCookie>()))
            val repository = mockk<WebLoginRepository>()
            coEvery { repository.readSavedLogin() } returns savedLogin
            coEvery { repository.upsertWebViewLogin(login = any()) } just Runs
            justRun { repository.setRestoring() }
            justRun { repository.unsetRestoring() }
            val useCase = RestoreWebLoginUseCase(webLoginRepository = repository)

            When("앱을 시작해 되돌린다") {
                Then("TC-8deae54a-4c48-42e5-ae00-37a42552acb5 TC-6054d52b-e3a5-4e4f-beb3-856bf404688b 되돌리는 중이 된 뒤 보관해 둔 세션 쿠키를 웹 표시 수단에 한 번 넣고 끝난다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe WebLoginRestoreState.RESTORING
                        awaitItem().shouldBeSuccess() shouldBe WebLoginRestoreState.DONE
                        awaitComplete()
                    }

                    coVerifyOrder {
                        repository.setRestoring()
                        repository.upsertWebViewLogin(login = savedLogin)
                        repository.unsetRestoring()
                    }
                }
            }
        }

        Given("보관된 세션 쿠키가 하나도 없다") {
            val repository = mockk<WebLoginRepository>()
            coEvery { repository.readSavedLogin() } returns WebLogin(siteUrlSet = setOf(fixtureMonkey.giveMeOne<String>()))
            justRun { repository.setRestoring() }
            justRun { repository.unsetRestoring() }
            val useCase = RestoreWebLoginUseCase(webLoginRepository = repository)

            When("앱을 시작해 되돌린다") {
                Then("TC-44b89df3-9791-4f8f-8de6-048c4714a94a 되돌리는 중을 거치지 않고 웹 표시 수단에 쿠키를 넣지 않는다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe WebLoginRestoreState.DONE
                        awaitComplete()
                    }

                    coVerify(exactly = 0) { repository.upsertWebViewLogin(login = any()) }
                }
            }
        }

        Given("보관한 세션 쿠키를 읽지 못한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<WebLoginRepository>()
            coEvery { repository.readSavedLogin() } throws throwable
            justRun { repository.setRestoring() }
            justRun { repository.unsetRestoring() }
            val useCase = RestoreWebLoginUseCase(webLoginRepository = repository)

            When("앱을 시작해 되돌린다") {
                Then("TC-9ffe581d-39f9-4181-9ad3-b1e6aca53a53 웹 표시 수단에 쿠키를 넣지 않고 실패를 전달한 뒤 끝난다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeFailure().shouldBeSameInstanceAs(throwable)
                        awaitComplete()
                    }

                    coVerify(exactly = 0) { repository.upsertWebViewLogin(login = any()) }
                    coVerify(exactly = 1) { repository.unsetRestoring() }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
