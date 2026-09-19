package io.github.taetae98coding.diary.domain.account.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.authentication.Session
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

class GetAccountUseCaseTest :
    BehaviorSpec({
        listOf(Session.Authenticated, Session.NotAuthenticated).forEach { session ->
            Given("저장된 사용자 정보가 없고 로그인 세션이 $session 이다") {
                val sessionRepository = mockk<SessionRepository>()
                every { sessionRepository.get() } returns flowOf(session)
                val userDataRepository = mockk<UserDataRepository>()
                every { userDataRepository.get() } returns flowOf<UserData?>(null)
                val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

                When("현재 계정 상태를 확인한다") {
                    Then("TC-ACCOUNT-DOMAIN-001 $session 계정이 게스트로 확정된다") {
                        useCase(Unit).test {
                            awaitItem().shouldBeSuccess() shouldBe Account.Guest
                            awaitComplete()
                        }
                    }
                }
            }
        }

        listOf("프로필 이미지가 있음", "프로필 이미지가 없음").forEach { profileImageCase ->
            Given("이메일과 $profileImageCase 상태의 사용자 정보가 저장되어 있다") {
                val email = fixtureMonkey.giveMeOne<String>()
                val profileImage = if (profileImageCase == "프로필 이미지가 있음") fixtureMonkey.giveMeOne<String>() else null
                val userData = UserData(id = fixtureMonkey.giveMeOne<Uuid>(), email = email, profileImage = profileImage)
                val sessionRepository = mockk<SessionRepository>()
                every { sessionRepository.get() } returns flowOf(Session.Authenticated)
                val userDataRepository = mockk<UserDataRepository>()
                every { userDataRepository.get() } returns flowOf<UserData?>(userData)
                val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

                When("현재 계정 상태를 확인한다") {
                    Then("TC-ACCOUNT-DOMAIN-002 $profileImageCase 계정이 사용자로 확정되고 이메일과 프로필 이미지를 그대로 가진다") {
                        useCase(Unit).test {
                            val user = awaitItem().shouldBeSuccess().shouldBeInstanceOf<Account.User>()
                            user.email shouldBe email
                            user.profileImage shouldBe profileImage
                            awaitComplete()
                        }
                    }
                }
            }
        }

        listOf(
            Session.Authenticated to true,
            Session.NotAuthenticated to false,
        ).forEach { (session, isSessionValid) ->
            Given("사용자 정보가 저장되어 있고 로그인 세션이 $session 이다") {
                val userData = UserData(id = fixtureMonkey.giveMeOne<Uuid>(), email = fixtureMonkey.giveMeOne<String>(), profileImage = fixtureMonkey.giveMeOne<String>())
                val sessionRepository = mockk<SessionRepository>()
                every { sessionRepository.get() } returns flowOf(session)
                val userDataRepository = mockk<UserDataRepository>()
                every { userDataRepository.get() } returns flowOf<UserData?>(userData)
                val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

                When("현재 계정 상태를 확인한다") {
                    Then("TC-ACCOUNT-DOMAIN-003 $session 세션 갱신 여부가 $isSessionValid 이다") {
                        useCase(Unit).test {
                            val user = awaitItem().shouldBeSuccess().shouldBeInstanceOf<Account.User>()
                            user.isSessionValid shouldBe isSessionValid
                            awaitComplete()
                        }
                    }
                }
            }
        }

        Given("로그인 세션만 첫 값을 확인하고 저장된 사용자 정보는 아직 확인되지 않았다") {
            val sessionRepository = mockk<SessionRepository>()
            every { sessionRepository.get() } returns flowOf(Session.Authenticated)
            val userDataRepository = mockk<UserDataRepository>()
            every { userDataRepository.get() } returns MutableSharedFlow<UserData?>()
            val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

            When("현재 계정 상태를 확인한다") {
                Then("TC-ACCOUNT-DOMAIN-004 세션만 확인되면 계정 상태를 확정하지 않는다") {
                    useCase(Unit).test {
                        expectNoEvents()
                    }
                }
            }
        }

        Given("저장된 사용자 정보만 첫 값을 확인하고 로그인 세션은 아직 확인되지 않았다") {
            val userData = UserData(id = fixtureMonkey.giveMeOne<Uuid>(), email = fixtureMonkey.giveMeOne<String>(), profileImage = fixtureMonkey.giveMeOne<String>())
            val sessionRepository = mockk<SessionRepository>()
            every { sessionRepository.get() } returns MutableSharedFlow<Session>()
            val userDataRepository = mockk<UserDataRepository>()
            every { userDataRepository.get() } returns flowOf<UserData?>(userData)
            val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

            When("현재 계정 상태를 확인한다") {
                Then("TC-ACCOUNT-DOMAIN-004 사용자 정보만 확인되면 계정 상태를 확정하지 않는다") {
                    useCase(Unit).test {
                        expectNoEvents()
                    }
                }
            }
        }

        Given("저장된 사용자 정보가 없어 계정이 게스트로 확정되어 있다") {
            val userData = UserData(id = fixtureMonkey.giveMeOne<Uuid>(), email = fixtureMonkey.giveMeOne<String>(), profileImage = fixtureMonkey.giveMeOne<String>())
            val sessionFlow = MutableStateFlow<Session>(Session.Authenticated)
            val userDataFlow = MutableStateFlow<UserData?>(null)
            val sessionRepository = mockk<SessionRepository>()
            every { sessionRepository.get() } returns sessionFlow
            val userDataRepository = mockk<UserDataRepository>()
            every { userDataRepository.get() } returns userDataFlow
            val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

            When("사용자 정보가 새로 저장된다") {
                Then("TC-ACCOUNT-DOMAIN-005 계정 상태가 사용자로 바뀐다") {
                    useCase(Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe Account.Guest
                        userDataFlow.value = userData
                        awaitItem().shouldBeSuccess().shouldBeInstanceOf<Account.User>()
                    }
                }
            }
        }

        Given("사용자 정보가 저장되어 계정이 사용자로 확정되어 있다") {
            val userData = UserData(id = fixtureMonkey.giveMeOne<Uuid>(), email = fixtureMonkey.giveMeOne<String>(), profileImage = fixtureMonkey.giveMeOne<String>())
            val sessionFlow = MutableStateFlow<Session>(Session.Authenticated)
            val userDataFlow = MutableStateFlow<UserData?>(userData)
            val sessionRepository = mockk<SessionRepository>()
            every { sessionRepository.get() } returns sessionFlow
            val userDataRepository = mockk<UserDataRepository>()
            every { userDataRepository.get() } returns userDataFlow
            val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

            When("저장된 사용자 정보가 사라진다") {
                Then("TC-ACCOUNT-DOMAIN-006 계정 상태가 게스트로 바뀐다") {
                    useCase(Unit).test {
                        awaitItem().shouldBeSuccess().shouldBeInstanceOf<Account.User>()
                        userDataFlow.value = null
                        awaitItem().shouldBeSuccess() shouldBe Account.Guest
                    }
                }
            }
        }

        Given("사용자 정보가 저장되어 있고 로그인 세션이 인증되지 않아 세션 갱신되지 않은 사용자로 확정되어 있다") {
            val userData = UserData(id = fixtureMonkey.giveMeOne<Uuid>(), email = fixtureMonkey.giveMeOne<String>(), profileImage = fixtureMonkey.giveMeOne<String>())
            val sessionFlow = MutableStateFlow<Session>(Session.NotAuthenticated)
            val userDataFlow = MutableStateFlow<UserData?>(userData)
            val sessionRepository = mockk<SessionRepository>()
            every { sessionRepository.get() } returns sessionFlow
            val userDataRepository = mockk<UserDataRepository>()
            every { userDataRepository.get() } returns userDataFlow
            val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

            When("사용자 정보는 그대로인 채 로그인 세션이 인증된 상태로 바뀐다") {
                Then("TC-ACCOUNT-DOMAIN-007 계정이 사용자로 유지되고 세션 갱신 여부만 갱신됨으로 바뀐다") {
                    useCase(Unit).test {
                        val before = awaitItem().shouldBeSuccess().shouldBeInstanceOf<Account.User>()
                        before.isSessionValid.shouldBeFalse()
                        sessionFlow.value = Session.Authenticated
                        val after = awaitItem().shouldBeSuccess().shouldBeInstanceOf<Account.User>()
                        after.isSessionValid.shouldBeTrue()
                        after.id shouldBe before.id
                        after.email shouldBe before.email
                        after.profileImage shouldBe before.profileImage
                    }
                }
            }
        }

        Given("로그인 세션을 확인하는 중 오류가 발생한다") {
            val sessionRepository = mockk<SessionRepository>()
            every { sessionRepository.get() } returns flow { throw IllegalStateException("session error") }
            val userDataRepository = mockk<UserDataRepository>()
            every { userDataRepository.get() } returns flowOf<UserData?>(null)
            val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

            When("현재 계정 상태를 확인한다") {
                Then("TC-ACCOUNT-DOMAIN-008 세션 오류가 조회 실패로 전달된다") {
                    useCase(Unit).test {
                        awaitItem().shouldBeFailure()
                        awaitComplete()
                    }
                }
            }
        }

        Given("저장된 사용자 정보를 확인하는 중 오류가 발생한다") {
            val sessionRepository = mockk<SessionRepository>()
            every { sessionRepository.get() } returns flowOf(Session.Authenticated)
            val userDataRepository = mockk<UserDataRepository>()
            every { userDataRepository.get() } returns flow { throw IllegalStateException("userData error") }
            val useCase = GetAccountUseCase(sessionRepository = sessionRepository, userDataRepository = userDataRepository)

            When("현재 계정 상태를 확인한다") {
                Then("TC-ACCOUNT-DOMAIN-008 사용자 정보 오류가 조회 실패로 전달된다") {
                    useCase(Unit).test {
                        awaitItem().shouldBeFailure()
                        awaitComplete()
                    }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
