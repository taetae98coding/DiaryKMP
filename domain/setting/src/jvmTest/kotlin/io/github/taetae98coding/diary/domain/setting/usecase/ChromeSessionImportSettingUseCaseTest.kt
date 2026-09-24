package io.github.taetae98coding.diary.domain.setting.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.setting.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.setting.repository.ChromeSessionImportSettingRepository
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

private const val PROFILE_DIRECTORY = "Profile 1"

class ChromeSessionImportSettingUseCaseTest :
    BehaviorSpec({
        Given("선택 저장에 성공한다") {
            val repository = mockk<ChromeSessionImportSettingRepository>()
            coEvery { repository.setProfileDirectory(directory = any()) } just runs
            coEvery { repository.unsetProfileDirectory() } just runs

            When("프로필을 고른다") {
                val result = SelectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository)(parameter = PROFILE_DIRECTORY)

                Then("그 프로필이 한 번 저장되고 성공이 전달된다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.setProfileDirectory(directory = PROFILE_DIRECTORY) }
                }
            }

            When("선택 안 함으로 되돌린다") {
                val result = UnselectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository)(parameter = Unit)

                Then("없음이 한 번 저장되고 성공이 전달된다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.unsetProfileDirectory() }
                }
            }
        }

        Given("선택 저장에 실패한다") {
            val repository = mockk<ChromeSessionImportSettingRepository>()
            coEvery { repository.setProfileDirectory(directory = any()) } throws IllegalStateException("write failed")
            coEvery { repository.unsetProfileDirectory() } throws IllegalStateException("write failed")

            When("프로필을 고른다") {
                val result = SelectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository)(parameter = PROFILE_DIRECTORY)

                Then("실패가 그대로 전달된다") {
                    result.shouldBeFailure()
                }
            }

            When("선택 안 함으로 되돌린다") {
                val result = UnselectChromeSessionProfileUseCase(chromeSessionImportSettingRepository = repository)(parameter = Unit)

                Then("실패가 그대로 전달된다") {
                    result.shouldBeFailure()
                }
            }
        }

        Given("저장된 선택을 읽을 수 있다") {
            val repository = mockk<ChromeSessionImportSettingRepository>()
            every { repository.getProfileDirectory() } returns flowOf("", PROFILE_DIRECTORY)

            When("고른 프로필을 조회한다") {
                val useCase = GetChromeSessionProfileDirectoryUseCase(chromeSessionImportSettingRepository = repository)

                Then("저장된 값이 바뀌는 대로 성공으로 전달된다") {
                    useCase(parameter = Unit).test {
                        awaitItem() shouldBe Result.success("")
                        awaitItem() shouldBe Result.success(PROFILE_DIRECTORY)
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
            val profileList = listOf(ChromeProfile(directory = "Default", name = "TaeJong"), ChromeProfile(directory = PROFILE_DIRECTORY, name = "Work"))
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
