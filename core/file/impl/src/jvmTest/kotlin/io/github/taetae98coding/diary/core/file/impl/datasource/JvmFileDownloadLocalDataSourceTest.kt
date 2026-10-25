package io.github.taetae98coding.diary.core.file.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.file.api.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import java.io.File
import java.nio.file.Files

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class JvmFileDownloadLocalDataSourceTest :
    FunSpec({
        test("TC-fa368167-d81a-4307-a63d-7e49c56ec884 고른 폴더가 있으면 그 폴더에, 없으면 기본 위치에 받은 내용을 그대로 저장한다") {
            runTest {
                val bytes = fixtureMonkey.giveMeOne<ByteArray>()
                val chosen = Files.createTempDirectory("chosen").toFile()
                val defaultDirectory = Files.createTempDirectory("home").toFile().resolve("Downloads")
                val dataSource = dataSource(defaultDirectory = defaultDirectory)

                listOf(
                    FileDownloadDirectory(uri = chosen.absolutePath, name = chosen.name) to chosen,
                    null to defaultDirectory,
                ).forEach { (directory, expectedDirectory) ->
                    val name = dataSource.save(directory = directory, nameCandidate = { "report.pdf" }, write = { sink -> sink.writeBytes(bytes) }).savedName()

                    name shouldBe "report.pdf"
                    expectedDirectory.resolve(name).readBytes() shouldBe bytes
                }
            }
        }

        test("TC-f9cd7730-015c-4120-aa0a-dda6e46df8b0 같은 이름이 있으면 덮어쓰지 않고 겹치지 않는 가장 작은 번호의 이름으로 저장한다") {
            runTest {
                listOf(
                    emptyList<String>() to "report.pdf",
                    listOf("report.pdf") to "report (1).pdf",
                    listOf("report.pdf", "report (1).pdf") to "report (2).pdf",
                    listOf("report.pdf", "report (2).pdf") to "report (1).pdf",
                ).forEach { (existingList, expected) ->
                    val directory = Files.createTempDirectory("collision").toFile()
                    existingList.forEach { name -> directory.resolve(name).writeText(name) }
                    val candidateList = listOf("report.pdf", "report (1).pdf", "report (2).pdf", "report (3).pdf")

                    val name =
                        dataSource()
                            .save(
                                directory = FileDownloadDirectory(uri = directory.absolutePath, name = directory.name),
                                nameCandidate = candidateList::get,
                                write = { sink -> sink.writeBytes(byteArrayOf(1)) },
                            ).savedName()

                    name shouldBe expected
                    existingList.forEach { existing -> directory.resolve(existing).readText() shouldBe existing }
                }
            }
        }

        test("TC-fc37bb69-7a4c-4aac-821c-bfd475e902b5 고른 폴더에 쓸 수 없으면 기본 위치에 대신 저장하지 않고 저장 위치 실패로 끝난다") {
            runTest {
                val defaultDirectory = Files.createTempDirectory("default").toFile()
                val missing = Files.createTempDirectory("missing").toFile().also(File::delete)

                shouldThrow<FileDownloadDirectoryUnavailableException> {
                    dataSource(defaultDirectory = defaultDirectory).save(
                        directory = FileDownloadDirectory(uri = missing.absolutePath, name = missing.name),
                        nameCandidate = { "report.pdf" },
                        write = { sink -> sink.writeBytes(byteArrayOf(1)) },
                    )
                }

                defaultDirectory.listFiles().orEmpty().toList() shouldBe emptyList()
            }
        }

        test("TC-c23b3f37-81ef-42d6-bcf3-5563583e7157 받는 도중 실패하거나 멈추면 쓰던 파일을 남기지 않는다") {
            runTest {
                listOf(IllegalStateException("connection"), CancellationException("left")).forEach { exception ->
                    val directory = Files.createTempDirectory("partial").toFile()

                    shouldThrow<Exception> {
                        dataSource().save(
                            directory = FileDownloadDirectory(uri = directory.absolutePath, name = directory.name),
                            nameCandidate = { "report.pdf" },
                            write = { sink ->
                                sink.writeBytes(byteArrayOf(1, 2, 3))
                                throw exception
                            },
                        )
                    }

                    directory.listFiles().orEmpty().toList() shouldBe emptyList()
                }
            }
        }

        test("TC-fc37bb69-7a4c-4aac-821c-bfd475e902b5 기본 위치를 만들 수 없으면 저장 위치 실패로 끝난다") {
            runTest {
                val blocker = Files.createTempFile("blocker", ".txt").toFile()

                shouldThrow<FileDownloadDirectoryUnavailableException> {
                    dataSource(defaultDirectory = blocker.resolve("Downloads")).save(directory = null, nameCandidate = { "report.pdf" }, write = {})
                }
            }
        }

        test("폴더를 고르면 그 경로와 폴더 이름을 기억하고, 폴더가 아니면 실패한다") {
            runTest {
                val directory = Files.createTempDirectory("retain").toFile()
                val file = Files.createTempFile("retain", ".txt").toFile()
                val dataSource = dataSource()

                dataSource.retainDirectory(uri = directory.absolutePath) shouldBe FileDownloadDirectory(uri = directory.absolutePath, name = directory.name)
                shouldThrow<IllegalStateException> { dataSource.retainDirectory(uri = file.absolutePath) }
            }
        }
    })

private fun TestScope.dataSource(defaultDirectory: File = Files.createTempDirectory("default").toFile()): JvmFileDownloadLocalDataSource =
    JvmFileDownloadLocalDataSource(
        dispatcher = StandardTestDispatcher(testScheduler),
        defaultDirectory = defaultDirectory.absolutePath,
    )

private fun RawSink.writeBytes(bytes: ByteArray) {
    val buffer = Buffer().apply { write(bytes) }

    write(buffer, buffer.size)
    flush()
}

private fun FileDownloadResult.savedName(): String = shouldBeInstanceOf<FileDownloadResult.Saved>().name
