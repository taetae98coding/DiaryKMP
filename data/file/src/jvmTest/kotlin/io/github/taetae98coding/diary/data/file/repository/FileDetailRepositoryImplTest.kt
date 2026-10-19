package io.github.taetae98coding.diary.data.file.repository

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.file.api.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileNotFoundRemoteException
import io.github.taetae98coding.diary.data.file.paging.FilePagingSourceHolder
import io.github.taetae98coding.diary.data.file.update.FileUpdateHolder
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.io.RawSink
import kotlin.uuid.Uuid
import io.github.taetae98coding.diary.domain.file.exception.FileDownloadDirectoryUnavailableException as DomainFileDownloadDirectoryUnavailableException

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileDetailRepositoryImplTest :
    FunSpec({
        test("TC-FILE-STORAGE-DATA-030 파일 정보를 식별자로 한 번 요청하고 받은 정보를 쓰며, 실패하면 다시 요청하지 않는다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val remoteFile = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.read(id = id) } returns remoteFile
            val repository = repository(fileRemoteDataSource = fileRemoteDataSource)

            val file = repository.fetch(id = id)

            file.id shouldBe remoteFile.id
            file.name shouldBe remoteFile.name
            file.title shouldBe remoteFile.title
            file.description shouldBe remoteFile.description
            file.mimeType shouldBe remoteFile.mimeType
            file.size shouldBe remoteFile.size
            file.createdAt shouldBe remoteFile.createdAt

            listOf(FileNotFoundRemoteException(), IllegalStateException("server")).forEach { exception ->
                val failingDataSource = mockk<FileRemoteDataSource>()
                coEvery { failingDataSource.read(id = id) } throws exception

                shouldThrow<Exception> { repository(fileRemoteDataSource = failingDataSource).fetch(id = id) }
                coVerify(exactly = 1) { failingDataSource.read(id = id) }
            }
        }

        test("TC-FILE-STORAGE-DATA-031 정보 고치기는 식별자와 세 값을 함께 보내고, 실패해도 다시 요청하지 않는다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val detail = fixtureMonkey.giveMeOne<DiaryFileDetail>()
            val remoteFile = fixtureMonkey.giveMeOne<FileRemoteEntity>()
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.update(id = id, name = detail.name, title = detail.title, description = detail.description) } returns remoteFile

            repository(fileRemoteDataSource = fileRemoteDataSource).updateDetail(id = id, detail = detail).title shouldBe remoteFile.title

            val failingDataSource = mockk<FileRemoteDataSource>()
            coEvery { failingDataSource.update(id = any(), name = any(), title = any(), description = any()) } throws FileNotFoundRemoteException()
            shouldThrow<FileNotFoundRemoteException> { repository(fileRemoteDataSource = failingDataSource).updateDetail(id = id, detail = detail) }
            coVerify(exactly = 1) { failingDataSource.update(id = any(), name = any(), title = any(), description = any()) }
        }

        test("TC-FILE-STORAGE-DATA-032 TC-FILE-HOME-FEATURE-055 고친 정보는 목록을 다시 요청하지 않고 같은 파일에만 덧씌워진다") {
            runTest {
                val fileA = fixtureMonkey.giveMeOne<FileRemoteEntity>()
                val fileB = fixtureMonkey.giveMeOne<FileRemoteEntity>()
                val newTitle = fixtureMonkey.nonBlankString()
                val newName = fixtureMonkey.nonBlankString()
                val updatedA = fileA.copy(title = newTitle, name = newName)
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.readList(cursor = null, size = 20) } returns listOf(fileA, fileB)
                coEvery { fileRemoteDataSource.update(id = fileA.id, name = any(), title = any(), description = any()) } returns updatedA
                val updateHolder = FileUpdateHolder()
                val repository = repository(fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = updateHolder)
                val listRepository = listRepository(fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = updateHolder)

                listRepository.page().asSnapshot()
                repository.updateDetail(id = fileA.id, detail = DiaryFileDetail(name = newName, title = newTitle, description = fileA.description))

                repository.getUpdated().test {
                    val updated = awaitItem()

                    updated.keys shouldBe setOf(fileA.id)
                    updated.getValue(fileA.id).title shouldBe newTitle
                    updated.getValue(fileA.id).name shouldBe newName
                }
                coVerify(exactly = 1) { fileRemoteDataSource.readList(cursor = any(), size = any()) }
            }
        }

        test("TC-FILE-HOME-FEATURE-056 고친 뒤 목록을 처음부터 다시 불러오면 서버가 돌려준 값을 쓴다") {
            runTest {
                val file = fixtureMonkey.giveMeOne<FileRemoteEntity>()
                val editedTitle = fixtureMonkey.nonBlankString()
                val fromOtherDevice = file.copy(title = editedTitle + fixtureMonkey.nonBlankString())
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.readList(cursor = null, size = 20) } returns listOf(file) andThen listOf(fromOtherDevice)
                coEvery { fileRemoteDataSource.update(id = file.id, name = any(), title = any(), description = any()) } returns file.copy(title = editedTitle)
                val updateHolder = FileUpdateHolder()
                val repository = repository(fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = updateHolder)
                val listRepository = listRepository(fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = updateHolder)

                val snapshot =
                    listRepository.page().asSnapshot {
                        repository.updateDetail(id = file.id, detail = DiaryFileDetail(name = file.name, title = editedTitle, description = file.description))
                        listRepository.refresh()
                    }

                snapshot.single().title shouldBe fromOtherDevice.title
                repository.getUpdated().test { awaitItem().shouldBeEmpty() }
            }
        }

        test("고치기 전에 시작한 불러오기가 그 파일을 받아 와도 고친 정보를 걷지 않는다") {
            val holder = FileUpdateHolder()
            val file = fixtureMonkey.giveMeOne<io.github.taetae98coding.diary.core.model.file.DiaryFile>()
            val loadStartVersion = holder.currentVersion()

            holder.put(file = file)
            holder.onLoaded(idSet = setOf(file.id), loadStartVersion = loadStartVersion)

            holder.updated.test { awaitItem().keys shouldBe setOf(file.id) }
        }

        test("TC-FILE-DOWNLOAD-DATA-003 내려받기는 고른 위치와 이름 후보를 저장 위치에 넘기고, 받는 내용은 그 파일의 식별자로 요청해 쓴다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>()
            val result = FileDownloadResult.Saved(name = fixtureMonkey.giveMeOne<String>())
            val sink = mockk<RawSink>()
            val nameCandidate: (Int) -> String = { index -> "name-$index" }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>(relaxUnitFun = true)
            val fileDownloadLocalDataSource = mockk<FileDownloadLocalDataSource>()
            val write = slot<suspend (RawSink) -> Unit>()
            coEvery { fileDownloadLocalDataSource.save(directory = directory, nameCandidate = nameCandidate, write = capture(write)) } coAnswers {
                write.captured(sink)
                result
            }
            val repository = repository(fileRemoteDataSource = fileRemoteDataSource, fileDownloadLocalDataSource = fileDownloadLocalDataSource)

            repository.download(id = id, directory = directory, nameCandidate = nameCandidate) shouldBe result
            coVerify(exactly = 1) { fileRemoteDataSource.download(id = id, sink = sink) }
        }

        test("TC-FILE-DOWNLOAD-FEATURE-002 저장 위치에 쓸 수 없으면 저장 위치 실패로 바꿔 알린다") {
            val fileDownloadLocalDataSource = mockk<FileDownloadLocalDataSource>()
            coEvery { fileDownloadLocalDataSource.save(directory = any(), nameCandidate = any(), write = any()) } throws FileDownloadDirectoryUnavailableException()
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            val repository = repository(fileRemoteDataSource = fileRemoteDataSource, fileDownloadLocalDataSource = fileDownloadLocalDataSource)

            shouldThrow<DomainFileDownloadDirectoryUnavailableException> {
                repository.download(id = fixtureMonkey.giveMeOne<Uuid>(), directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>(), nameCandidate = { "" })
            }
            coVerify(exactly = 0) { fileRemoteDataSource.download(id = any(), sink = any()) }
        }

        test("TC-FILE-DOWNLOAD-FEATURE-003 TC-FILE-DOWNLOAD-DOMAIN-003 서버에서 받지 못하면 그 실패를 그대로 알리고 다시 요청하지 않는다") {
            listOf(FileNotFoundRemoteException(), IllegalStateException("connection")).forEach { exception ->
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.download(id = any(), sink = any()) } throws exception
                val fileDownloadLocalDataSource = mockk<FileDownloadLocalDataSource>()
                val write = slot<suspend (RawSink) -> Unit>()
                coEvery { fileDownloadLocalDataSource.save(directory = any(), nameCandidate = any(), write = capture(write)) } coAnswers {
                    write.captured(mockk())
                    FileDownloadResult.HandedToBrowser
                }
                val repository = repository(fileRemoteDataSource = fileRemoteDataSource, fileDownloadLocalDataSource = fileDownloadLocalDataSource)

                shouldThrow<Exception> { repository.download(id = fixtureMonkey.giveMeOne<Uuid>(), directory = null, nameCandidate = { "" }) }::class shouldBe exception::class
                coVerify(exactly = 1) { fileRemoteDataSource.download(id = any(), sink = any()) }
            }
        }
    })

private fun repository(
    fileRemoteDataSource: FileRemoteDataSource,
    fileUpdateHolder: FileUpdateHolder = FileUpdateHolder(),
    fileDownloadLocalDataSource: FileDownloadLocalDataSource = mockk(),
): FileDetailRepositoryImpl =
    FileDetailRepositoryImpl(
        fileRemoteDataSource = fileRemoteDataSource,
        fileUpdateHolder = fileUpdateHolder,
        fileDownloadLocalDataSource = fileDownloadLocalDataSource,
    )

private fun listRepository(
    fileRemoteDataSource: FileRemoteDataSource,
    fileUpdateHolder: FileUpdateHolder,
): FileRepositoryImpl =
    FileRepositoryImpl(
        fileLocalDataSource = mockk(),
        fileRemoteDataSource = fileRemoteDataSource,
        filePagingSourceHolder = FilePagingSourceHolder(fileRemoteDataSource = fileRemoteDataSource, fileUpdateHolder = fileUpdateHolder),
        fileUpdateHolder = fileUpdateHolder,
    )
