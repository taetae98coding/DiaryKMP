package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUnreadableException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk

private const val MAX_SIZE = 52_428_800L

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ReadFileUploadSourceUseCaseTest :
    BehaviorSpec({
        Given("고른 파일의 크기가 테스트 데이터와 같다") {
            When("그 파일을 고른다") {
                Then("TC-FILE-ADD-FEATURE-005 50MB 이하는 고른 파일이 되고 넘으면 크기 초과로 실패한다") {
                    mapOf(
                        0L to true,
                        MAX_SIZE to true,
                        MAX_SIZE + 1 to false,
                    ).forEach { (size, isSelected) ->
                        val source = fixtureMonkey.fileUploadSource(size = size)
                        val fileRepository = mockk<FileRepository>()
                        coEvery { fileRepository.readSource(uri = source.uri) } returns source

                        val result = ReadFileUploadSourceUseCase(fileRepository = fileRepository)(parameter = source.uri)

                        if (isSelected) {
                            result.shouldBeSuccess() shouldBe source
                        } else {
                            result.shouldBeFailure().shouldBeInstanceOf<FileTooLargeException>()
                        }
                    }
                }
            }
        }

        Given("고른 파일의 이름이나 크기를 읽을 수 없다") {
            val uri = fixtureMonkey.fileUri()
            val exception = FileUnreadableException(name = fixtureMonkey.giveMeOne<String>(), cause = IllegalStateException())
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.readSource(uri = uri) } throws exception

            When("그 파일을 고른다") {
                val result = ReadFileUploadSourceUseCase(fileRepository = fileRepository)(parameter = uri)

                Then("TC-FILE-ADD-FEATURE-005 읽지 못했다는 실패로 끝난다") {
                    result.shouldBeFailure().shouldBeInstanceOf<FileUnreadableException>()
                }
            }
        }
    })
