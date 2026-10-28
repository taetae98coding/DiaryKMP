package io.github.taetae98coding.diary.data.file.repository

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUploadResult
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.data.file.update.FileUpdateHolder
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUnreadableException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileRepositoryImplTest :
    FunSpec({
        test("TC-7657ffc9-4037-4d7c-b233-3962e9398528 기기가 알려 주는 이름, 형식, 크기를 읽고 형식을 모르면 일반 파일로 본다") {
            mapOf(
                "application/pdf" to "application/pdf",
                "" to "application/octet-stream",
            ).forEach { (mimeType, sourceMimeType) ->
                val uri = fixtureMonkey.fileUri()
                val name = "보고서-${fixtureMonkey.giveMeOne<Int>()}.pdf"
                val size = fixtureMonkey.giveMeOne<Long>()
                val repository =
                    FileRepositoryImpl(
                        fileLocalDataSource = fileLocalDataSource(uri = uri, name = name, mimeType = mimeType, size = size),
                        fileRemoteDataSource = mockk(),
                        fileUpdateHolder = FileUpdateHolder(),
                    )

                repository.readSource(uri = uri) shouldBe FileUploadSource(uri = uri, name = name, mimeType = sourceMimeType, size = size)
            }
        }

        test("TC-aab59fbf-b414-4195-aed3-3fecd5be4f91 이름을 읽은 뒤 크기를 읽지 못하면 읽어 둔 이름을 실패에 싣고, 이름을 읽지 못하면 이름 없이 실패한다") {
            val uri = fixtureMonkey.fileUri()
            val name = "file-${fixtureMonkey.giveMeOne<String>()}"
            val sizeUnreadable = fileLocalDataSource(uri = uri)
            coEvery { sizeUnreadable.name(uri = uri) } returns name
            coEvery { sizeUnreadable.size(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val nameUnreadable = fileLocalDataSource(uri = uri)
            coEvery { nameUnreadable.name(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())

            shouldThrow<FileUnreadableException> {
                FileRepositoryImpl(fileLocalDataSource = sizeUnreadable, fileRemoteDataSource = mockk(), fileUpdateHolder = FileUpdateHolder()).readSource(uri = uri)
            }.name shouldBe name
            shouldThrow<FileUnreadableException> {
                FileRepositoryImpl(fileLocalDataSource = nameUnreadable, fileRemoteDataSource = mockk(), fileUpdateHolder = FileUpdateHolder()).readSource(uri = uri)
            }.name shouldBe ""
        }

        test("앞선 실행이 올리려고 기기에 둔 사본을 지우도록 기기 저장소에 맡긴다") {
            val fileLocalDataSource = mockk<FileLocalDataSource>()
            coEvery { fileLocalDataSource.deleteLeftoverCopies() } returns Unit

            FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = mockk(), fileUpdateHolder = FileUpdateHolder()).deleteLeftoverUploadSources()

            coVerify(exactly = 1) { fileLocalDataSource.deleteLeftoverCopies() }
        }

        test("TC-aab59fbf-b414-4195-aed3-3fecd5be4f91 이름이나 크기를 알 수 없는 파일은 서버에 요청하지 않고 크기 초과가 아닌 실패로 끝난다") {
            listOf(
                { dataSource: FileLocalDataSource, uri: FileUri -> coEvery { dataSource.name(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>()) },
                { dataSource: FileLocalDataSource, uri: FileUri -> coEvery { dataSource.size(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>()) },
            ).forEach { unreadable ->
                val uri = fixtureMonkey.fileUri()
                val fileLocalDataSource = fileLocalDataSource(uri = uri)
                unreadable(fileLocalDataSource, uri)
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = FileUpdateHolder())

                val exception = shouldThrow<Exception> { repository.readSource(uri = uri) }

                exception.shouldNotBeInstanceOf<FileTooLargeException>()
                coVerify(exactly = 0) { fileRemoteDataSource.upload(name = any(), title = any(), description = any(), mimeType = any(), contentLength = any(), accountId = any(), openContent = any(), onSent = any()) }
            }
        }

        test("TC-24dc37eb-887e-4982-bbe8-d5e86d4a989c 올리는 도중 내용을 읽을 수 없으면 크기 초과가 아닌 실패로 끝난다") {
            val source = fixtureMonkey.fileUploadSource()
            val exception = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileLocalDataSource = fileLocalDataSource(uri = source.uri)
            coEvery { fileLocalDataSource.openSource(uri = source.uri) } throws exception
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(name = any(), title = any(), description = any(), mimeType = any(), contentLength = any(), accountId = any(), openContent = any(), onSent = any())
            } coAnswers { arg<suspend () -> RawSource>(6).invoke().close().let { remoteFile() } }
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = FileUpdateHolder())

            shouldThrow<IllegalStateException> { repository.create(source = source, title = fixtureMonkey.giveMeOne<String>(), description = fixtureMonkey.giveMeOne<String>(), accountId = fixtureMonkey.giveMeOne<Uuid>(), onSent = {}) } shouldBeSameInstanceAs exception
        }

        test("TC-11549cf7-1e0e-4997-8f8c-b73c93f15246 고른 위치의 내용과 크기를 제목, 설명과 함께 그대로 올리고 보낸 양과 서버가 돌려준 파일 정보를 전달한다") {
            val title = "title-${fixtureMonkey.giveMeOne<String>()}"
            val description = fixtureMonkey.giveMeOne<String>()
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val bytes = "file-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()
            val source = fixtureMonkey.fileUploadSource(size = bytes.size.toLong())
            val fileLocalDataSource = fileLocalDataSource(uri = source.uri, size = source.size)
            coEvery { fileLocalDataSource.openSource(uri = source.uri) } answers { Buffer().apply { write(bytes) } }
            val remoteFile = remoteFile()
            val openContentSlot = slot<suspend () -> RawSource>()
            val onSentSlot = slot<(Long) -> Unit>()
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(
                    name = source.name,
                    title = title,
                    description = description,
                    mimeType = source.mimeType,
                    contentLength = source.size,
                    accountId = accountId,
                    openContent = capture(openContentSlot),
                    onSent = capture(onSentSlot),
                )
            } answers {
                onSentSlot.captured(source.size)
                remoteFile
            }
            val sentBytesList = mutableListOf<Long>()
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = FileUpdateHolder())

            val actual = repository.create(source = source, title = title, description = description, accountId = accountId) { sentBytes -> sentBytesList += sentBytes }

            openContentSlot.captured().buffered().use { rawSource -> rawSource.readByteArray() } shouldBe bytes
            sentBytesList shouldBe listOf(source.size)
            actual.id shouldBe remoteFile.id
            actual.name shouldBe remoteFile.name
            actual.title shouldBe remoteFile.title
            actual.description shouldBe remoteFile.description
            actual.mimeType shouldBe remoteFile.mimeType
            actual.size shouldBe remoteFile.size
            actual.createdAt shouldBe remoteFile.createdAt
        }

        test("TC-e4baf560-0898-49fe-ad5c-a7c6d1c4716b 서버가 크기 초과로 거절하면 크기 초과 실패로 바꾸고 그 밖의 실패는 그대로 전달한다") {
            val source = fixtureMonkey.fileUploadSource()
            val otherException = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(name = any(), title = any(), description = any(), mimeType = any(), contentLength = any(), accountId = any(), openContent = any(), onSent = any())
            } throws FileTooLargeRemoteException(message = fixtureMonkey.giveMeOne<String>()) andThenThrows otherException
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource(uri = source.uri), fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = FileUpdateHolder())

            shouldThrow<FileTooLargeException> { repository.create(source = source, title = fixtureMonkey.giveMeOne<String>(), description = fixtureMonkey.giveMeOne<String>(), accountId = fixtureMonkey.giveMeOne<Uuid>(), onSent = {}) }
            shouldThrow<IllegalStateException> { repository.create(source = source, title = fixtureMonkey.giveMeOne<String>(), description = fixtureMonkey.giveMeOne<String>(), accountId = fixtureMonkey.giveMeOne<Uuid>(), onSent = {}) } shouldBeSameInstanceAs otherException
        }

        test("고른 파일의 읽기 권한을 붙들고 놓는 일은 기기의 파일 보관 수단에 맡긴다") {
            val uri = fixtureMonkey.fileUri()
            val fileLocalDataSource = mockk<FileLocalDataSource>(relaxUnitFun = true)
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = mockk(), fileUpdateHolder = FileUpdateHolder())

            repository.addUploadSource(uri = uri)
            repository.removeUploadSource(uri = uri)

            coVerify(exactly = 1) { fileLocalDataSource.retain(uri = uri) }
            coVerify(exactly = 1) { fileLocalDataSource.release(uri = uri) }
        }

        test("앞선 실행에서 이어지는 올리기의 진행과 결과를 이름, 크기, 보낸 양과 결과 종류 그대로 전달한다") {
            val upload = fixtureMonkey.giveMeOne<ContinuedFileUploadRemoteEntity>()
            val file = remoteFile()
            val name = fixtureMonkey.giveMeOne<String>()
            val fileRemoteDataSource = mockk<FileRemoteDataSource>(relaxUnitFun = true)
            every { fileRemoteDataSource.getContinuedUpload() } returns flowOf(upload, null)
            every { fileRemoteDataSource.getContinuedUploadResult() } returns
                flowOf(
                    ContinuedFileUploadResultRemoteEntity.Succeeded(name = name, file = file),
                    ContinuedFileUploadResultRemoteEntity.TooLarge(name = name),
                    ContinuedFileUploadResultRemoteEntity.Failed(name = name),
                )
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = FileUpdateHolder())

            repository.getContinuedUpload().test {
                awaitItem() shouldBe ContinuedFileUpload(name = upload.name, size = upload.contentLength, sentBytes = upload.sentBytes)
                awaitItem() shouldBe null
                awaitComplete()
            }
            repository.getContinuedUploadResult().test {
                val succeeded = awaitItem() as ContinuedFileUploadResult.Succeeded
                succeeded.name shouldBe name
                succeeded.file.id shouldBe file.id
                awaitItem() shouldBe ContinuedFileUploadResult.TooLarge(name = name)
                awaitItem() shouldBe ContinuedFileUploadResult.Failed(name = name)
                awaitComplete()
            }
            val exceptAccountId = fixtureMonkey.giveMeOne<Uuid>()
            repository.deleteContinuedUpload(exceptAccountId = exceptAccountId)

            coVerify(exactly = 1) { fileRemoteDataSource.cancelContinuedUpload(exceptAccountId = exceptAccountId) }
        }

        test("TC-c3c9a856-e7b9-41c7-b35b-025cbe78c827 목록을 처음 불러올 때 마지막 파일 없이 20개를 한 번 요청한다") {
            runTest {
                val fileList = List(3) { remoteFile() }
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.readList(cursor = null, size = 20) } returns fileList
                val repository =
                    FileRepositoryImpl(
                        fileLocalDataSource = mockk(),
                        fileRemoteDataSource = fileRemoteDataSource,
                        fileUpdateHolder = FileUpdateHolder(),
                    )

                val snapshot = repository.page().asSnapshot()

                snapshot.map { file -> file.id } shouldBe fileList.map { file -> file.id }
                coVerify(exactly = 1) { fileRemoteDataSource.readList(cursor = any(), size = any()) }
            }
        }

        test("TC-4c3f6c39-bda2-4ac2-8036-5bf7096a0332 목록의 파일 정보에 서버가 돌려준 제목과 설명이 담긴다") {
            runTest {
                val remoteFile = remoteFile()
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.readList(cursor = null, size = 20) } returns listOf(remoteFile)
                val repository =
                    FileRepositoryImpl(
                        fileLocalDataSource = mockk(),
                        fileRemoteDataSource = fileRemoteDataSource,
                        fileUpdateHolder = FileUpdateHolder(),
                    )

                val snapshot = repository.page().asSnapshot()

                snapshot.single().title shouldBe remoteFile.title
                snapshot.single().description shouldBe remoteFile.description
            }
        }

        test("TC-e83464f1-ff45-45d4-b832-57f4b4f82143 목록의 끝에 다가가면 받은 마지막 파일의 올린 시각과 식별자를 기준으로 20개를 이어서 요청한다") {
            runTest {
                val firstPage = List(20) { remoteFile() }
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.readList(cursor = null, size = 20) } returns firstPage
                coEvery { fileRemoteDataSource.readList(cursor = match { cursor -> cursor != null }, size = any()) } returns emptyList()
                val repository =
                    FileRepositoryImpl(
                        fileLocalDataSource = mockk(),
                        fileRemoteDataSource = fileRemoteDataSource,
                        fileUpdateHolder = FileUpdateHolder(),
                    )

                repository.page().asSnapshot { scrollTo(index = firstPage.lastIndex) }

                coVerify(exactly = 1) {
                    fileRemoteDataSource.readList(cursor = FileCursorRemoteEntity(createdAt = firstPage.last().createdAt, id = firstPage.last().id), size = 20)
                }
            }
        }
    }) {
    public companion object {
        private fun remoteFile(): FileRemoteEntity = fixtureMonkey.giveMeOne<FileRemoteEntity>()

        private fun fileLocalDataSource(
            uri: FileUri,
            name: String = "file-${fixtureMonkey.giveMeOne<Int>()}.txt",
            mimeType: String = "application/${fixtureMonkey.giveMeOne<Int>()}",
            size: Long = fixtureMonkey.fileUploadSource().size,
        ): FileLocalDataSource {
            val dataSource = mockk<FileLocalDataSource>()
            coEvery { dataSource.name(uri = uri) } returns name
            coEvery { dataSource.mimeType(uri = uri) } returns mimeType
            coEvery { dataSource.size(uri = uri) } returns size
            coEvery { dataSource.openSource(uri = uri) } answers { Buffer() }
            return dataSource
        }
    }
}
