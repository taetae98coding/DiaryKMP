package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.core.model.file.FileDownloadSetting
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import io.github.taetae98coding.diary.domain.file.repository.FileDownloadSettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class DownloadFileUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정이고 다운로드 위치 설정이 있다") {
            When("파일을 내려받는다") {
                Then("TC-fa368167-d81a-4307-a63d-7e49c56ec884 고른 폴더가 있으면 그 폴더에, 없으면 기본 위치에 저장하도록 맡기고 저장한 이름을 돌려준다") {
                    listOf(null, fixtureMonkey.giveMeOne<FileDownloadDirectory>()).forEach { directory ->
                        val file = fixtureMonkey.giveMeOne<DiaryFile>().copy(name = "report.pdf")
                        val savedName = FileDownloadResult.Saved(name = fixtureMonkey.giveMeOne<String>())
                        val fileRepository = mockk<FileDetailRepository>()
                        val nameCandidate = slot<(Int) -> String>()
                        coEvery { fileRepository.download(id = file.id, directory = directory, nameCandidate = capture(nameCandidate)) } returns savedName
                        val useCase = useCase(fileDetailRepository = fileRepository, setting = FileDownloadSetting(directory = directory))

                        val result = useCase(parameter = file)

                        result.shouldBeSuccess() shouldBe savedName
                        nameCandidate.captured(0) shouldBe "report.pdf"
                        nameCandidate.captured(1) shouldBe "report (1).pdf"
                    }
                }

                Then("TC-5591938c-4f65-42ea-b01d-98177ca0c509 내려받을 때마다 그 시점의 설정을 읽는다") {
                    val first = fixtureMonkey.giveMeOne<FileDownloadDirectory>()
                    val second = fixtureMonkey.giveMeOne<FileDownloadDirectory>()
                    val settingList = ArrayDeque(listOf(first, second))
                    val settingRepository = mockk<FileDownloadSettingRepository>()
                    every { settingRepository.get() } answers { flowOf(FileDownloadSetting(directory = settingList.removeFirst())) }
                    val fileRepository = mockk<FileDetailRepository>()
                    coEvery { fileRepository.download(id = any(), directory = any(), nameCandidate = any()) } returns FileDownloadResult.HandedToBrowser
                    val useCase = DownloadFileUseCase(getAccountUseCase = getAccountUseCase(), fileDetailRepository = fileRepository, fileDownloadSettingRepository = settingRepository)
                    val file = fixtureMonkey.giveMeOne<DiaryFile>()

                    useCase(parameter = file)
                    useCase(parameter = file)

                    coVerify(exactly = 1) { fileRepository.download(id = file.id, directory = first, nameCandidate = any()) }
                    coVerify(exactly = 1) { fileRepository.download(id = file.id, directory = second, nameCandidate = any()) }
                }

                Then("TC-fc37bb69-7a4c-4aac-821c-bfd475e902b5 저장 위치에 저장하지 못하면 그 실패를 그대로 알린다") {
                    val fileRepository = mockk<FileDetailRepository>()
                    coEvery { fileRepository.download(id = any(), directory = any(), nameCandidate = any()) } throws FileDownloadDirectoryUnavailableException()
                    val useCase = useCase(fileDetailRepository = fileRepository, setting = FileDownloadSetting(directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>()))

                    useCase(parameter = fixtureMonkey.giveMeOne<DiaryFile>()).shouldBeFailure().shouldBeInstanceOf<FileDownloadDirectoryUnavailableException>()
                    coVerify(exactly = 1) { fileRepository.download(id = any(), directory = any(), nameCandidate = any()) }
                }
            }
        }

        Given("다운로드 위치 설정을 읽지 못한다") {
            When("파일을 내려받는다") {
                Then("TC-eecbd8ea-88d6-4533-b956-21bffb9ce0d2 어느 위치에도 저장하지 않고 실패로 끝난다") {
                    val fileRepository = mockk<FileDetailRepository>()
                    val settingRepository = mockk<FileDownloadSettingRepository>()
                    every { settingRepository.get() } returns flow { throw IllegalStateException("setting") }
                    val useCase = DownloadFileUseCase(getAccountUseCase = getAccountUseCase(), fileDetailRepository = fileRepository, fileDownloadSettingRepository = settingRepository)

                    useCase(parameter = fixtureMonkey.giveMeOne<DiaryFile>()).shouldBeFailure()
                    coVerify(exactly = 0) { fileRepository.download(id = any(), directory = any(), nameCandidate = any()) }
                }
            }
        }

        Given("로그인한 계정을 확인하지 못했거나 게스트다") {
            When("파일을 내려받는다") {
                Then("TC-aebde2d9-41f7-47c1-bbb4-170835d3492c 서버에 요청하지 않고 실패로 끝난다") {
                    listOf(
                        Result.success(Account.Guest),
                        Result.failure(IllegalStateException("account")),
                    ).forEach { accountResult ->
                        val fileRepository = mockk<FileDetailRepository>()
                        val useCase =
                            DownloadFileUseCase(
                                getAccountUseCase = getAccountUseCase(result = accountResult),
                                fileDetailRepository = fileRepository,
                                fileDownloadSettingRepository = settingRepository(setting = FileDownloadSetting(directory = null)),
                            )

                        useCase(parameter = fixtureMonkey.giveMeOne<DiaryFile>()).shouldBeFailure()
                        coVerify(exactly = 0) { fileRepository.download(id = any(), directory = any(), nameCandidate = any()) }
                    }
                }
            }
        }
    })

private fun useCase(
    fileDetailRepository: FileDetailRepository,
    setting: FileDownloadSetting,
): DownloadFileUseCase =
    DownloadFileUseCase(
        getAccountUseCase = getAccountUseCase(),
        fileDetailRepository = fileDetailRepository,
        fileDownloadSettingRepository = settingRepository(setting = setting),
    )

private fun settingRepository(setting: FileDownloadSetting): FileDownloadSettingRepository = mockk<FileDownloadSettingRepository>().also { repository -> every { repository.get() } returns flowOf(setting) }

private fun getAccountUseCase(result: Result<Account> = Result.success(fixtureMonkey.giveMeOne<Account.User>())): GetAccountUseCase = mockk<GetAccountUseCase>().also { useCase -> every { useCase(parameter = Unit) } returns flowOf(result) }
