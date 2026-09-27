package io.github.taetae98coding.diary.domain.browser.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.browser.repository.ChromeSessionImportSettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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
            listOf(selectedProfileDirectory(), "").forEach { profileDirectory ->
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
            val profileList = profileList()
            val unlistedDirectory = "unlisted-" + profileList.joinToString(separator = "-") { profile -> profile.directory }
            val useCase = useCase(isSupported = true, profileDirectory = unlistedDirectory, profileRepository = profileRepository(profileList))

            When("가져올 프로필을 찾는다") {
                val result = useCase(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-011 없음을 전달한다") {
                    result shouldBe Result.success(null)
                }
            }
        }

        Given("고른 프로필이 목록에 있다") {
            val profileList = profileList()
            val useCase = useCase(isSupported = true, profileDirectory = profileList.last().directory, profileRepository = profileRepository(profileList))

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
            val useCase = useCase(isSupported = true, profileDirectory = selectedProfileDirectory(), profileRepository = profileRepository)

            When("가져올 프로필을 찾는다") {
                val result = useCase(parameter = Unit)

                Then("TC-CHROME-SESSION-IMPORT-DOMAIN-014 실패를 전달한다") {
                    result.shouldBeFailure()
                }
            }
        }
    })

private fun selectedProfileDirectory(): String = "profile-" + fixtureMonkey.giveMeOne<String>()

// 목록 안의 프로필을 폴더로 구분할 수 있도록 서로 다른 폴더를 준다.
private fun profileList(): List<ChromeProfile> = List(2) { index -> fixtureMonkey.giveMeOne<ChromeProfile>().copy(directory = "profile-$index-" + fixtureMonkey.giveMeOne<String>()) }

private fun profileRepository(profileList: List<ChromeProfile>): ChromeProfileRepository = mockk { coEvery { findAll() } returns profileList }

private fun useCase(
    isSupported: Boolean,
    profileDirectory: String,
    profileRepository: ChromeProfileRepository,
): FindChromeSessionImportProfileUseCase {
    val settingRepository = mockk<ChromeSessionImportSettingRepository>()
    every { settingRepository.isSupported } returns isSupported
    every { settingRepository.getProfileDirectory() } returns flowOf(profileDirectory)

    return FindChromeSessionImportProfileUseCase(chromeSessionImportSettingRepository = settingRepository, chromeProfileRepository = profileRepository)
}
