package io.github.taetae98coding.diary.work.fileupload.report

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.state.FileHomeViewingHolder
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
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-005 결과 알림을 보내지 않고 화면에 결과를 전달한다") {
                    resultEventList().forEach { (result, event) ->
                        runTest {
                            val fixture = ReporterFixture(isViewing = true)

                            fixture.eventHolder.event.test {
                                fixture.reporter.report(result = result)

                                awaitItem() shouldBe event
                                expectNoEvents()
                            }
                            verify(exactly = 0) { fixture.notifier.notifyResult(result = any()) }
                        }
                    }
                }
            }
        }

        Given("올리기가 끝나는 순간 FileHome을 보고 있지 않다") {
            When("테스트 데이터의 결과를 알린다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-004 TC-FILE-HOME-FEATURE-036 화면에 전달하지 않고 결과 알림을 보낸다") {
                    resultEventList().forEach { (result, _) ->
                        runTest {
                            val fixture = ReporterFixture(isViewing = false)

                            fixture.eventHolder.event.test {
                                fixture.reporter.report(result = result)

                                expectNoEvents()
                            }
                            verify(exactly = 1) { fixture.notifier.notifyResult(result = result) }
                        }
                    }
                }
            }
        }

        Given("FileHome을 보는 동안 올리기를 시작한 뒤 화면을 떠났다") {
            When("올리기가 끝나 결과를 알린다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-DOMAIN-002 끝나는 순간을 기준으로 결과 알림을 보내고 앱 화면 안에는 안내를 전달하지 않는다") {
                    runTest {
                        val fixture = ReporterFixture(isViewing = true)
                        val result = FileUploadResult.Succeeded(name = fixtureMonkey.giveMeOne<String>(), fileId = fixtureMonkey.giveMeOne<Uuid>())

                        fixture.viewingHolder.isViewing = false
                        fixture.eventHolder.event.test {
                            fixture.reporter.report(result = result)

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
    isViewing: Boolean,
) {
    val viewingHolder = FileHomeViewingHolder().also { holder -> holder.isViewing = isViewing }
    val eventHolder = FileUploadEventHolder()
    val notifier =
        mockk<FileUploadNotifier> {
            every { notifyResult(result = any()) } returns Unit
        }
    val reporter =
        FileUploadResultReporter(
            fileHomeViewingHolder = viewingHolder,
            fileUploadEventHolder = eventHolder,
            fileUploadNotifier = notifier,
        )
}
