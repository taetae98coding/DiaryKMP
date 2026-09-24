package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.browser.repository.ChromeSessionImportSettingRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

private const val PROFILE_DIRECTORY = "Profile 1"

private val profileList: List<ChromeProfile> =
    listOf(
        ChromeProfile(directory = "Default", name = "TaeJong"),
        ChromeProfile(directory = PROFILE_DIRECTORY, name = "Work"),
    )

class FindChromeSessionImportProfileUseCaseTest :
    BehaviorSpec({
        Given("프로필을 고르지 않았다") {
            val profileRepository = mockk<ChromeProfileRepository>()
            val useCase = useCase(isSupported = true, profileDirectory = "", profileRepository = profileRepository)

            When("가져올 프로필을 찾는다") {
                val result = useCase(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-001 프로필 목록을 읽지 않고 없음을 전달한다") {
                    result shouldBe Result.success(null)
                    coVerify(exactly = 0) { profileRepository.findAll() }
                }
            }
        }

        Given("제공하지 않는 환경이다") {
            listOf(PROFILE_DIRECTORY, "").forEach { profileDirectory ->
                val settingRepository = mockk<ChromeSessionImportSettingRepository>()
                every { settingRepository.isSupported } returns false
                val profileRepository = mockk<ChromeProfileRepository>()
                val useCase = FindChromeSessionImportProfileUseCase(chromeSessionImportSettingRepository = settingRepository, chromeProfileRepository = profileRepository)

                When("선택이 '$profileDirectory' 인 상태에서 가져올 프로필을 찾는다") {
                    val result = useCase(parameter = Unit)

                    Then("TC-CHROME-SESSION-IMPORT-DOMAIN-002 선택과 목록을 읽지 않고 없음을 전달한다") {
                        result shouldBe Result.success(null)
                        coVerify(exactly = 0) { settingRepository.getProfileDirectory() }
                        coVerify(exactly = 0) { profileRepository.findAll() }
                    }
                }
            }
        }

        Given("고른 프로필이 목록에 없다") {
            val useCase = useCase(isSupported = true, profileDirectory = "Profile 9")

            When("가져올 프로필을 찾는다") {
                val result = useCase(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-011 없음을 전달한다") {
                    result shouldBe Result.success(null)
                }
            }
        }

        Given("고른 프로필이 목록에 있다") {
            val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY)

            When("가져올 프로필을 찾는다") {
                val result = useCase(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-012 그 프로필을 전달한다") {
                    result shouldBe Result.success(profileList.last())
                }
            }
        }

        Given("프로필을 골라 두었지만 프로필 목록을 읽을 수 없다") {
            val profileRepository = mockk<ChromeProfileRepository>()
            coEvery { profileRepository.findAll() } throws IllegalStateException("local state unreadable")
            val useCase = useCase(isSupported = true, profileDirectory = PROFILE_DIRECTORY, profileRepository = profileRepository)

            When("가져올 프로필을 찾는다") {
                val result = useCase(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-014 실패를 전달한다") {
                    result.shouldBeFailure()
                }
            }
        }
    })

private fun useCase(
    isSupported: Boolean,
    profileDirectory: String,
    profileRepository: ChromeProfileRepository = mockk { coEvery { findAll() } returns profileList },
): FindChromeSessionImportProfileUseCase {
    val settingRepository = mockk<ChromeSessionImportSettingRepository>()
    every { settingRepository.isSupported } returns isSupported
    every { settingRepository.getProfileDirectory() } returns flowOf(profileDirectory)

    return FindChromeSessionImportProfileUseCase(chromeSessionImportSettingRepository = settingRepository, chromeProfileRepository = profileRepository)
}
