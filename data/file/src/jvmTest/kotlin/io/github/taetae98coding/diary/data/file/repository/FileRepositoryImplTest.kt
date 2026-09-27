package io.github.taetae98coding.diary.data.file.repository

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUploadResult
import io.github.taetae98coding.diary.core.model.file.DiaryFile
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
import io.github.taetae98coding.diary.data.file.paging.FilePagingSourceHolder
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readByteArray

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileRepositoryImplTest :
    FunSpec({
        test("TC-FILE-STORAGE-DOMAIN-002 기기가 알려 주는 이름, 형식, 크기를 읽고 형식을 모르면 일반 파일로 본다") {
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
                        filePagingSourceHolder = mockk(relaxed = true),
                    )

                repository.findSource(uri = uri) shouldBe FileUploadSource(uri = uri, name = name, mimeType = sourceMimeType, size = size)
            }
        }

        test("TC-FILE-STORAGE-DOMAIN-003 이름을 읽은 뒤 크기를 읽지 못하면 읽어 둔 이름을 실패에 싣고, 이름을 읽지 못하면 이름 없이 실패한다") {
            val uri = fixtureMonkey.fileUri()
            val name = "file-${fixtureMonkey.giveMeOne<String>()}"
            val sizeUnreadable = fileLocalDataSource(uri = uri)
            coEvery { sizeUnreadable.name(uri = uri) } returns name
            coEvery { sizeUnreadable.size(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val nameUnreadable = fileLocalDataSource(uri = uri)
            coEvery { nameUnreadable.name(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())

            shouldThrow<FileUnreadableException> {
                FileRepositoryImpl(fileLocalDataSource = sizeUnreadable, fileRemoteDataSource = mockk(), filePagingSourceHolder = mockk(relaxed = true)).findSource(uri = uri)
            }.name shouldBe name
            shouldThrow<FileUnreadableException> {
                FileRepositoryImpl(fileLocalDataSource = nameUnreadable, fileRemoteDataSource = mockk(), filePagingSourceHolder = mockk(relaxed = true)).findSource(uri = uri)
            }.name shouldBe ""
        }

        test("앞선 실행이 올리려고 기기에 둔 사본을 지우도록 기기 저장소에 맡긴다") {
            val fileLocalDataSource = mockk<FileLocalDataSource>()
            coEvery { fileLocalDataSource.deleteLeftoverCopies() } returns Unit

            FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = mockk(), filePagingSourceHolder = mockk(relaxed = true)).deleteLeftoverUploadSources()

            coVerify(exactly = 1) { fileLocalDataSource.deleteLeftoverCopies() }
        }

        test("TC-FILE-STORAGE-DOMAIN-003 이름이나 크기를 알 수 없는 파일은 서버에 요청하지 않고 크기 초과가 아닌 실패로 끝난다") {
            listOf(
                { dataSource: FileLocalDataSource, uri: FileUri -> coEvery { dataSource.name(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>()) },
                { dataSource: FileLocalDataSource, uri: FileUri -> coEvery { dataSource.size(uri = uri) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>()) },
            ).forEach { unreadable ->
                val uri = fixtureMonkey.fileUri()
                val fileLocalDataSource = fileLocalDataSource(uri = uri)
                unreadable(fileLocalDataSource, uri)
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = mockk(relaxed = true))

                val exception = shouldThrow<Exception> { repository.findSource(uri = uri) }

                exception.shouldNotBeInstanceOf<FileTooLargeException>()
                coVerify(exactly = 0) { fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any(), onSent = any()) }
            }
        }

        test("TC-FILE-STORAGE-DOMAIN-008 올리는 도중 내용을 읽을 수 없으면 크기 초과가 아닌 실패로 끝난다") {
            val source = fixtureMonkey.fileUploadSource()
            val exception = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileLocalDataSource = fileLocalDataSource(uri = source.uri)
            coEvery { fileLocalDataSource.openSource(uri = source.uri) } throws exception
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any(), onSent = any())
            } coAnswers { arg<suspend () -> RawSource>(3).invoke().close().let { remoteFile() } }
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = mockk(relaxed = true))

            shouldThrow<IllegalStateException> { repository.create(source = source, onSent = {}) } shouldBeSameInstanceAs exception
        }

        test("TC-FILE-STORAGE-DATA-005 고른 위치의 내용과 크기를 그대로 올리고 보낸 양과 서버가 돌려준 파일 정보를 전달한다") {
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
                    mimeType = source.mimeType,
                    contentLength = source.size,
                    openContent = capture(openContentSlot),
                    onSent = capture(onSentSlot),
                )
            } answers {
                onSentSlot.captured(source.size)
                remoteFile
            }
            val sentBytesList = mutableListOf<Long>()
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = mockk(relaxed = true))

            val actual = repository.create(source = source) { sentBytes -> sentBytesList += sentBytes }

            openContentSlot.captured().buffered().use { rawSource -> rawSource.readByteArray() } shouldBe bytes
            sentBytesList shouldBe listOf(source.size)
            actual.id shouldBe remoteFile.id
            actual.name shouldBe remoteFile.name
            actual.mimeType shouldBe remoteFile.mimeType
            actual.size shouldBe remoteFile.size
            actual.createdAt shouldBe remoteFile.createdAt
        }

        test("TC-FILE-STORAGE-DATA-006 서버가 크기 초과로 거절하면 크기 초과 실패로 바꾸고 그 밖의 실패는 그대로 전달한다") {
            val source = fixtureMonkey.fileUploadSource()
            val otherException = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery {
                fileRemoteDataSource.upload(name = any(), mimeType = any(), contentLength = any(), openContent = any(), onSent = any())
            } throws FileTooLargeRemoteException(message = fixtureMonkey.giveMeOne<String>()) andThenThrows otherException
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource(uri = source.uri), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = mockk(relaxed = true))

            shouldThrow<FileTooLargeException> { repository.create(source = source, onSent = {}) }
            shouldThrow<IllegalStateException> { repository.create(source = source, onSent = {}) } shouldBeSameInstanceAs otherException
        }

        test("고른 파일의 읽기 권한을 붙들고 놓는 일은 기기의 파일 보관 수단에 맡긴다") {
            val uri = fixtureMonkey.fileUri()
            val fileLocalDataSource = mockk<FileLocalDataSource>(relaxUnitFun = true)
            val repository = FileRepositoryImpl(fileLocalDataSource = fileLocalDataSource, fileRemoteDataSource = mockk(), filePagingSourceHolder = mockk(relaxed = true))

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
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = mockk(relaxed = true))

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
            repository.deleteContinuedUpload()

            coVerify(exactly = 1) { fileRemoteDataSource.cancelContinuedUpload() }
        }

        test("TC-FILE-STORAGE-DATA-001 목록을 처음 불러올 때 마지막 파일 없이 20개를 한 번 요청한다") {
            val fileList = List(3) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns fileList
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource))

            val snapshot = repository.page().asSnapshot()

            snapshot.map { file -> file.id } shouldBe fileList.map { file -> file.id }
            coVerify(exactly = 1) { fileRemoteDataSource.fetch(cursor = any(), size = any()) }
        }

        test("TC-FILE-STORAGE-DATA-002 목록의 끝에 다가가면 받은 마지막 파일의 올린 시각과 식별자를 기준으로 20개를 이어서 요청한다") {
            val firstPage = List(20) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns firstPage
            coEvery { fileRemoteDataSource.fetch(cursor = match { cursor -> cursor != null }, size = any()) } returns emptyList()
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource))

            repository.page().asSnapshot { scrollTo(index = firstPage.lastIndex) }

            coVerify(exactly = 1) {
                fileRemoteDataSource.fetch(cursor = FileCursorRemoteEntity(createdAt = firstPage.last().createdAt, id = firstPage.last().id), size = 20)
            }
        }

        test("다시 불러오기는 첫 페이지를 받은 뒤 그 페이지로 목록을 새로 시작하고 첫 페이지를 다시 요청하지 않는다") {
            val firstPage = List(3) { remoteFile() }
            val refreshedPage = List(3) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns firstPage andThen refreshedPage
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource))

            val snapshot = repository.page().asSnapshot { repository.refresh() }

            snapshot.map { file -> file.id } shouldBe refreshedPage.map { file -> file.id }
            coVerify(exactly = 2) { fileRemoteDataSource.fetch(cursor = null, size = 20) }
        }

        test("TC-FILE-HOME-FEATURE-023 다시 불러오기에 실패하면 실패를 전달하고 이전 목록을 그대로 둔다") {
            val firstPage = List(3) { remoteFile() }
            val exception = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns firstPage andThenThrows exception
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource))

            val snapshot =
                repository.page().asSnapshot {
                    shouldThrow<IllegalStateException> { repository.refresh() }.message shouldBe exception.message
                }

            snapshot.map { file -> file.id } shouldBe firstPage.map { file -> file.id }
            coVerify(exactly = 2) { fileRemoteDataSource.fetch(cursor = null, size = 20) }
        }

        test("TC-FILE-HOME-FEATURE-038 다시 불러오기로 받은 20개의 끝에 이르면 받은 마지막 파일을 기준으로 20개를 이어서 요청한다") {
            val firstPage = List(20) { remoteFile() }
            val refreshedPage = List(20) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns firstPage andThen refreshedPage
            coEvery { fileRemoteDataSource.fetch(cursor = match { cursor -> cursor != null }, size = any()) } returns emptyList()
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource))

            repository.page().asSnapshot {
                repository.refresh()
                scrollTo(index = refreshedPage.lastIndex)
            }

            coVerify(exactly = 1) {
                fileRemoteDataSource.fetch(cursor = FileCursorRemoteEntity(createdAt = refreshedPage.last().createdAt, id = refreshedPage.last().id), size = 20)
            }
        }

        test("TC-FILE-HOME-FEATURE-043 다시 불러오는 동안 다른 계정의 목록으로 바뀌면 받은 첫 페이지를 새 목록에 넣지 않는다") {
            val pageA = List(3) { remoteFile() }
            val lateA = List(3) { remoteFile() }
            val pageB = List(3) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource))
            lateinit var pagingB: Flow<PagingData<DiaryFile>>
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns pageA andThenAnswer {
                pagingB = repository.page()
                lateA
            } andThen pageB

            repository.page().asSnapshot()
            repository.refresh()
            val snapshotB = pagingB.asSnapshot()

            snapshotB.map { file -> file.id } shouldBe pageB.map { file -> file.id }
        }

        test("받은 첫 페이지는 새로 만든 목록의 첫 불러오기에 쓰이지 않는다") {
            val pageA = List(3) { remoteFile() }
            val refreshed = List(3) { remoteFile() }
            val pageB = List(3) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = 20) } returns pageA andThen refreshed andThen pageB
            val repository = FileRepositoryImpl(fileLocalDataSource = mockk(), fileRemoteDataSource = fileRemoteDataSource, filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource))

            repository.page().asSnapshot()
            repository.refresh()
            val snapshotB = repository.page().asSnapshot()

            snapshotB.map { file -> file.id } shouldBe pageB.map { file -> file.id }
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
