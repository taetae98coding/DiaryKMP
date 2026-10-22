package io.github.taetae98coding.diary.data.browser.repository

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.browser.cookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.BrowserSettingLocalDataSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

private const val PROFILE_A = "Default"
private const val PROFILE_B = "Profile 1"

class ChromeSessionImportSettingRepositoryImplTest :
    FunSpec({
        test("TC-ff647b05-ab2d-4a7c-b4e2-1b777b23ff20 저장된 값이 없으면 고른 프로필이 없음을 제공한다") {
            val repository = repository(directoryFlow = MutableStateFlow(""))

            repository.getProfileDirectory().first() shouldBe ""
        }

        test("TC-c247585a-0e54-480f-8d45-ae55450a14da 저장한 선택을 이후 조회에서 제공한다") {
            val repository = repository(directoryFlow = MutableStateFlow(""))

            repository.setProfileDirectory(directory = PROFILE_A)
            repository.getProfileDirectory().first() shouldBe PROFILE_A

            repository.unsetProfileDirectory()
            repository.getProfileDirectory().first() shouldBe ""
        }

        test("TC-7c799f5a-7b25-4da6-b06d-fcd421e16a9a 다시 저장하면 마지막에 저장한 선택을 제공한다") {
            val repository = repository(directoryFlow = MutableStateFlow(PROFILE_A))

            repository.unsetProfileDirectory()
            repository.setProfileDirectory(directory = PROFILE_B)

            repository.getProfileDirectory().first() shouldBe PROFILE_B
        }

        test("TC-d5152151-dea2-4201-a096-de0744c604b6 조회 중에 선택이 바뀌면 이어서 새 값을 제공한다") {
            val repository = repository(directoryFlow = MutableStateFlow(""))

            repository.getProfileDirectory().test {
                awaitItem() shouldBe ""

                repository.setProfileDirectory(directory = PROFILE_A)

                awaitItem() shouldBe PROFILE_A
            }
        }

        test("TC-05358690-676f-4acf-a76f-a2b14334d85a 저장된 값을 읽을 수 없으면 없음을 제공하지 않고 실패를 그대로 알린다") {
            val failure = IllegalStateException("browser setting read failed")
            val repository =
                ChromeSessionImportSettingRepositoryImpl(
                    browserSettingLocalDataSource =
                        mockk {
                            every { getChromeSessionProfileDirectory() } returns flow { throw failure }
                        },
                    chromeCookieLocalDataSource = mockk(),
                )

            val thrown =
                shouldThrow<IllegalStateException> {
                    repository.getProfileDirectory().first()
                }

            thrown shouldBe failure
        }

        test("제공 여부는 Chrome 쿠키 저장소의 제공 여부를 그대로 따른다") {
            listOf(true, false).forEach { isSupported ->
                val repository =
                    ChromeSessionImportSettingRepositoryImpl(
                        browserSettingLocalDataSource = mockk(),
                        chromeCookieLocalDataSource = mockk { every { this@mockk.isSupported } returns isSupported },
                    )

                repository.isSupported shouldBe isSupported
            }
        }
    })

private fun repository(directoryFlow: MutableStateFlow<String>): ChromeSessionImportSettingRepositoryImpl =
    ChromeSessionImportSettingRepositoryImpl(
        browserSettingLocalDataSource =
            mockk<BrowserSettingLocalDataSource> {
                every { getChromeSessionProfileDirectory() } returns directoryFlow
                coEvery { upsertChromeSessionProfileDirectory(directory = any()) } coAnswers {
                    directoryFlow.value = firstArg()
                }
            },
        chromeCookieLocalDataSource = mockk<ChromeCookieLocalDataSource> { every { isSupported } returns true },
    )
