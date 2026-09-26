package io.github.taetae98coding.diary.domain.browser.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import io.github.taetae98coding.diary.domain.browser.ChromeSessionImportManager
import io.github.taetae98coding.diary.domain.browser.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.browser.repository.ChromeSessionImportSettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ChromeSessionImportSettingUseCaseTest :
    BehaviorSpec({
        Given("선택 저장에 성공한다") {
            val profileDirectory = "profile-" + fixtureMonkey.giveMeOne<String>()
            val repository = mockk<ChromeSessionImportSettingRepository>()
            coEvery { repository.setProfileDirectory(directory = any()) } just runs
            coEvery { repository.unsetProfileDirectory() } just runs

            When("프로필을 고른다") {
                val manager = mockk<ChromeSessionImportManager>(relaxed = true)
                val result = SelectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository, chromeSessionImportManager = manager)(parameter = profileDirectory)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-016 그 프로필이 한 번 저장되고, 지운 뒤 가져오도록 요청되며 성공이 전달된다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.setProfileDirectory(directory = profileDirectory) }
                    verify(exactly = 1) { manager.requestImport(clearsBefore = true) }
                }
            }

            When("선택 안 함으로 되돌린다") {
                val manager = mockk<ChromeSessionImportManager>(relaxed = true)
                val result = UnselectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository, chromeSessionImportManager = manager)(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-017 없음이 한 번 저장되고, 지우도록 요청되며 성공이 전달된다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.unsetProfileDirectory() }
                    verify(exactly = 1) { manager.requestImport(clearsBefore = true) }
                }
            }
        }

        Given("선택 저장에 실패한다") {
            val profileDirectory = "profile-" + fixtureMonkey.giveMeOne<String>()
            val repository = mockk<ChromeSessionImportSettingRepository>()
            coEvery { repository.setProfileDirectory(directory = any()) } throws IllegalStateException("write failed")
            coEvery { repository.unsetProfileDirectory() } throws IllegalStateException("write failed")

            When("프로필을 고른다") {
                val manager = mockk<ChromeSessionImportManager>(relaxed = true)
                val result = SelectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository, chromeSessionImportManager = manager)(parameter = profileDirectory)

                Then("실패가 그대로 전달되고 가져오기를 요청하지 않는다") {
                    result.shouldBeFailure()
                    verify(exactly = 0) { manager.requestImport(any()) }
                }
            }

            When("선택 안 함으로 되돌린다") {
                val manager = mockk<ChromeSessionImportManager>(relaxed = true)
                val result = UnselectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository, chromeSessionImportManager = manager)(parameter = Unit)

                Then("실패가 그대로 전달되고 지우기를 요청하지 않는다") {
                    result.shouldBeFailure()
                    verify(exactly = 0) { manager.requestImport(any()) }
                }
            }
        }

        Given("앱이 보이게 된다") {
            val manager = mockk<ChromeSessionImportManager>(relaxed = true)

            When("로그인 정보 가져오기를 요청한다") {
                val result = RequestChromeSessionImportUseCase(chromeSessionImportManager = manager)(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-018 지우지 않고 가져오도록 요청되며 성공이 전달된다") {
                    result.shouldBeSuccess()
                    verify(exactly = 1) { manager.requestImport(clearsBefore = false) }
                }
            }
        }

        Given("가져오는 상태가 바뀐다") {
            val manager = mockk<ChromeSessionImportManager>()
            every { manager.state } returns flowOf(ChromeSessionImportState.IDLE, ChromeSessionImportState.IMPORTING, ChromeSessionImportState.IMPORTED)

            When("가져오는 상태를 조회한다") {
                val useCase = GetChromeSessionImportStateUseCase(chromeSessionImportManager = manager)

                Then("상태가 바뀌는 대로 성공으로 전달된다") {
                    useCase(parameter = Unit).test {
                        awaitItem() shouldBe Result.success(ChromeSessionImportState.IDLE)
                        awaitItem() shouldBe Result.success(ChromeSessionImportState.IMPORTING)
                        awaitItem() shouldBe Result.success(ChromeSessionImportState.IMPORTED)
                        awaitComplete()
                    }
                }
            }
        }

        Given("저장된 선택을 읽을 수 있다") {
            val profileDirectory = "profile-" + fixtureMonkey.giveMeOne<String>()
            val repository = mockk<ChromeSessionImportSettingRepository>()
            every { repository.getProfileDirectory() } returns flowOf("", profileDirectory)

            When("고른 프로필을 조회한다") {
                val useCase = GetChromeSessionProfileDirectoryUseCase(chromeSessionImportSettingRepository = repository)

                Then("저장된 값이 바뀌는 대로 성공으로 전달된다") {
                    useCase(parameter = Unit).test {
                        awaitItem() shouldBe Result.success("")
                        awaitItem() shouldBe Result.success(profileDirectory)
                        awaitComplete()
                    }
                }
            }
        }

        Given("저장된 선택을 읽을 수 없다") {
            val repository = mockk<ChromeSessionImportSettingRepository>()
            every { repository.getProfileDirectory() } returns flow { throw IllegalStateException("read failed") }

            When("고른 프로필을 조회한다") {
                val useCase = GetChromeSessionProfileDirectoryUseCase(chromeSessionImportSettingRepository = repository)

                Then("실패가 전달된다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeFailure()
                        awaitComplete()
                    }
                }
            }
        }

        Given("Chrome 프로필 목록을 읽을 수 있다") {
            val profileList = List(2) { fixtureMonkey.giveMeOne<ChromeProfile>() }
            val repository = mockk<ChromeProfileRepository>()
            coEvery { repository.findAll() } returns profileList

            When("고를 수 있는 프로필을 조회한다") {
                val result = FindChromeProfileListUseCase(chromeProfileRepository = repository)(parameter = Unit)

                Then("읽은 목록을 그 순서대로 전달한다") {
                    result shouldBe Result.success(profileList)
                }
            }
        }

        Given("Chrome 프로필 목록을 읽을 수 없다") {
            val repository = mockk<ChromeProfileRepository>()
            coEvery { repository.findAll() } throws IllegalStateException("read failed")

            When("고를 수 있는 프로필을 조회한다") {
                val result = FindChromeProfileListUseCase(chromeProfileRepository = repository)(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-013 실패를 그대로 전달한다") {
                    result.shouldBeFailure()
                }
            }
        }

        Given("실행 환경의 제공 여부가 정해져 있다") {
            listOf(true, false).forEach { isSupported ->
                val repository = mockk<ChromeSessionImportSettingRepository>()
                every { repository.isSupported } returns isSupported

                When("제공 여부가 $isSupported 인 환경에서 제공 여부를 조회한다") {
                    val result = FindChromeSessionImportSupportUseCase(chromeSessionImportSettingRepository = repository)(parameter = Unit)

                    Then("그 제공 여부가 성공으로 전달된다") {
                        result shouldBe Result.success(isSupported)
                    }
                }
            }
        }
    })
