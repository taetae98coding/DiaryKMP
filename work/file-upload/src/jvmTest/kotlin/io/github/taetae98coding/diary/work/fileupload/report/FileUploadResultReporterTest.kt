package io.github.taetae98coding.diary.work.fileupload.report

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.state.FileScreenViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileUploadResultReporterTest :
    BehaviorSpec({
        Given("올리기가 끝나는 순간 FileHome을 보고 있다") {
            When("테스트 데이터의 결과를 알린다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-005 결과 알림을 보내지 않고 FileHome에만 결과를 전달한다") {
                    resultEventList().forEach { (result, event) ->
                        runTest {
                            val fixture = ReporterFixture(viewingScreen = FileScreen.HOME)

                            fixture.eventHolder.getEvent(screen = FileScreen.ADD).test {
                                fixture.eventHolder.getEvent(screen = FileScreen.HOME).test {
                                    fixture.reporter.report(result = result)

                                    awaitItem() shouldBe event
                                    expectNoEvents()
                                }
                                expectNoEvents()
                            }
                            verify(exactly = 0) { fixture.notifier.notifyResult(result = any()) }
                        }
                    }
                }
            }
        }

        Given("올리기가 끝나는 순간 FileHome에서 연 FileAdd를 보고 있다") {
            When("테스트 데이터의 결과를 알린다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-019 TC-FILE-ADD-FEATURE-013 결과 알림을 보내지 않고 FileAdd에 결과를 전달한다") {
                    resultEventList().forEach { (result, event) ->
                        runTest {
                            val fixture = ReporterFixture(viewingScreen = FileScreen.ADD)

                            fixture.eventHolder.getEvent(screen = FileScreen.ADD).test {
                                fixture.reporter.report(result = result)

                                awaitItem() shouldBe event
                                expectNoEvents()
                            }
                            verify(exactly = 0) { fixture.notifier.notifyResult(result = any()) }
                        }
                    }
                }
            }

            When("올리기가 성공으로 끝난다") {
                Then("TC-FILE-HOME-FEATURE-049 FileHome에는 FileAdd를 보는 동안 성공했다고 전달한다") {
                    runTest {
                        val fixture = ReporterFixture(viewingScreen = FileScreen.ADD)
                        val fileId = fixtureMonkey.giveMeOne<Uuid>()

                        fixture.eventHolder.getEvent(screen = FileScreen.HOME).test {
                            fixture.reporter.report(result = FileUploadResult.Succeeded(name = fixtureMonkey.giveMeOne<String>(), fileId = fileId))

                            awaitItem() shouldBe FileUploadEvent.SucceededOnFileAdd(fileId = fileId)
                            expectNoEvents()
                        }
                    }
                }
            }

            When("올리기가 실패로 끝난다") {
                Then("TC-FILE-HOME-FEATURE-050 FileHome에는 아무것도 전달하지 않는다") {
                    listOf(FileUploadResult.TooLarge, FileUploadResult.Failed(name = fixtureMonkey.giveMeOne<String>())).forEach { result ->
                        runTest {
                            val fixture = ReporterFixture(viewingScreen = FileScreen.ADD)

                            fixture.eventHolder.getEvent(screen = FileScreen.HOME).test {
                                fixture.reporter.report(result = result)

                                expectNoEvents()
                            }
                        }
                    }
                }
            }
        }

        Given("올리기가 끝나는 순간 파일 화면을 보고 있지 않다") {
            When("테스트 데이터의 결과를 알린다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-004 TC-FILE-HOME-FEATURE-036 화면에 전달하지 않고 결과 알림을 보낸다") {
                    resultEventList().forEach { (result, _) ->
                        runTest {
                            val fixture = ReporterFixture(viewingScreen = null)

                            fixture.eventHolder.getEvent(screen = FileScreen.HOME).test {
                                fixture.reporter.report(result = result)

                                expectNoEvents()
                            }
                            verify(exactly = 1) { fixture.notifier.notifyResult(result = result) }
                        }
                    }
                }
            }
        }

        Given("FileAdd를 보는 동안 올리기를 시작한 뒤 FileHome을 거쳐 화면을 떠났다") {
            When("올리기가 끝나 결과를 알린다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-DOMAIN-002 끝나는 순간을 기준으로 결과 알림을 보내고 앱 화면 안에는 안내를 전달하지 않는다") {
                    runTest {
                        val fixture = ReporterFixture(viewingScreen = FileScreen.ADD)
                        val result = FileUploadResult.Succeeded(name = fixtureMonkey.giveMeOne<String>(), fileId = fixtureMonkey.giveMeOne<Uuid>())

                        fixture.viewingHolder.start(screen = FileScreen.HOME)
                        fixture.viewingHolder.stop(screen = FileScreen.ADD)
                        fixture.viewingHolder.stop(screen = FileScreen.HOME)
                        fixture.eventHolder.getEvent(screen = FileScreen.ADD).test {
                            fixture.eventHolder.getEvent(screen = FileScreen.HOME).test {
                                fixture.reporter.report(result = result)

                                expectNoEvents()
                            }
                            expectNoEvents()
                        }

                        verify(exactly = 1) { fixture.notifier.notifyResult(result = result) }
                    }
                }
            }
        }
    })

private fun resultEventList(): List<Pair<FileUploadResult, FileUploadEvent>> {
    val fileId = fixtureMonkey.giveMeOne<Uuid>()
    val name = fixtureMonkey.giveMeOne<String>()

    return listOf(
        FileUploadResult.Succeeded(name = name, fileId = fileId) to FileUploadEvent.Succeeded(fileId = fileId),
        FileUploadResult.TooLarge to FileUploadEvent.TooLarge,
        FileUploadResult.Failed(name = name) to FileUploadEvent.Failed,
    )
}

private class ReporterFixture(
    viewingScreen: FileScreen?,
) {
    val viewingHolder = FileScreenViewingHolder().also { holder -> viewingScreen?.let(holder::start) }
    val eventHolder = FileUploadEventHolder()
    val notifier =
        mockk<FileUploadNotifier> {
            every { notifyResult(result = any()) } returns Unit
        }
    val reporter =
        FileUploadResultReporter(
            fileScreenViewingHolder = viewingHolder,
            fileUploadEventHolder = eventHolder,
            fileUploadNotifier = notifier,
        )
}
