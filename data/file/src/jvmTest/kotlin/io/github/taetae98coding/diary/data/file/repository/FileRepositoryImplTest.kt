package io.github.taetae98coding.diary.data.file.repository

import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readByteArray

private const val MAX_SIZE = 52_428_800L

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileRepositoryImplTest :
    FunSpec({
        test("TC-FILE-STORAGE-DOMAIN-001 50MB 이하 파일은 올리고 넘는 파일은 서버에 요청하지 않고 크기 초과로 실패한다") {
            mapOf(
                0L to true,
                MAX_SIZE to true,
                MAX_SIZE + 1 to false,
            ).forEach { (size, isUploaded) ->
                val uri = fixtureMonkey.fileUri()
                val fileLocalDataSource = fileLocalDataSource(uri = uri, size = size)
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any()) } returns remoteFile()
                val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource)

                if (isUploaded) {
                    repository.create(uri = uri, maxSize = MAX_SIZE)
                    coVerify(exactly = 1) { fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = size, openContent = any()) }
                } else {
                    shouldThrow<FileTooLargeException> { repository.create(uri = uri, maxSize = MAX_SIZE) }
                    coVerify(exactly = 0) { fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any()) }
                }
            }
        }

        test("TC-FILE-STORAGE-DOMAIN-002 기기가 알려 주는 이름과 형식을 함께 보내고 형식을 모르면 일반 파일로 보낸다") {
            mapOf(
                "application/pdf" to "application/pdf",
                "" to "application/octet-stream",
            ).forEach { (mimeType, sentMimeType) ->
                val uri = fixtureMonkey.fileUri()
                val name = "보고서-${fixtureMonkey.giveMeOne<Int>()}.pdf"
                val fileLocalDataSource = fileLocalDataSource(uri = uri, name = name, mimeType = mimeType)
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any()) } returns remoteFile()
                val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource)

                repository.create(uri = uri, maxSize = MAX_SIZE)

                coVerify(exactly = 1) { fileRemoteDataSource.upload(name = name, mimeType = sentMimeType, contentLength = any(), openContent = any()) }
            }
        }

        test("TC-FILE-STORAGE-DOMAIN-003 이름이나 크기를 알 수 없는 파일은 서버에 요청하지 않고 크기 초과가 아닌 실패로 끝난다") {
            listOf(
                { dataSource: FileLocalDataSource, uri: FileUri -> coEvery { dataSource.name(uri = uri) } throws IllegalStateException("name") },
                { dataSource: FileLocalDataSource, uri: FileUri -> coEvery { dataSource.size(uri = uri) } throws IllegalStateException("size") },
            ).forEach { unreadable ->
                val uri = fixtureMonkey.fileUri()
                val fileLocalDataSource = fileLocalDataSource(uri = uri)
                unreadable(fileLocalDataSource, uri)
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource)

                val exception = shouldThrow<Exception> { repository.create(uri = uri, maxSize = MAX_SIZE) }

                exception.shouldNotBeInstanceOf<FileTooLargeException>()
                coVerify(exactly = 0) { fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any()) }
            }
        }

        test("TC-FILE-STORAGE-DOMAIN-008 올리는 도중 내용을 읽을 수 없으면 크기 초과가 아닌 실패로 끝난다") {
            val uri = fixtureMonkey.fileUri()
            val exception = IllegalStateException("content cannot be read")
            val fileLocalDataSource = fileLocalDataSource(uri = uri)
            coEvery { fileLocalDataSource.openSource(uri = uri) } throws exception
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any())
            } coAnswers { arg<suspend () -> RawSource>(3).invoke().close().let { remoteFile() } }
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource)

            shouldThrow<IllegalStateException> { repository.create(uri = uri, maxSize = MAX_SIZE) } shouldBeSameInstanceAs exception
        }

        test("TC-FILE-STORAGE-DATA-005 고른 위치의 내용을 그대로 올리고 서버가 돌려준 파일 정보를 전달한다") {
            val uri = fixtureMonkey.fileUri()
            val bytes = "file-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()
            val fileLocalDataSource = fileLocalDataSource(uri = uri, size = bytes.size.toLong())
            coEvery { fileLocalDataSource.openSource(uri = uri) } answers { Buffer().apply { write(bytes) } }
            val remoteFile = remoteFile()
            val openContentSlot = slot<suspend () -> RawSource>()
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = capture(openContentSlot))
            } returns remoteFile
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource)

            val actual = repository.create(uri = uri, maxSize = MAX_SIZE)

            openContentSlot.captured().buffered().use { source -> source.readByteArray() } shouldBe bytes
            actual.id shouldBe remoteFile.id
            actual.name shouldBe remoteFile.name
            actual.mimeType shouldBe remoteFile.mimeType
            actual.size shouldBe remoteFile.size
            actual.createdAt shouldBe remoteFile.createdAt
        }

        test("TC-FILE-STORAGE-DATA-006 서버가 크기 초과로 거절하면 크기 초과 실패로 바꾸고 그 밖의 실패는 그대로 전달한다") {
            val uri = fixtureMonkey.fileUri()
            val otherException = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any())
            } throws FileTooLargeRemoteException(message = "too large") andThenThrows otherException
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource(uri = uri), fileRemoteDataSource = fileRemoteDataSource)

            shouldThrow<FileTooLargeException> { repository.create(uri = uri, maxSize = MAX_SIZE) }
            shouldThrow<IllegalStateException> { repository.create(uri = uri, maxSize = MAX_SIZE) } shouldBeSameInstanceAs otherException
        }

        test("TC-FILE-STORAGE-DATA-001 목록을 처음 불러올 때 마지막 파일 없이 20개를 한 번 요청한다") {
            val fileList = List(3) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns fileList
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource)

            val snapshot = repository.page().asSnapshot()

            snapshot.map { file -> file.id } shouldBe fileList.map { file -> file.id }
            coVerify(exactly = 1) { fileRemoteDataSource.fetch(cursor = any(), size = any()) }
        }

        test("TC-FILE-STORAGE-DATA-002 목록의 끝에 다가가면 받은 마지막 파일의 올린 시각과 식별자를 기준으로 20개를 이어서 요청한다") {
            val firstPage = List(20) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns firstPage
            coEvery { fileRemoteDataSource.fetch(cursor = match { cursor -> cursor != null }, size = any()) } returns emptyList()
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource)

            repository.page().asSnapshot { scrollTo(index = firstPage.lastIndex) }

            coVerify(exactly = 1) {
                fileRemoteDataSource.fetch(cursor = FileCursorRemoteEntity(createdAt = firstPage.last().createdAt, id = firstPage.last().id), size = 20)
            }
        }
    }) {
    public companion object {
        private fun remoteFile(): FileRemoteEntity = fixtureMonkey.giveMeOne<FileRemoteEntity>()

        private fun fileLocalDataSource(
            uri: FileUri,
            name: String = "memo.txt",
            mimeType: String = "text/plain",
            size: Long = 1,
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
