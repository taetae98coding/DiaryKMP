package io.github.taetae98coding.diary.work.fileupload.report

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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
        Given("올리기 결과를 받는 화면이 있다") {
            When("테스트 데이터의 결과를 알린다") {
                Then("TC-f27003e1-5af1-458e-8752-01093aba51f6 TC-f33a3f46-e611-4fd6-b6a4-453ca777497a 결과 알림을 보내고 화면에도 결과를 전달한다") {
                    resultEventList().forEach { (result, event) ->
                        runTest {
                            val fixture = ReporterFixture()

                            fixture.eventHolder.event.test {
                                fixture.reporter.report(result = result)

                                awaitItem() shouldBe event
                                expectNoEvents()
                            }
                            verify(exactly = 1) { fixture.notifier.notifyResult(result = result) }
                        }
                    }
                }
            }
        }

        Given("올리기 결과를 받는 화면이 없다") {
            When("테스트 데이터의 결과를 알린 뒤 화면이 결과를 받기 시작한다") {
                Then("TC-0cfddb99-8c07-4897-a998-4682648a3f46 TC-d144a188-a4a5-4b91-8cd3-9c558f8d8d3d TC-59947c2f-2fce-4c84-a2c4-77bf7137348b 결과 알림만 보내고 화면에는 지난 결과를 전달하지 않는다") {
                    resultEventList().forEach { (result, _) ->
                        runTest {
                            val fixture = ReporterFixture()

                            fixture.reporter.report(result = result)

                            fixture.eventHolder.event.test { expectNoEvents() }
                            verify(exactly = 1) { fixture.notifier.notifyResult(result = result) }
                        }
                    }
                }
            }

            When("올리기가 성공으로 끝난 뒤 FileHome이 성공을 받기 시작한다") {
                Then("TC-7ef172cd-3ed6-454e-8f51-2c50f9ee0589 쌓아 둔 성공을 한 번 전달한다") {
                    runTest {
                        val fixture = ReporterFixture()
                        val fileId = fixtureMonkey.giveMeOne<Uuid>()

                        fixture.reporter.report(result = FileUploadResult.Succeeded(name = fixtureMonkey.giveMeOne<String>(), fileId = fileId))

                        fixture.eventHolder.succeededEvent.test {
                            awaitItem() shouldBe FileUploadEvent.Succeeded(fileId = fileId)
                            expectNoEvents()
                        }
                    }
                }
            }

            When("올리기가 실패로 끝난 뒤 FileHome이 성공을 받기 시작한다") {
                Then("아무것도 전달하지 않는다") {
                    listOf(FileUploadResult.TooLarge, FileUploadResult.Failed(name = fixtureMonkey.giveMeOne<String>())).forEach { result ->
                        runTest {
                            val fixture = ReporterFixture()

                            fixture.reporter.report(result = result)

                            fixture.eventHolder.succeededEvent.test { expectNoEvents() }
                        }
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

private class ReporterFixture {
    val eventHolder = FileUploadEventHolder()
    val notifier =
        mockk<FileUploadNotifier> {
            every { notifyResult(result = any()) } returns Unit
        }
    val reporter =
        FileUploadResultReporter(
            fileUploadEventHolder = eventHolder,
            fileUploadNotifier = notifier,
        )
}
