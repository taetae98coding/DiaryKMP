package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileNameBlankException
import io.github.taetae98coding.diary.domain.file.exception.FileNameTooLongException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class UpdateFileUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정이 제목, 설명, 파일 이름을 고쳤다") {
            When("수정한다") {
                Then("TC-FILE-DETAIL-DOMAIN-004 TC-FILE-DETAIL-DATA-002 세 값을 앞뒤 공백까지 그대로 보내고 서버가 돌려준 정보를 돌려준다") {
                    listOf(
                        DiaryFileDetail(name = " report.pdf", title = " 보고서 ", description = ""),
                        DiaryFileDetail(name = fixtureMonkey.nonBlankString(), title = fixtureMonkey.nonBlankString(), description = fixtureMonkey.giveMeOne<String>()),
                    ).forEach { detail ->
                        val id = fixtureMonkey.giveMeOne<Uuid>()
                        val updated = fixtureMonkey.giveMeOne<DiaryFile>()
                        val fileRepository = mockk<FileDetailRepository>()
                        coEvery { fileRepository.updateDetail(id = id, detail = detail) } returns updated
                        val useCase = UpdateFileUseCase(getAccountUseCase = getAccountUseCase(), fileDetailRepository = fileRepository)

                        val result = useCase(parameter = UpdateFileUseCase.Parameter(id = id, detail = detail))

                        result.shouldBeSuccess() shouldBe updated
                    }
                }
            }
        }

        Given("수정 성립 조건을 어긴 입력이 있다") {
            When("수정한다") {
                Then("TC-FILE-DETAIL-DOMAIN-001 TC-FILE-DETAIL-DOMAIN-002 TC-FILE-DETAIL-DOMAIN-003 처음 어긴 조건의 실패로 끝나고 서버에 보내지 않는다") {
                    listOf(
                        DiaryFileDetail(name = "", title = "", description = "") to FileTitleBlankException::class,
                        DiaryFileDetail(name = "a.txt", title = "  ", description = "") to FileTitleBlankException::class,
                        DiaryFileDetail(name = "", title = "제목", description = "") to FileNameBlankException::class,
                        DiaryFileDetail(name = "   ", title = "제목", description = "") to FileNameBlankException::class,
                        DiaryFileDetail(name = "a".repeat(256), title = "제목", description = "") to FileNameTooLongException::class,
                        DiaryFileDetail(name = "👨‍👩‍👧".repeat(52), title = "제목", description = "") to FileNameTooLongException::class,
                    ).forEach { (detail, expected) ->
                        val fileRepository = mockk<FileDetailRepository>()
                        val useCase = UpdateFileUseCase(getAccountUseCase = getAccountUseCase(), fileDetailRepository = fileRepository)

                        val result = useCase(parameter = UpdateFileUseCase.Parameter(id = fixtureMonkey.giveMeOne<Uuid>(), detail = detail))

                        result.shouldBeFailure()::class shouldBe expected
                        coVerify(exactly = 0) { fileRepository.updateDetail(id = any(), detail = any()) }
                    }
                }

                Then("TC-FILE-DETAIL-DOMAIN-003 255자 이름은 받는다") {
                    listOf("a".repeat(255), "😀".repeat(255)).forEach { name ->
                        val detail = DiaryFileDetail(name = name, title = fixtureMonkey.nonBlankString(), description = fixtureMonkey.giveMeOne<String>())
                        val fileRepository = mockk<FileDetailRepository>()
                        coEvery { fileRepository.updateDetail(id = any(), detail = detail) } returns fixtureMonkey.giveMeOne<DiaryFile>()
                        val useCase = UpdateFileUseCase(getAccountUseCase = getAccountUseCase(), fileDetailRepository = fileRepository)

                        useCase(parameter = UpdateFileUseCase.Parameter(id = fixtureMonkey.giveMeOne<Uuid>(), detail = detail)).shouldBeSuccess()
                    }
                }
            }
        }

        Given("계정이 게스트이거나 확인되지 않았다") {
            When("수정한다") {
                Then("TC-FILE-DETAIL-FEATURE-018 실패로 끝나고 서버에 보내지 않는다") {
                    listOf(
                        Result.success(Account.Guest),
                        Result.failure(IllegalStateException("account")),
                    ).forEach { accountResult ->
                        val fileRepository = mockk<FileDetailRepository>()
                        val useCase = UpdateFileUseCase(getAccountUseCase = getAccountUseCase(result = accountResult), fileDetailRepository = fileRepository)

                        val result = useCase(parameter = UpdateFileUseCase.Parameter(id = fixtureMonkey.giveMeOne<Uuid>(), detail = DiaryFileDetail(name = fixtureMonkey.nonBlankString(), title = fixtureMonkey.nonBlankString(), description = fixtureMonkey.giveMeOne<String>())))

                        result.shouldBeFailure().shouldBeInstanceOf<IllegalStateException>()
                        coVerify(exactly = 0) { fileRepository.updateDetail(id = any(), detail = any()) }
                    }
                }
            }
        }
    })

private fun getAccountUseCase(result: Result<Account> = Result.success(fixtureMonkey.giveMeOne<Account.User>())): GetAccountUseCase = mockk<GetAccountUseCase>().also { useCase -> every { useCase(parameter = Unit) } returns flowOf(result) }
