package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.WebLogin
import io.github.taetae98coding.diary.domain.web.repository.WebLoginRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class SaveWebLoginUseCaseTest :
    BehaviorSpec({
        Given("보관해 둔 로그인 정보가 있고 웹 표시 수단에 지금의 세션 쿠키가 있다") {
            val savedLogin = fixtureMonkey.giveMeOne<WebLogin>()
            val currentLogin = fixtureMonkey.giveMeOne<WebLogin>()
            val repository = repository(savedLogin = savedLogin)
            coEvery { repository.readWebViewLogin(siteUrlSet = savedLogin.siteUrlSet) } returns currentLogin
            val useCase = SaveWebLoginUseCase(webLoginRepository = repository)

            When("앱이 활성 상태에서 벗어나 보관한다") {
                val result = useCase(parameter = Unit)

                Then("TC-908e16b2-fd82-4d9d-83b8-9d6431d795a4 웹 표시 수단에서 읽은 지금의 세션 쿠키를 보관한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.submitSavedLogin(login = currentLogin) }
                }

                Then("TC-d8af2d99-95b6-42e6-b4c4-a221586c9350 이전에 보관해 둔 것을 지금의 세션 쿠키로 모두 바꾼다") {
                    coVerify(exactly = 1) { repository.submitSavedLogin(login = any()) }
                    coVerify(exactly = 0) { repository.submitSavedLogin(login = savedLogin) }
                }

                Then("TC-5c2c51e7-49c1-4fd7-9f84-77c0426380ed 이전에 보관한 사이트의 주소에서도 세션 쿠키를 확인한다") {
                    coVerify(exactly = 1) { repository.readWebViewLogin(siteUrlSet = savedLogin.siteUrlSet) }
                }

                Then("TC-f2f9cbff-6f3d-47fe-a8cd-e5e8a527031e 웹 표시 수단의 로그인 정보를 기기에 바로 기록하도록 한 번 요청한다") {
                    coVerify(exactly = 1) { repository.flushWebViewLogin() }
                }
            }
        }

        Given("웹 표시 수단이 세션 쿠키를 확인하는 기능을 제공하지 않는다") {
            val repository = repository(savedLogin = fixtureMonkey.giveMeOne<WebLogin>())
            coEvery { repository.readWebViewLogin(siteUrlSet = any()) } returns null
            val useCase = SaveWebLoginUseCase(webLoginRepository = repository)

            When("앱이 활성 상태에서 벗어나 보관한다") {
                val result = useCase(parameter = Unit)

                Then("TC-5af3c19f-698d-4340-bade-b68e163a5da9 보관하지 않고 실패로도 다루지 않으며 기기에 바로 기록하도록 한 번 요청한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { repository.submitSavedLogin(login = any()) }
                    coVerify(exactly = 1) { repository.flushWebViewLogin() }
                }
            }
        }

        Given("웹 표시 수단에서 세션 쿠키를 읽지 못한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = repository(savedLogin = fixtureMonkey.giveMeOne<WebLogin>())
            coEvery { repository.readWebViewLogin(siteUrlSet = any()) } throws throwable
            val useCase = SaveWebLoginUseCase(webLoginRepository = repository)

            When("앱이 활성 상태에서 벗어나 보관한다") {
                val result = useCase(parameter = Unit)

                Then("TC-31ff2113-2f58-4cb0-b0c3-3d0e4ed631ba 이전에 보관해 둔 것을 바꾸지 않고 실패를 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { repository.submitSavedLogin(login = any()) }
                }
            }
        }

        Given("세션 쿠키가 보관되어 있고 앱을 시작해 로그인 정보를 되돌리는 중이다") {
            val currentLogin = fixtureMonkey.giveMeOne<WebLogin>()
            val isRestoring = MutableStateFlow(true)
            val repository = repository(savedLogin = fixtureMonkey.giveMeOne<WebLogin>())
            every { repository.getRestoring() } returns isRestoring
            coEvery { repository.readWebViewLogin(siteUrlSet = any()) } returns currentLogin
            val useCase = SaveWebLoginUseCase(webLoginRepository = repository)

            When("되돌리기가 끝나기 전에 보관할 계기가 오고, 그 뒤 되돌리기가 끝난다") {
                Then("TC-f5c2e8ed-f73a-45ee-93a8-359a500b39ef 되돌리기가 끝나기 전에는 보관하지 않고 끝난 뒤에 한 번 보관한다") {
                    runTest {
                        val result = async { useCase(parameter = Unit) }
                        runCurrent()

                        coVerify(exactly = 0) { repository.readWebViewLogin(siteUrlSet = any()) }
                        coVerify(exactly = 0) { repository.submitSavedLogin(login = any()) }

                        isRestoring.value = false

                        result.await().shouldBeSuccess()
                        coVerify(exactly = 1) { repository.submitSavedLogin(login = currentLogin) }
                    }
                }
            }
        }

        Given("앱 저장 영역에 쓰지 못한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = repository(savedLogin = fixtureMonkey.giveMeOne<WebLogin>())
            coEvery { repository.readWebViewLogin(siteUrlSet = any()) } returns fixtureMonkey.giveMeOne<WebLogin>()
            coEvery { repository.submitSavedLogin(login = any()) } throws throwable
            val useCase = SaveWebLoginUseCase(webLoginRepository = repository)

            When("앱이 활성 상태에서 벗어나 보관한다") {
                val result = useCase(parameter = Unit)

                Then("TC-31ff2113-2f58-4cb0-b0c3-3d0e4ed631ba 실패를 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun repository(savedLogin: WebLogin): WebLoginRepository {
            val repository = mockk<WebLoginRepository>()
            coEvery { repository.readSavedLogin() } returns savedLogin
            coEvery { repository.submitSavedLogin(login = any()) } just Runs
            coEvery { repository.flushWebViewLogin() } just Runs
            every { repository.getRestoring() } returns MutableStateFlow(false)

            return repository
        }
    }
}
